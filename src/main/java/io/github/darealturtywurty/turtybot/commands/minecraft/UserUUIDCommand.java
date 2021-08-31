package io.github.darealturtywurty.turtybot.commands.minecraft;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class UserUUIDCommand implements GuildCommand {

    public static JsonElement readJsonFromUrl(final String url)
            throws IOException, JsonParseException, JsonSyntaxException {
        final InputStream is = new URL(url).openStream();
        try (var reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            final String jsonText = readAll(reader);
            return JsonParser.parseString(jsonText);
        } finally {
            is.close();
        }
    }

    private static String readAll(final Reader rd) throws IOException {
        final var sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.MINECRAFT;
    }

    @Override
    public String getDescription() {
        return "Gets the UUID of a minecraft user.";
    }

    @Override
    public String getName() {
        return "useruuid";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(new OptionData(OptionType.STRING, "mc_username", "Minecraft Username", true));
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        final String username = ctx.getEvent().getOption("mc_username").getAsString();

        final var builder = new EmbedBuilder();
        try {
            final var jsonText = readJsonFromUrl(
                    "https://api.mojang.com/users/profiles/minecraft/" + username).toString();
            builder.setTitle("Minecraft User:");
            builder.setDescription(jsonText.split("\"")[3]);
            builder.addField("UUID:", jsonText.split("\"")[7], true);
            ctx.getEvent().deferReply().addEmbeds(builder.build()).mentionRepliedUser(false).queue();
        } catch (JsonParseException | IOException e) {
            builder.setTitle("Invalid User");
            builder.setColor(Color.decode("#EA2027"));
            builder.setDescription("A user with the name of " + username + " does not exist.");
            builder.addField("Try again with a valid username!", "", true);
            ctx.getEvent().deferReply().addEmbeds(builder.build()).mentionRepliedUser(false)
                    .queue(msg -> msg.deleteOriginal().queueAfter(15, TimeUnit.SECONDS));
        }
    }
}
