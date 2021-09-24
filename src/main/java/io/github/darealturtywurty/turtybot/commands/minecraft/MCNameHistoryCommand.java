package io.github.darealturtywurty.turtybot.commands.minecraft;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
public class MCNameHistoryCommand implements GuildCommand {

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.MINECRAFT;
    }

    @Override
    public String getDescription() {
        return "Gets the name history of a minecraft user.";
    }

    @Override
    public String getName() {
        return "mcnamehistory";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(new OptionData(OptionType.STRING, "uuid",
                "UUID of the user. " + "HINT: Use /useruuid to obtain a UUID.", true));
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        final String text = URLEncoder.encode(ctx.getEvent().getOption("uuid").getAsString(),
                StandardCharsets.UTF_8);
        try {
            final URLConnection urlc = new URL("https://api.mojang.com/user/profiles/" + text + "/names")
                    .openConnection();
            urlc.addRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
            final String result = IOUtils
                    .toString(new BufferedReader(new InputStreamReader(urlc.getInputStream())));
            final JsonArray usernames = Constants.GSON.fromJson(result, JsonArray.class).getAsJsonArray();

            System.out.println(usernames.get(0).getAsJsonObject());
            final var embed = new EmbedBuilder();
            embed.setTitle("Minecraft Name History for user: "
                    + usernames.get(0).getAsJsonObject().get("name").getAsString());
            embed.setTimestamp(Instant.now());
            embed.setColor(ctx.getMember().getColorRaw());
            final AtomicInteger counter = new AtomicInteger(1);
            for (final JsonElement elem : usernames) {
                if (elem instanceof final JsonObject object) {
                    final String name = object.get("name").getAsString();
                    String changeDate = "";
                    if (object.has("changedToAt")) {
                        changeDate = "\n" + Constants.DATE_FORMAT
                                .format(new Date(object.get("changedToAt").getAsLong()));
                    }

                    embed.addField(counter.getAndIncrement() + ".", "Name: " + name + changeDate, false);
                }
            }
            ctx.getEvent().deferReply().addEmbeds(embed.build()).mentionRepliedUser(false).queue();
        } catch (final IOException e) {
            ctx.getEvent().deferReply()
                    .setContent("There was an issue getting the name history for this user!")
                    .mentionRepliedUser(false).queue();
        }
    }

    @Override
    public boolean productionReady() {
        return true;
    }
}
