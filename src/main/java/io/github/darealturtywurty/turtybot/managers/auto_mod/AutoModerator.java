package io.github.darealturtywurty.turtybot.managers.auto_mod;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;

import io.github.darealturtywurty.turtybot.commands.moderation.MuteCommand;
import io.github.darealturtywurty.turtybot.util.BotUtils;
import io.github.darealturtywurty.turtybot.util.BotUtils.WarnUtils;
import io.github.darealturtywurty.turtybot.util.Constants;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class AutoModerator extends ListenerAdapter {

	private static class FileReadException extends RuntimeException {
		private static final long serialVersionUID = -6780037474145653723L;

		private final String message;

		public FileReadException(final String message) {
			this.message = message;
		}

		@Override
		public String getLocalizedMessage() {
			return this.message;
		}

		@Override
		public String getMessage() {
			return this.message;
		}
	}

	public static final Set<Long> USER_MUTE_MAP = new HashSet<>();
	private static final Set<String> BANNED_WORDS = new HashSet<>();
	private static final Map<Character, String> CHAR_REPLACEMENT_MAP = new HashMap<>();

	static {
		final String[] toIgnore = { "fuck", "shit", "bitch", "ass", "barely legal", "butt", "buttcheeks", "dick", "horny",
				"make me come", "nsfw", "nude", "pissing", "sexuality", "sexual", "sexually", "suck", "taste my",
				"tea bagging", "tied up", "topless", "twat", "xx" };
		final var url = "https://raw.githubusercontent.com/LDNOOBW/List-of-Dirty-Naughty-Obscene-and-Otherwise-Bad-Words/master/en";
		try (var inputStream = new URL(url).openStream()) {
			final String[] results = IOUtils.toString(inputStream, StandardCharsets.UTF_8).split("\n");
			for (final String word : results) {
				var ignored = false;
				for (final String ignore : toIgnore) {
					if (ignore.toLowerCase().trim().contains(word.toLowerCase().trim())) {
						ignored = true;
						break;
					}
				}

				if (!ignored) {
					BANNED_WORDS.add(word);
				}
			}
		} catch (final IOException e) {
			throw new FileReadException(e.getLocalizedMessage());
		}

		CHAR_REPLACEMENT_MAP.put('a', "@");
		CHAR_REPLACEMENT_MAP.put('b', "8");
		CHAR_REPLACEMENT_MAP.put('c', "(");
		CHAR_REPLACEMENT_MAP.put('d', "6");
		CHAR_REPLACEMENT_MAP.put('e', "3");
		CHAR_REPLACEMENT_MAP.put('e', "£");
		CHAR_REPLACEMENT_MAP.put('e', "₤");
		CHAR_REPLACEMENT_MAP.put('e', "€");
		CHAR_REPLACEMENT_MAP.put('f', "#");
		CHAR_REPLACEMENT_MAP.put('g', "9");
		CHAR_REPLACEMENT_MAP.put('h', "#");
		CHAR_REPLACEMENT_MAP.put('i', "1");
		CHAR_REPLACEMENT_MAP.put('i', "l");
		CHAR_REPLACEMENT_MAP.put('i', "!");
		CHAR_REPLACEMENT_MAP.put('k', "<");
		CHAR_REPLACEMENT_MAP.put('l', "1");
		CHAR_REPLACEMENT_MAP.put('l', "i");
		CHAR_REPLACEMENT_MAP.put('l', "!");
		CHAR_REPLACEMENT_MAP.put('o', "0");
		CHAR_REPLACEMENT_MAP.put('q', "9");
		CHAR_REPLACEMENT_MAP.put('s', "5");
		CHAR_REPLACEMENT_MAP.put('s', "$");
		CHAR_REPLACEMENT_MAP.put('t', "+");
		CHAR_REPLACEMENT_MAP.put('v', ">");
		CHAR_REPLACEMENT_MAP.put('v', "<");
		CHAR_REPLACEMENT_MAP.put('w', "uu");
		CHAR_REPLACEMENT_MAP.put('w', "2u");
		CHAR_REPLACEMENT_MAP.put('x', "%");
		CHAR_REPLACEMENT_MAP.put('y', "?");

		Set.copyOf(BANNED_WORDS).forEach(word -> CHAR_REPLACEMENT_MAP.forEach((character, replacement) -> BANNED_WORDS
				.add(word.replace(character.toString(), Matcher.quoteReplacement(replacement)))));
	}

	private static void amongusDetection(final GuildMessageReceivedEvent event) {
		final User user = event.getAuthor();
		final String shortenedText = event.getMessage().getContentRaw().toLowerCase().trim();
		if (shortenedText.contains("sus") || shortenedText.contains("amogus") || shortenedText.contains("amongus")
				|| shortenedText.contains("imposter")
						&& !shortenedText.startsWith(BotUtils.getPrefixFromGuild(event.getGuild())) && !user.isBot()
						&& !event.isWebhookMessage()) {
			try {
				user.openPrivateChannel().queue(channel -> channel.sendMessage(Constants.BEAN_DUMPY_URL).queue());
			} catch (final UnsupportedOperationException e) {
				event.getMessage().reply(Constants.BEAN_DUMPY_URL).queue();
			}
		}
	}

	private static void badWordFilter(final Message message) {
		final String text = normalizeMessage(message);
		for (final String word : BANNED_WORDS) {
			for (final String str : text.split(" ")) {
				if (str.equalsIgnoreCase(word)) {
					message.delete().queue(msg -> message.getAuthor().openPrivateChannel().queue(channel -> {
						channel.sendMessage("Please do not use the word: `" + word + "` in server: `"
								+ message.getGuild().getName() + "`. This word can only be used in an NSFW channel or "
								+ "any other channel that the server owner has allowed!\n"
								+ "In case you wanted your message back, here it is: ").queue();
						channel.sendMessage(message.getContentRaw()).queue();
					}));
				}
			}
		}
	}

	private static void discordDetection(final Message message) {
		if (message.getContentRaw().contains("discord.gg") && !BotUtils.isBotOwner(message.getAuthor())
				&& message.getAuthor().getIdLong() != message.getGuild().getSelfMember().getIdLong()) {
			message.delete().queue();
		}
	}

	private static String normalizeMessage(final Message message) {
		return Normalizer.normalize(message.getContentDisplay().toLowerCase(), Form.NFC);
	}

	private static void spamDetection(final GuildMessageReceivedEvent event) {
		event.getChannel().getHistory().retrievePast(20).queue(messages -> {
			final int spamCount = messages.stream().filter(
					msg -> event.getMessage().getTimeCreated().toEpochSecond() - msg.getTimeCreated().toEpochSecond() < 10)
					.collect(Collectors.toList()).size();
			if (spamCount >= 10) {
				if (!USER_MUTE_MAP.contains(event.getAuthor().getIdLong())) {
					MuteCommand.muteMember(event.getGuild(), event.getGuild().getSelfMember(), event.getMember(), null,
							"Spamming (sent " + spamCount + " messages in 5 seconds)!", 1800000L);
					USER_MUTE_MAP.add(event.getAuthor().getIdLong());
				} else {
					WarnUtils.getUserWarns(event.getGuild(), event.getAuthor()).addWarn(event.getGuild(),
							event.getGuild().getSelfMember(), "Spamming (sent " + spamCount + " messages in 5 seconds!)");
					USER_MUTE_MAP.remove(event.getAuthor().getIdLong());
				}
			}
		});
	}

	@Override
	public void onGuildMessageReceived(final GuildMessageReceivedEvent event) {
		super.onGuildMessageReceived(event);
		if (!event.getAuthor().isBot() && !event.isWebhookMessage() && !event.getMember().isOwner()) {
			// TODO: Check config to see if spam is/is not allowed in the channel.
			spamDetection(event);

			// TODO: Check config to look for any other channels that may allow bad words.
			if (!event.getChannel().isNSFW() && !event.getChannel().getName().equalsIgnoreCase("off-topic")) {
				badWordFilter(event.getMessage());
			}
		}
		discordDetection(event.getMessage());
	}

	@Override
	public void onGuildMessageUpdate(final GuildMessageUpdateEvent event) {
		super.onGuildMessageUpdate(event);
		// TODO: Check config to look for any other channels that may allow bad words.
		if (!event.getChannel().isNSFW() && !event.getChannel().getName().equalsIgnoreCase("off-topic")
				&& !event.getAuthor().isBot() && !event.getMember().isOwner()) {
			badWordFilter(event.getMessage());
		}
		discordDetection(event.getMessage());
	}
}
