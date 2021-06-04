package io.github.darealturtywurty.turtybot.commands.bot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

import io.github.darealturtywurty.turtybot.TurtyBot;
import io.github.darealturtywurty.turtybot.commands.core.CommandManager;
import io.github.darealturtywurty.turtybot.util.BotUtils;
import io.github.darealturtywurty.turtybot.util.Constants;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
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
		var user = event.getAuthor();

		if (event.getMessage().getContentRaw().contains("discord.gg") && !BotUtils.isBotOwner(user)) {
			event.getMessage().delete().queue();
			return;
		}

		// Sauce.
		// TODO: Delete this if you are hosting the bot yourself.
		if (event.getMessage().getContentRaw().contains(BotUtils.getPrefixFromGuild(event.getGuild()) + "sauce")) {
			sauce(event.getMessage());
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
				} catch (Exception ex) {
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
				|| !event.getMessage().getContentRaw().startsWith(BotUtils.getPrefixFromGuild(event.getGuild()))) {
			return;
		}

		this.commandManager.handle(event);
	}

	@Override
	public void onGuildReady(GuildReadyEvent event) {
		super.onGuildReady(event);
		event.getGuild().loadMembers();
		List<TextChannel> channels = event.getGuild().getTextChannels().stream()
				.filter(channel -> channel.getName().contains("general")).collect(Collectors.toList());
		if (!channels.isEmpty())
			channels.get(0)
					.sendMessage(
							"I am now online. For more information on my commands, visit this server's bot channel and use `"
									+ BotUtils.getPrefixFromGuild(event.getGuild()) + "help`.")
					.queue();
	}

	private void hostFileOnline(InputStream attachment, String fileName, Consumer<String> callback) {
		try {
			var text = IOUtils.toString(attachment, StandardCharsets.UTF_8.name());
			if (text.isBlank())
				return;
			var gist = new Gist();

			var gistFile = new GistFile();
			gistFile.setContent(text);
			gist.setFiles(Collections.singletonMap(fileName, gistFile));
			gist.setDescription("Hello World!");

			var gistService = new GistService();
			gistService.getClient().setOAuth2Token(this.githubToken);
			gist = gistService.createGist(gist);

			callback.accept(gist.getHtmlUrl());
		} catch (IOException ex) {
			callback.accept("Error: " + ex.getMessage());
		}
	}

	private void sauce(Message message) {
		message.getAuthor().openPrivateChannel().queue(channel -> {
			try {
				InputStream stream = TurtyBot.class.getResourceAsStream("/link.sauce");
				var reader = new BufferedReader(new InputStreamReader(stream));
				channel.sendMessage(reader.readLine()).queue();
			} catch (IOException | InsufficientPermissionException | IllegalArgumentException
					| UnsupportedOperationException e) {
				Constants.LOGGER.warning("There was an issue sending a user the sauce. " + e.getLocalizedMessage());
			}
		});
		message.delete().queue();
	}
}
