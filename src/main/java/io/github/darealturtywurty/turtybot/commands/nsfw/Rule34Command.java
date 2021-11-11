package io.github.darealturtywurty.turtybot.commands.nsfw;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.github.darealturtywurty.turtybot.util.Constants;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Rule34Command extends ListenerAdapter {

    private static final String BASE_URL = "https://r34-json-api.herokuapp.com/posts?tags=";

    @Override
    public void onGuildMessageReceived(final GuildMessageReceivedEvent event) {
        super.onGuildMessageReceived(event);
        if (event.getAuthor().isBot() || event.isWebhookMessage() || !event.getChannel().isNSFW())
            return;

        final String[] args = event.getMessage().getContentRaw().toLowerCase().trim().split(" ");
        if (args.length < 2)
            return;
        if (!args[0].equals("!rule34"))
            return;
        final String searches = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        final String[] tags = searches.split(",");
        final var tagBuilder = new StringBuilder();
        int counter = 0;
        for (final String tag : tags) {
            tagBuilder.append((counter++ != 0 ? "+" : "") + tag.trim().replace(',', '+').replace('\s', '_'));
        }
        System.out.println(tagBuilder);

        if (!tagBuilder.isEmpty()) {
            try {
                final var connection = (HttpURLConnection) new URL(BASE_URL + tagBuilder.toString())
                        .openConnection();
                connection.setRequestMethod("GET");

                final var reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                final var content = new StringBuilder();
                while ((inputLine = reader.readLine()) != null) {
                    content.append(inputLine);
                }

                reader.close();
                connection.disconnect();

                final JsonArray array = Constants.GSON.fromJson(content.toString(), JsonArray.class);
                if (array.size() > 0) {
                    final var object = (JsonObject) array.get(Constants.RANDOM.nextInt(array.size()));
                    event.getChannel().sendMessage(object.get("file_url").getAsString()).queue();
                } else {
                    event.getChannel().sendMessage(
                            "Sorry! Nothing could be found with these tags. Maybe you were too specific.")
                            .queue();
                }
            } catch (final IOException exception) {
                exception.printStackTrace();
            }
        }
    }
}
