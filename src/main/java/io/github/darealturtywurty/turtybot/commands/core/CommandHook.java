package io.github.darealturtywurty.turtybot.commands.core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.GistFile;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.GistService;

import io.github.darealturtywurty.turtybot.util.BotUtils;
import io.github.darealturtywurty.turtybot.util.BotUtils.CoreBotUtils;
import io.github.darealturtywurty.turtybot.util.Constants;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandHook extends ListenerAdapter {
	private final CommandManager commandManager = new CommandManager();
	private final GitHubClient githubClient = new GitHubClient().setOAuth2Token(BotUtils.getGithubToken());

	private void hostFileOnline(final InputStream attachment, final String fileName, final Consumer<String> callback) {
		try {
			final var text = IOUtils.toString(attachment, StandardCharsets.UTF_8.name());
			if (text.isBlank())
				return;
			var gist = new Gist();

			final var gistFile = new GistFile();
			gistFile.setContent(text);
			gist.setFiles(Collections.singletonMap(fileName, gistFile));
			gist.setDescription("Hello World!");

			final var gistService = new GistService();
			gistService.getClient().setOAuth2Token(BotUtils.getGithubToken());
			gist = gistService.createGist(gist);

			callback.accept(gist.getHtmlUrl());
		} catch (final IOException ex) {
			callback.accept("Error: " + ex.getMessage());
		}
	}

	@Override
	public void onGuildMessageReceived(final GuildMessageReceivedEvent event) {
		final var user = event.getAuthor();

		final String shortenedText = event.getMessage().getContentRaw().toLowerCase().trim();
		if (shortenedText.contains("sus") || shortenedText.contains("amogus") || shortenedText.contains("amongus")
				|| shortenedText.contains("imposter")) {
			try {
				user.openPrivateChannel().queue(channel -> channel.sendMessage(Constants.BEAN_DUMPY_URL).queue());
			} catch (final UnsupportedOperationException e) {
				event.getMessage().reply(Constants.BEAN_DUMPY_URL).queue();
			}
		}

		if (event.getMessage().getContentRaw().contains("discord.gg") && !BotUtils.isBotOwner(user)) {
			event.getMessage().delete().queue();
			return;
		}

		// Handle Text Files
		if (!event.getMessage().getAttachments().isEmpty()) {
			final Map<String, InputStream> validTextFiles = new HashMap<>();

			event.getMessage().getAttachments().forEach(attachment -> {
				try {
					final InputStream stream = attachment.retrieveInputStream().get();
					// TODO: Find a better way to determine the list of valid file extensions.
					if (Constants.FILE_EXTENSIONS.contains(attachment.getFileExtension())) {
						validTextFiles.put(attachment.getFileName(), stream);
					}
					stream.close();
				} catch (final Exception ex) {
					throw new IllegalArgumentException(ex);
				}
			});

			if (!validTextFiles.isEmpty()) {
				validTextFiles.forEach((fileName, attachment) -> hostFileOnline(attachment, fileName, text -> event
						.getChannel()
						.sendMessage(event.getAuthor().getAsMention() + " Here, I put that file into a gist for you."
								+ "\nWe prefer you to not upload files on this server, so please upload it on a paste site!\n<"
								+ text + ">")
						.queue()));
				event.getMessage().delete().queue();
				return;
			}
		}

		if (user.isBot() || event.isWebhookMessage()
				|| !event.getMessage().getContentRaw().startsWith(BotUtils.getPrefixFromGuild(event.getGuild())))
			return;

		this.commandManager.handle(event);
	}

	@Override
	public void onReady(final ReadyEvent event) {
		super.onReady(event);
		CoreBotUtils.readGuildInfo(event.getJDA());
		CoreBotUtils.writeGuildInfo();
		Constants.LEVELLING_MANAGER.startTimer(event.getJDA());
		event.getJDA().getGuilds().forEach(guild -> {
			guild.loadMembers();
			final List<TextChannel> channels = guild.getTextChannels().stream()
					.filter(channel -> channel.getName().contains("general")).collect(Collectors.toList());
			if (!channels.isEmpty() && guild.getIdLong() != 819294753732296776L) {
				channels.get(0).sendMessage(
						"I am now online. For more information on my commands, visit this server's bot channel and use `"
								+ BotUtils.getPrefixFromGuild(guild) + "help`.")
						.queue();
			}
		});
	}
}
