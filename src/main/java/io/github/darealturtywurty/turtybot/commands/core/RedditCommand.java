package io.github.darealturtywurty.turtybot.commands.core;

import java.time.Instant;
import java.util.UUID;

import javax.annotation.Nullable;

import com.squareup.moshi.JsonDataException;

import io.github.darealturtywurty.turtybot.util.BotUtils;
import io.github.darealturtywurty.turtybot.util.Constants;
import io.github.darealturtywurty.turtybot.util.SubredditMissingException;
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
import net.dv8tion.jda.api.entities.TextChannel;

public abstract class RedditCommand implements IRedditCommand {

	private static RedditClient redditClient;

	@Nullable
	private static RootCommentNode getPost(final IRedditCommand cmd, final TextChannel channel, final String subreddit) {
		try {
			return getValidSubreddit(cmd, BotUtils.getModLogChannel(channel.getGuild()), subreddit).randomSubmission();
		} catch (JsonDataException | ApiException e) {
			if (e.getLocalizedMessage().contains("404 (Not Found)"))
				return getValidSubreddit(cmd, BotUtils.getModLogChannel(channel.getGuild()), subreddit).randomSubmission();
			channel.sendMessage("There was an error with this post. Please try again!").queue();
		} catch (final NetworkException e) {
			channel.sendMessage("Hey! Slow down there, you are using me too much.").queue();
		}
		return null;
	}

	protected static String getRandomSubreddit(final IRedditCommand cmd) {
		return cmd.getSubreddits().get(Constants.RANDOM.nextInt(cmd.getSubreddits().size()));
	}

	private static SubredditReference getValidSubreddit(final IRedditCommand cmd, final TextChannel loggingChannel,
			final String subredditStr) {
		final var subredditReference = redditClient.subreddit(subredditStr);
		try {
			subredditReference.about();
			return subredditReference;
		} catch (final ApiException e) {
			loggingChannel.sendMessage(subredditStr).queue();
			System.out.println(subredditStr);

			return getValidSubreddit(cmd, loggingChannel, getRandomSubreddit(cmd));
		}
	}

	private static void initRedditClient() {
		final var oAuthCreds = Credentials.userless(BotUtils.getRedditID(), BotUtils.getRedditSecret(), UUID.randomUUID());
		final var userAgent = new UserAgent("bot", "io.github.darealturtywurty.turtybot", "1.0.0", "TurtyWurty");
		redditClient = OAuthHelper.automatic(new OkHttpNetworkAdapter(userAgent), oAuthCreds);
		redditClient.setLogHttp(false);
	}

	protected RedditCommand() {
		initRedditClient();
	}

	@Override
	public void handle(final CommandContext ctx) {
		if (getSubreddits().isEmpty())
			throw new SubredditMissingException("Command by name \"" + getName() + "\" is missing subreddits!");
		final String subreddit = getRandomSubreddit(this);
		final RootCommentNode post = getPost(this, ctx.getChannel(), subreddit);
		if (post == null) {
			handle(ctx);
			return;
		}

		final String url = post.getSubject().getUrl().isBlank() ? post.getSubject().getThumbnail()
				: post.getSubject().getUrl();
		final var embed = new EmbedBuilder();
		embed.setTitle(post.getSubject().getTitle());
		embed.setDescription(post.getSubject().getBody());
		embed.setColor(BotUtils.generateRandomColor());
		embed.setTimestamp(Instant.now());
		embed.setFooter(ctx.getAuthor().getName() + "#" + ctx.getAuthor().getDiscriminator(),
				ctx.getAuthor().getEffectiveAvatarUrl());
		if (!url.endsWith("mp4") && !url.endsWith("mov") && !url.endsWith("wmv") && !url.endsWith("avi")
				&& !url.endsWith("flv") && !url.endsWith("webm") && !url.endsWith("mkv")) {
			embed.setImage(url);
			ctx.getChannel().sendMessageEmbeds(embed.build()).queue();
		} else {
			ctx.getChannel().sendMessageEmbeds(embed.build()).queue(msg -> msg.editMessage(url).queue());
		}

		ctx.getMessage().delete().queue();
	}
}
