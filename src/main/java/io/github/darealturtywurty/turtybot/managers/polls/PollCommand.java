package io.github.darealturtywurty.turtybot.managers.polls;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.vdurmont.emoji.EmojiParser;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;
import io.github.darealturtywurty.turtybot.util.BotUtils;
import io.github.darealturtywurty.turtybot.util.Constants;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class PollCommand extends ListenerAdapter implements IGuildCommand {

	private static final Set<Poll> POLLS = new HashSet<>();

	public static List<String> getEmojis(final Message message) {
		// Collect emojis
		final String content = message.getContentRaw();
		final List<String> emojis = EmojiParser.extractEmojis(content);
		final List<String> customEmoji = message.getEmotes().stream().map(emote -> emote.getName() + ":" + emote.getId())
				.collect(Collectors.toList());

		// Create merged list
		final List<String> merged = new ArrayList<>(emojis);
		merged.addAll(customEmoji);

		// Sort based on index in message to preserve order
		merged.sort(Comparator.comparingInt(content::indexOf));
		return merged;
	}

	public PollCommand() {
		// TODO: Read polls from MongoDB into polls
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.UTILITY;
	}

	@Override
	public String getDescription() {
		return "Creates a poll from the message.";
	}

	@Override
	public String getName() {
		return "poll";
	}

	@Override
	public void handle(final CommandContext ctx) {
		final var message = ctx.getMessage();
		final List<String> emojis = getEmojis(message);
		if (emojis.isEmpty()) {
			message.reply("You must supply at least one valid emoji!").mentionRepliedUser(false).queue(msg -> {
				msg.delete().queueAfter(15, TimeUnit.SECONDS);
			});
			return;
		}

		final var embed = new EmbedBuilder();
		embed.setTitle(ctx.getAuthor().getName() + " asks:");
		embed.setDescription(message.getContentRaw().replace(BotUtils.getPrefixFromGuild(ctx.getGuild()) + "poll", ""));
		embed.setColor(ctx.getMember().getColorRaw());
		embed.setTimestamp(Instant.now());
		embed.setFooter(ctx.getAuthor().getName() + "#" + ctx.getAuthor().getDiscriminator(),
				ctx.getAuthor().getEffectiveAvatarUrl());
		ctx.getChannel().sendMessageEmbeds(embed.build()).mentionRepliedUser(false).queue(msg -> {
			var succeeded = false;
			for (final var emoji : emojis) {
				try {
					msg.addReaction(emoji).queue();
					succeeded = true;
				} catch (final Exception e) {
					Constants.LOGGER.warning("Failed to react with emoji (" + emoji + ").");
				}
			}

			if (!succeeded) {
				message.reply("You must supply at least one valid emoji!").mentionRepliedUser(false).queue(msg1 -> {
					msg.delete().queueAfter(15, TimeUnit.SECONDS);
					msg1.delete().queueAfter(15, TimeUnit.SECONDS);
				});
			}
		});

		message.delete().queue();
	}

	@Override
	public void onGuildMessageReactionAdd(final GuildMessageReactionAddEvent event) {
		super.onGuildMessageReactionAdd(event);
		if (event.getUser().isBot())
			return;

		final Optional<Poll> oPoll = POLLS
				.stream().filter(p -> p.guildId == event.getGuild().getIdLong()
						&& p.channelId == event.getChannel().getIdLong() && p.messageId == event.getMessageIdLong())
				.findFirst();
		if (oPoll.isPresent()) {
			final var poll = oPoll.get();
			final Map<String, Long> reactions = poll.reactions;
			if (reactions.containsKey(event.getReactionEmote().getId())
					&& reactions.get(event.getReactionEmote().getId()) == event.getUserIdLong()) {
				event.getReaction().removeReaction().queue();
			} else {
				poll.reactions.put(event.getReactionEmote().getId(), event.getUserIdLong());
			}
		}
	}

	@Override
	public void onGuildMessageReactionRemove(final GuildMessageReactionRemoveEvent event) {
		super.onGuildMessageReactionRemove(event);
		final Optional<Poll> oPoll = POLLS
				.stream().filter(p -> p.guildId == event.getGuild().getIdLong()
						&& p.channelId == event.getChannel().getIdLong() && p.messageId == event.getMessageIdLong())
				.findFirst();
		if (oPoll.isPresent()) {
			final var poll = oPoll.get();
			final Map<String, Long> reactions = poll.reactions;
			if (event.getUser().isBot()) {
				final AtomicReference<Message> message = new AtomicReference<>();
				try {
					event.getChannel().retrieveMessageById(event.getMessageIdLong()).queue(message::set);
					if (message.get() != null) {
						if (reactions.containsKey(event.getReactionEmote().getId())) {
							message.get().delete().queue();
							POLLS.remove(poll);
						}
					} else {
						POLLS.remove(poll);
					}
				} catch (final Exception e) {
					POLLS.remove(poll);
				}
				return;
			}

			if (reactions.containsKey(event.getReactionEmote().getId())
					&& reactions.get(event.getReactionEmote().getId()) == event.getUserIdLong()) {
				poll.reactions.remove(event.getReactionEmote().getId(), event.getUserIdLong());
			}
		}
	}
}
