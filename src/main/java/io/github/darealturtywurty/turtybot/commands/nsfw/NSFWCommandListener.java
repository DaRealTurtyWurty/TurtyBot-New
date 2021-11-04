package io.github.darealturtywurty.turtybot.commands.nsfw;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.squareup.moshi.JsonDataException;

import io.github.darealturtywurty.turtybot.util.SubredditMissingException;
import io.github.darealturtywurty.turtybot.util.core.BotUtils;
import net.dean.jraw.ApiException;
import net.dean.jraw.RedditClient;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.http.OkHttpNetworkAdapter;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.oauth.Credentials;
import net.dean.jraw.oauth.OAuthHelper;
import net.dean.jraw.references.SubredditReference;
import net.dean.jraw.tree.RootCommentNode;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class NSFWCommandListener extends ListenerAdapter {

    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient();
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setLenient().create();
    public static final Map<Long, Set<BaseNSFWCommand>> COMMANDS = new HashMap<>();

    private static RedditClient redditClient;

    public NSFWCommandListener() {
        final var oAuthCreds = Credentials.userless(BotUtils.getRedditID(), BotUtils.getRedditSecret(),
                UUID.randomUUID());
        final var userAgent = new UserAgent("bot", "io.github.darealturtywurty.turtybot", "1.0.0",
                "TurtyWurty");
        NSFWCommandListener.redditClient = OAuthHelper.automatic(new OkHttpNetworkAdapter(userAgent),
                oAuthCreds);
        NSFWCommandListener.redditClient.setLogHttp(false);
    }

    protected static String getRandomSubreddit(final Set<String> subreddits) {
        return subreddits.stream().skip(ThreadLocalRandom.current().nextInt(subreddits.size())).findFirst()
                .orElse("beans");
    }

    private static void execute(final BaseNSFWCommand command, final Message message) {
        if (command.subreddits.isEmpty())
            throw new SubredditMissingException(
                    "Command by name \"" + command.name + "\" is missing subreddits!");
        final String subreddit = getRandomSubreddit(command.subreddits);
        final RootCommentNode post = getPost(command.subreddits, message.getTextChannel(), subreddit);
        if (post == null) {
            execute(command, message);
            return;
        }

        final String url = post.getSubject().getUrl().isBlank() ? post.getSubject().getThumbnail()
                : post.getSubject().getUrl();
        message.getTextChannel().sendMessage(url).queue(msg -> message.delete().queue());
    }

    @Nullable
    private static RootCommentNode getPost(final Set<String> subreddits, final TextChannel channel,
            final String subreddit) {
        try {
            return getValidSubreddit(subreddits, BotUtils.getModLogChannel(channel.getGuild()), subreddit)
                    .randomSubmission();
        } catch (JsonDataException | ApiException e) {
            if (e.getLocalizedMessage().contains("404 (Not Found)"))
                return getValidSubreddit(subreddits, BotUtils.getModLogChannel(channel.getGuild()), subreddit)
                        .randomSubmission();
            channel.sendMessage("There was an error with this post. Please try again!").queue();
        } catch (final NetworkException e) {
            channel.sendMessage("Hey! Slow down there, you are using me too much.").queue();
        }
        return null;
    }

    private static SubredditReference getValidSubreddit(final Set<String> subreddits,
            final TextChannel loggingChannel, final String subredditStr) {
        final var subredditReference = redditClient.subreddit(subredditStr);
        try {
            subredditReference.about();
            return subredditReference;
        } catch (final ApiException e) {
            System.out.println(subredditStr + " is an invalid subreddit!");

            return getValidSubreddit(subreddits, loggingChannel, getRandomSubreddit(subreddits));
        }
    }

    @Override
    public void onGuildMessageReceived(final GuildMessageReceivedEvent event) {
        super.onGuildMessageReceived(event);
        final String content = event.getMessage().getContentRaw();
        if (!event.getAuthor().isBot() && !event.isWebhookMessage() && content.startsWith("!")
                && event.getChannel().isNSFW() && COMMANDS.containsKey(event.getGuild().getIdLong())) {
            final String commandName = content.substring(1);
            if (commandName.equalsIgnoreCase("hentai")) {
                final var request = new Request.Builder().url("https://nekobot.xyz/api/image?type=hentai")
                        .build();

                try (var response = HTTP_CLIENT.newCall(request).execute()) {
                    final var url = GSON.fromJson(response.body().string(), JsonObject.class).get("message")
                            .getAsString();
                    event.getChannel()
                            .sendMessageEmbeds(
                                    new EmbedBuilder().setColor(BotUtils.generateRandomPastelColor())
                                            .setTimestamp(Instant.now()).setImage(url).build())
                            .queue(msg -> event.getMessage().delete().queue());
                } catch (final IOException e) {
                    final var strBuilder = new StringBuilder("```\n");
                    strBuilder.append(e.getLocalizedMessage() + "\n");
                    for (final StackTraceElement el : e.getStackTrace()) {
                        strBuilder.append(el.toString());
                    }
                    strBuilder.append("```");

                    event.getChannel().sendMessage(
                            "There was an issue retrieving hentai. Please report the following error to the bot owner:\n"
                                    + strBuilder.toString())
                            .queue(msg -> event.getMessage().delete().queue());
                }
                return;
            }

            final Set<BaseNSFWCommand> commands = COMMANDS.get(event.getGuild().getIdLong());
            commands.stream().filter(cmd -> cmd.name.equalsIgnoreCase(commandName)).findFirst()
                    .ifPresent(cmd -> execute(cmd, event.getMessage()));
        }
    }
}
