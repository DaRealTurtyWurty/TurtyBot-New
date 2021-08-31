package io.github.darealturtywurty.turtybot.commands.fun;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.time.Instant;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.google.gson.JsonObject;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import io.github.darealturtywurty.turtybot.util.BotUtils;
import io.github.darealturtywurty.turtybot.util.Constants;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class DogImageCommand implements GuildCommand {

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.FUN;
    }

    @Override
    public String getDescription() {
        return "Random dog image.";
    }

    @Override
    public String getName() {
        return "dog";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of();
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        try {
            final URLConnection urlc = new URL("https://dog.ceo/api/breeds/image/random").openConnection();
            urlc.addRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
            final String result = IOUtils
                    .toString(new BufferedReader(new InputStreamReader(urlc.getInputStream())));
            urlc.getInputStream().close();

            final var embed = new EmbedBuilder();
            embed.setColor(BotUtils.generateRandomPastelColor());
            embed.setTimestamp(Instant.now());
            embed.setTitle("Dog 🐶");
            embed.setImage(Constants.GSON.fromJson(result, JsonObject.class).get("message").getAsString());
            ctx.getEvent().deferReply().addEmbeds(embed.build()).mentionRepliedUser(false).queue();
        } catch (final IOException e) {
            ctx.getEvent().deferReply().setContent("There was an issue accessing the database.")
                    .mentionRepliedUser(false).queue();
        }
    }
}
