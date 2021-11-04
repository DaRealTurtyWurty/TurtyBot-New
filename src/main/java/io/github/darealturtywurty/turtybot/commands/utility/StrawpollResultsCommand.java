package io.github.darealturtywurty.turtybot.commands.utility;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import io.github.darealturtywurty.turtybot.util.Constants;
import io.github.darealturtywurty.turtybot.util.core.ImageUtils;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class StrawpollResultsCommand implements GuildCommand {

    private static final int WIDTH = 1080, HEIGHT = 720;

    @SuppressWarnings("deprecation")
    public static JFreeChart createChart(final PieDataset<String> dataset, final String title,
            final boolean is3D) {
        JFreeChart chart;
        if (is3D) {
            // TODO: Rewrite this to use the new library
            chart = ChartFactory.createPieChart3D(title, dataset, false, true, Locale.getDefault());
        } else {
            chart = ChartFactory.createPieChart(title, dataset, false, true, Locale.getDefault());
        }
        return chart;
    }

    public static PieDataset<String> createDataset(final Map<String, Double> data) {
        final var dataset = new DefaultPieDataset<String>();
        data.forEach(dataset::setValue);
        return dataset;
    }

    public static BufferedImage drawChart(final JFreeChart chart) {
        final var bufferedImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D graphics = bufferedImage.createGraphics();
        chart.draw(graphics, new Rectangle2D.Double(0, 0, WIDTH, HEIGHT));
        graphics.dispose();
        return bufferedImage;
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.UTILITY;
    }

    @Override
    public String getDescription() {
        return "Retreives the results of a strawpoll.";
    }

    @Override
    public String getName() {
        return "strawpoll-results";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.STRING, "id", "Found here: https://strawpoll.com/<id>", true),
                new OptionData(OptionType.BOOLEAN, "is3d",
                        "Whether or not the data should be displayed in 3D.", true));
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        final String id = ctx.getEvent().getOption("id").getAsString();
        final boolean is3D = ctx.getEvent().getOption("is3d").getAsBoolean();
        try {
            final URLConnection urlc = new URL("https://strawpoll.com/api/poll/" + id).openConnection();
            urlc.addRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
            final String result = IOUtils
                    .toString(new BufferedReader(new InputStreamReader(urlc.getInputStream())));
            urlc.getInputStream().close();
            final JsonObject response = Constants.GSON.fromJson(result, JsonObject.class).getAsJsonObject()
                    .get("content").getAsJsonObject().get("poll").getAsJsonObject();
            final JsonArray answers = response.get("poll_answers").getAsJsonArray();

            final Map<String, Double> answerMap = new HashMap<>();
            for (final JsonElement element : answers) {
                final JsonObject answer = element.getAsJsonObject();
                answerMap.put(answer.get("answer").getAsString(), (double) answer.get("votes").getAsInt());
            }

            final PieDataset<String> dataset = createDataset(answerMap);
            final JFreeChart chart = createChart(dataset, response.get("title").getAsString(), is3D);
            final BufferedImage chartImg = drawChart(chart);
            final InputStream chartStream = ImageUtils.toInputStream(chartImg);

            ctx.getEvent().deferReply().addFile(chartStream, "chart.png").queue();
        } catch (final IOException e) {
            Constants.LOGGER.warning(e.getLocalizedMessage());
        }
    }

    @Override
    public boolean productionReady() {
        return true;
    }
}
