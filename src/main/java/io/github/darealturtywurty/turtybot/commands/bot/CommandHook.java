package io.github.darealturtywurty.turtybot.commands.bot;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.io.IOUtils;
import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.GistFile;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.GistService;

import io.github.darealturtywurty.turtybot.commands.core.CommandManager;
import io.github.darealturtywurty.turtybot.util.BotUtils;
import io.github.darealturtywurty.turtybot.util.Constants;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandHook extends ListenerAdapter {
	private final CommandManager commandManager = new CommandManager();
	private final GitHubClient githubClient;
	private final String githubToken;

	public CommandHook() {
		this.githubToken = BotUtils.getGithubToken();
		this.githubClient = new GitHubClient().setOAuth2Token(this.githubToken);
	}

	@Override
	public void onGuildMessageReceived(final GuildMessageReceivedEvent event) {
		User user = event.getAuthor();

		if (event.getMessage().getContentRaw().contains("discord.gg") && !BotUtils.isBotOwner(user)) {
			event.getMessage().delete().queue();
			return;
		}

		// Handle Text Files
		if (!event.getMessage().getAttachments().isEmpty()) {
			Map<String, InputStream> validTextFiles = new HashMap<>();

			event.getMessage().getAttachments().forEach(attachment -> {
				try {
					InputStream stream = attachment.retrieveInputStream().get();
					// TODO: Find a better way to determine the list of valid file extensions.
					if (Constants.FILE_EXTENSIONS.contains(attachment.getFileExtension())) {
						validTextFiles.put(attachment.getFileName(), stream);
					}
				} catch (Exception e) {
					throw new IllegalArgumentException(e);
				}
			});

			if (!validTextFiles.isEmpty()) {
				validTextFiles.forEach((fileName, attachment) -> hostFileOnline(attachment, fileName, (text) -> event
						.getChannel()
						.sendMessage(event.getAuthor().getAsMention() + " Here, I put that file into a gist for you."
								+ "\nWe prefer you to not upload files on this server, so please upload it on a paste site!\n<"
								+ text + ">")
						.queue()));
				event.getMessage().delete().queue();
				return;
			}
		}

		// Handle showcases
		if (event.getChannel().getName().equalsIgnoreCase("showcases")) {
			boolean hasURL = false;
			for (String item : event.getMessage().getContentRaw().split("\\s+")) {
				if (Constants.URL_PATTERN.matcher(item).matches())
					hasURL = true;
			}

			if (hasURL) {
				event.getMessage().addReaction("⭐").queue();
				return;
			}

			if (event.getMessage().getAttachments().isEmpty()) {
				event.getMessage().delete().queue();
			} else if (!(event.getMessage().getAttachments().get(0).isImage()
					|| event.getMessage().getAttachments().get(0).isVideo())) {
				event.getMessage().delete().queue();
			} else {
				event.getMessage().addReaction("⭐").queue();
			}
			return;
		}

		if (user.isBot() || event.isWebhookMessage()
				|| !event.getMessage().getContentRaw().startsWith(BotUtils.getPrefixFromGuild(event.getGuild()))) {
			return;
		}

		this.commandManager.handle(event);
	}

	private void hostFileOnline(InputStream attachment, String fileName, Consumer<String> callback) {
		try {
			String text = IOUtils.toString(attachment, StandardCharsets.UTF_8.name());
			if (text.isBlank())
				return;
			Gist gist = new Gist();

			GistFile gistFile = new GistFile();
			gistFile.setContent(text);
			gist.setFiles(Collections.singletonMap(fileName, gistFile));
			gist.setDescription("Hello World!");

			GistService gistService = new GistService();
			gistService.getClient().setOAuth2Token(this.githubToken);
			gist = gistService.createGist(gist);

			callback.accept(gist.getHtmlUrl());
		} catch (IOException e) {
			callback.accept("Error: " + e.getMessage());
		}
	}
}
