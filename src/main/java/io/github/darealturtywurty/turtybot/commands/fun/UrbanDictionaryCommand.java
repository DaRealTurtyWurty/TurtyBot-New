package io.github.darealturtywurty.turtybot.commands.fun;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import com.google.gson.JsonObject;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import io.github.darealturtywurty.turtybot.util.Constants;
import io.github.darealturtywurty.turtybot.util.core.BotUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

@RegisterBotCmd
public class UrbanDictionaryCommand implements GuildCommand {

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.FUN;
    }

    @Override
    public String getDescription() {
        return "Looks-up the result in urban dictionary!";
    }

    @Override
    public String getName() {
        return "urban";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(new OptionData(OptionType.STRING, "phrase", "The phrase to lookup", true));
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        try {
            final String text = URLEncoder.encode(ctx.getEvent().getOption("phrase").getAsString(),
                    StandardCharsets.UTF_8);
            final OkHttpClient client = new OkHttpClient();
            final Request request = new Request.Builder()
                    .url("https://mashape-community-urban-dictionary.p.rapidapi.com/define?term=" + text)
                    .get().addHeader("x-rapidapi-key", "3cddd9097emshb08d77f414126e1p17fe02jsnef39ca636003")
                    .addHeader("x-rapidapi-host", "mashape-community-urban-dictionary.p.rapidapi.com")
                    .build();

            final ResponseBody responseBody = client.newCall(request).execute().body();
            final var results = Constants.GSON.fromJson(responseBody.string(), JsonObject.class).get("list")
                    .getAsJsonArray();
            final var result1 = results.get(0).getAsJsonObject();
            final var definition = result1.get("definition").getAsString();
            final var examples = result1.get("example").getAsString();
            responseBody.close();

            final var embed = new EmbedBuilder();
            embed.setColor(BotUtils.generateRandomPastelColor());
            embed.setTimestamp(Instant.now());
            embed.setTitle("Urban Dictionary Lookup for: " + text);
            embed.addField("Definition:", definition, false);
            embed.addField("Examples", examples, false);
            ctx.getEvent().deferReply().addEmbeds(embed.build()).mentionRepliedUser(false).queue();
        } catch (final IOException e) {
            ctx.getEvent().deferReply().setContent("There was an issue accessing the database.")
                    .mentionRepliedUser(false).queue();
        }
    }

    @Override
    public boolean productionReady() {
        return true;
    }
}
