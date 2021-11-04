package io.github.darealturtywurty.turtybot.managers.auto_mod;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import org.apache.commons.io.IOUtils;

import com.google.gson.JsonArray;

import io.github.darealturtywurty.turtybot.commands.moderation.MuteCommand;
import io.github.darealturtywurty.turtybot.util.Constants;
import io.github.darealturtywurty.turtybot.util.core.BotUtils;
import io.github.darealturtywurty.turtybot.util.core.CoreBotUtils;
import io.github.darealturtywurty.turtybot.util.core.WarnUtils;
import io.github.darealturtywurty.turtybot.util.data.GuildInfo;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class AutoModerator extends ListenerAdapter {

    private static final Set<String> BANNED_WORDS = new HashSet<>();
    private static final Map<Character, String[]> CHAR_REPLACEMENT_MAP = new HashMap<>();
    private static final Set<String> SCAM_LINKS = new HashSet<>();

    static {
        setupBannedWords();
        setupScamLinks();
    }

    public static void scamDetection(final Message message) {
        final String content = message.getContentRaw().toLowerCase();
        for (final String link : SCAM_LINKS) {
            if (content.contains(link)) {
                message.delete().queue(consume -> message.getAuthor().openPrivateChannel()
                        .queue(channel -> channel.sendMessage(
                                "It appears your account has been comprimised, as you have sent scam links in Guild: `"
                                        + message.getGuild().getName()
                                        + "`. It is highly recommended that you change your password, "
                                        + "so that your discord token will reset and access from the hijakers will be lost. "
                                        + "In the future, please be more careful with the links you are clicking on!")
                                .queue()));
                break;
            }
        }
    }

    private static void amongusDetection(final GuildMessageReceivedEvent event) {
        final User user = event.getAuthor();
        final String shortenedText = event.getMessage().getContentRaw().toLowerCase().trim();
        if (shortenedText.contains("sus") || shortenedText.contains("amogus")
                || shortenedText.contains("amongus")
                || shortenedText.contains("imposter")
                        && !shortenedText.startsWith(BotUtils.getPrefixFromGuild(event.getGuild()))
                        && !user.isBot() && !event.isWebhookMessage()) {
            try {
                user.openPrivateChannel()
                        .queue(channel -> channel.sendMessage(Constants.BEAN_DUMPY_URL).queue());
            } catch (final UnsupportedOperationException e) {
                event.getMessage().reply(Constants.BEAN_DUMPY_URL).queue();
            }
        }
    }

    private static void badWordFilter(final Message message) {
        final String text = normalizeMessage(message);
        for (final String word : BANNED_WORDS) {
            for (final String str : text.split(" ")) {
                if (str.toLowerCase().equals(word.toLowerCase().replaceAll("[^a-zA-Z0-9]", " "))) {
                    message.delete().queue(msg -> message.getAuthor().openPrivateChannel().queue(channel -> {
                        channel.sendMessage("Please do not use the word: `" + word + "` in server: `"
                                + message.getGuild().getName()
                                + "`. This word can only be used in an NSFW channel or "
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

    private static void setupBannedWords() {
        final String[] toIgnore = { "fuck", "fucking", "shit", "bitch", "ass", "barely legal", "butt",
                "buttcheeks", "dick", "horny", "make me come", "nsfw", "nude", "pissing", "sexuality",
                "sexual", "sexually", "suck", "sucks", "taste my", "tea bagging", "tied up", "topless",
                "twat", "xx", "shitty", "boobs", "apeshit", "baby batter", "baby juice", "beaner", "beaners",
                "rape", "dingleberry", "eat my ass", "fuckin", "genitals", "girl on", "god damn",
                "how to kill", "how to murder", "huge fat", "humping", "kinky", "motherfucker", "nsfw images",
                "nude", "nudity", "pissing", "porn", "raping", "rapist", "sex", "sexy", "swastika",
                "tongue in a", "boob", "boobies" };
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

        CHAR_REPLACEMENT_MAP.put('a', new String[] { "@" });
        CHAR_REPLACEMENT_MAP.put('b', new String[] { "8" });
        CHAR_REPLACEMENT_MAP.put('c', new String[] { "(" });
        CHAR_REPLACEMENT_MAP.put('d', new String[] { "6" });
        CHAR_REPLACEMENT_MAP.put('e', new String[] { "3", "£", "₤", "€" });
        CHAR_REPLACEMENT_MAP.put('f', new String[] { "#" });
        CHAR_REPLACEMENT_MAP.put('g', new String[] { "9" });
        CHAR_REPLACEMENT_MAP.put('h', new String[] { "#" });
        CHAR_REPLACEMENT_MAP.put('i', new String[] { "1", "l", "!" });
        CHAR_REPLACEMENT_MAP.put('k', new String[] { "<" });
        CHAR_REPLACEMENT_MAP.put('l', new String[] { "1", "i", "!" });
        CHAR_REPLACEMENT_MAP.put('o', new String[] { "0" });
        CHAR_REPLACEMENT_MAP.put('q', new String[] { "9" });
        CHAR_REPLACEMENT_MAP.put('s', new String[] { "5", "$" });
        CHAR_REPLACEMENT_MAP.put('t', new String[] { "+" });
        CHAR_REPLACEMENT_MAP.put('u', new String[] { "v" });
        CHAR_REPLACEMENT_MAP.put('v', new String[] { ">", "<", "u" });
        CHAR_REPLACEMENT_MAP.put('w', new String[] { "uu", "2u" });
        CHAR_REPLACEMENT_MAP.put('x', new String[] { "%" });
        CHAR_REPLACEMENT_MAP.put('y', new String[] { "?" });

        Set.copyOf(BANNED_WORDS).forEach(word -> CHAR_REPLACEMENT_MAP.forEach(
                (character, replacements) -> List.of(replacements).forEach(replacement -> BANNED_WORDS
                        .add(word.replace(character.toString(), Matcher.quoteReplacement(replacement))))));
    }

    private static void setupScamLinks() {
        final var url = "https://phish.sinking.yachts/v2/all";
        try (var inputStream = new URL(url).openStream()) {
            final String result = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            Constants.GSON.fromJson(result, JsonArray.class)
                    .forEach(link -> SCAM_LINKS.add(link.getAsString()));
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private static void spamDetection(final GuildMessageReceivedEvent event) {
        // TODO: Fix
        if (BotUtils.notTestServer(event.getGuild()))
            return;
        final GuildInfo info = CoreBotUtils.GUILDS.get(event.getGuild().getIdLong());
        event.getChannel().getHistory().retrievePast(20).queue(messages -> {
            final int spamCount = messages.stream()
                    .filter(msg -> event.getMessage().getTimeCreated().toEpochSecond()
                            - msg.getTimeCreated().toEpochSecond() < 10)
                    .toList().size();
            if (spamCount >= 10) {
                if (!info.userMutes.contains(event.getAuthor().getIdLong())) {
                    MuteCommand.muteMember(event.getGuild(), event.getGuild().getSelfMember(),
                            event.getMember(), null,
                            "Spamming (sent " + spamCount + " messages in 5 seconds)!", 1800000L);
                    info.userMutes.add(event.getAuthor().getIdLong());
                } else {
                    WarnUtils.getUserWarns(event.getGuild(), event.getAuthor()).addWarn(event.getGuild(),
                            event.getGuild().getSelfMember(),
                            "Spamming (sent " + spamCount + " messages in 5 seconds!)");
                    info.userMutes.remove(event.getAuthor().getIdLong());
                }
            }
        });
    }

    @Override
    public void onGuildMessageReceived(final GuildMessageReceivedEvent event) {
        super.onGuildMessageReceived(event);
        if (!event.getAuthor().isBot() && !event.isWebhookMessage() && !event.getMember().isOwner()) {
            // TODO: Check config to see if spam is/is not allowed in the channel.
            // spamDetection(event);

            // TODO: Check config to look for any other channels that may allow bad words.
            if (!event.getChannel().isNSFW() && !event.getChannel().getName().equalsIgnoreCase("off-topic")) {
                badWordFilter(event.getMessage());
            }
        }

        discordDetection(event.getMessage());
        scamDetection(event.getMessage());

        if (!event.getAuthor().isBot()) {
            amongusDetection(event);
        }
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
        scamDetection(event.getMessage());
    }

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
}
