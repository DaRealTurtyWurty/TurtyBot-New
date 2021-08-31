package io.github.darealturtywurty.turtybot.commands.fun;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.google.gson.JsonObject;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import io.github.darealturtywurty.turtybot.util.Constants;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class EightBallCommand implements GuildCommand {

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.FUN;
    }

    @Override
    public String getDescription() {
        return "Answers your question to what the gods of random think.";
    }

    @Override
    public String getName() {
        return "8ball";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.STRING, "question", "What question would you like to ask?", true));
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        final String text = ctx.getEvent().getOption("question").getAsString();

        try {
            final URLConnection urlc = new URL("https://8ball.delegator.com/magic/JSON/"
                    + URLEncoder.encode(text, StandardCharsets.UTF_8)).openConnection();
            urlc.addRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
            final String result = IOUtils
                    .toString(new BufferedReader(new InputStreamReader(urlc.getInputStream())));
            urlc.getInputStream().close();
            final JsonObject magicObj = Constants.GSON.fromJson(result, JsonObject.class).get("magic")
                    .getAsJsonObject();

            final var embed = new EmbedBuilder();
            var color = Color.BLACK;
            switch (magicObj.get("type").getAsString().toLowerCase()) {
            case "affirmative":
                color = Color.GREEN;
                break;
            case "neutral":
                color = Color.BLUE;
                break;
            case "contrary":
                color = Color.RED;
                break;
            default:
                break;
            }

            embed.setColor(color);
            embed.setTimestamp(Instant.now());
            embed.setTitle("8Ball 🎱");
            embed.setDescription(magicObj.get("answer").getAsString());
            ctx.getEvent().deferReply().addEmbeds(embed.build()).mentionRepliedUser(false).queue();
        } catch (final IOException e) {
            ctx.getEvent().deferReply().setContent("There was an issue accessing the database.")
                    .mentionRepliedUser(false).queue();
        }
    }

}
