package io.github.darealturtywurty.turtybot.managers.starboard;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

import io.github.darealturtywurty.turtybot.util.BotUtils.CoreBotUtils;
import io.github.darealturtywurty.turtybot.util.BotUtils.StarboardUtils;
import io.github.darealturtywurty.turtybot.util.Constants;
import io.github.darealturtywurty.turtybot.util.data.GuildInfo;
import io.github.darealturtywurty.turtybot.util.data.ShowcaseInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class StarboardManager extends ListenerAdapter {

	public static ShowcaseInfo getOrCreateShowcaseInfo(final GuildInfo guildInfo, final long messageID) {
		final boolean exists = guildInfo.showcaseInfos.containsKey(messageID);
		ShowcaseInfo info = null;
		if (!exists) {
			info = new ShowcaseInfo(messageID);
			guildInfo.showcaseInfos.put(messageID, info);
		} else {
			info = guildInfo.showcaseInfos.get(messageID);
		}
		return info;
	}

	public static long getShowcaseUserID(final Guild guild, final ShowcaseInfo showcaseInfo) {
		final var guildInfo = getOrCreateGuildInfo(guild);
		final var showcases = StarboardUtils.getShowcasesChannel(guild);
		final var starboard = StarboardUtils.getStarboardChannel(guild);
		final var authorID = new AtomicLong(0L);

		showcases.retrieveMessageById(showcaseInfo.originalMessageID).queue(showcaseMsg -> {
			if (showcaseMsg == null) {
				starboard.retrieveMessageById(showcaseInfo.starboardMessageID).queue(starboardMsg -> {
					if (starboardMsg == null) {
						guildInfo.showcaseInfos.remove(showcaseInfo.originalMessageID);
						return;
					}

					authorID.set(Long.parseLong(starboardMsg.getEmbeds().get(0).getAuthor().getProxyIconUrl().split("/")[4]
							.toLowerCase().trim()));
				});
				return;
			}

			authorID.set(showcaseMsg.getAuthor().getIdLong());
		});

		return authorID.get();
	}

	public static String getStarEmoji(final Guild guild, final int stars) {
		if (stars < StarboardUtils.getStageStar(guild, 0))
			return "⭐";
		if (stars < StarboardUtils.getStageStar(guild, 1))
			return "🌟";
		if (stars < StarboardUtils.getStageStar(guild, 2))
			return "✨";
		if (stars < StarboardUtils.getStageStar(guild, 3))
			return "🌃";
		if (stars < StarboardUtils.getStageStar(guild, 4))
			return "🌠";
		if (stars < StarboardUtils.getStageStar(guild, 5))
			return "🤩";
		return "💫";
	}

	public static int getStars(final Guild guild, final long messageID) {
		final var guildInfo = getOrCreateGuildInfo(guild);
		return getOrCreateShowcaseInfo(guildInfo, messageID).getStars();
	}

	public static void updateMessage(final Guild guild, final long messageID, final int stars, final long starboardMsgID) {
		final var guildInfo = getOrCreateGuildInfo(guild);
		final var showcaseInfo = getOrCreateShowcaseInfo(guildInfo, messageID);
		showcaseInfo.setStars(stars);
		showcaseInfo.starboardMessageID = starboardMsgID;
		CoreBotUtils.GUILDS.put(guild, guildInfo);
		CoreBotUtils.writeGuildInfo();
	}

	private static GuildInfo getOrCreateGuildInfo(final Guild guild) {
		return CoreBotUtils.GUILDS.get(guild) == null ? new GuildInfo(guild) : CoreBotUtils.GUILDS.get(guild);
	}

	@Override
	public void onGuildMessageDelete(final GuildMessageDeleteEvent event) {
		super.onGuildMessageDelete(event);
		final var guild = event.getGuild();
		if (!StarboardUtils.isStarboardEnabled(guild))
			return;
		if (event.getChannel().getIdLong() != StarboardUtils.getShowcasesFromGuild(guild))
			return;

		final var guildInfo = getOrCreateGuildInfo(event.getGuild());
		final boolean alreadyExists = guildInfo.showcaseInfos.containsKey(event.getMessageIdLong());
		final var showcaseInfo = getOrCreateShowcaseInfo(guildInfo, event.getMessageIdLong());
		if (alreadyExists && showcaseInfo.getStars() < StarboardUtils.getStageStar(event.getGuild(), 1)) {
			guildInfo.showcaseInfos.remove(event.getMessageIdLong());
		} else if (showcaseInfo.getStars() >= StarboardUtils.getStageStar(event.getGuild(), 1)) {
			guildInfo.showcaseInfos.remove(event.getMessageIdLong());
			guildInfo.userStarStats.get(getShowcaseUserID(guild, showcaseInfo)).otherStars += showcaseInfo.getStars();
		}
	}

	@Override
	public void onGuildMessageReactionAdd(final GuildMessageReactionAddEvent event) {
		super.onGuildMessageReactionAdd(event);
		final var guild = event.getGuild();
		final var showcases = StarboardUtils.getShowcasesChannel(guild);
		if (!StarboardUtils.isStarboardEnabled(guild) || event.getUser().isBot()
				|| event.getChannel().getIdLong() != showcases.getIdLong()
				|| !event.getReactionEmote().getEmoji().equalsIgnoreCase("⭐"))
			return;

		final var messageID = event.getMessageIdLong();

		final var guildInfo = getOrCreateGuildInfo(guild);
		final var showcaseInfo = getOrCreateShowcaseInfo(guildInfo, messageID);
		final int stars = getStars(guild, messageID) + 1;
		updateMessage(guild, messageID, stars, showcaseInfo.starboardMessageID);

		if (stars != StarboardUtils.getMinimumStars(guild))
			return;

		final var starboard = StarboardUtils.getStarboardChannel(guild);
		showcases.retrieveMessageById(messageID).queue(msg -> {
			final var starboardEmbed = new EmbedBuilder()
					.setAuthor(msg.getAuthor().getName(), null, msg.getAuthor().getEffectiveAvatarUrl())
					.setDescription(msg.getContentRaw())
					.setImage(msg.getAttachments().isEmpty() ? "" : msg.getAttachments().get(0).getUrl())
					.addField("**Source**", "[Jump](" + msg.getJumpUrl() + ")", false)
					.setFooter("Message ID: " + event.getMessageId()).setTimestamp(Instant.now())
					.setColor(msg.getMember().getColorRaw());
			starboard.sendMessage(getStarEmoji(guild, stars) + " " + stars + " " + showcases.getAsMention())
					.queue(starboardMsg -> {
						updateMessage(guild, messageID, stars, starboardMsg.getIdLong());
						starboardMsg.editMessageEmbeds(starboardEmbed.build()).queue();
					});
		});
	}

	@Override
	public void onGuildMessageReactionRemove(final GuildMessageReactionRemoveEvent event) {
		super.onGuildMessageReactionRemove(event);
		final var guild = event.getGuild();
		if (!StarboardUtils.isStarboardEnabled(guild) || event.getUser().isBot()
				|| event.getChannel().getIdLong() != StarboardUtils.getShowcasesFromGuild(guild)
				|| !event.getReactionEmote().getEmoji().equalsIgnoreCase("⭐"))
			return;

		final var messageID = event.getMessageIdLong();
		final int stars = getStars(guild, messageID) - 1;

		final var guildInfo = getOrCreateGuildInfo(guild);
		final var showcaseInfo = getOrCreateShowcaseInfo(guildInfo, messageID);
		updateMessage(guild, messageID, stars, showcaseInfo.starboardMessageID);

		final var showcases = StarboardUtils.getShowcasesChannel(guild);
		final var starboard = StarboardUtils.getStarboardChannel(guild);
		if (stars >= StarboardUtils.getMinimumStars(guild)) {
			starboard.retrieveMessageById(showcaseInfo.starboardMessageID).queue(msg -> msg
					.editMessage(getStarEmoji(guild, stars) + " " + stars + " " + showcases.getAsMention()).queue());
			return;
		}

		if (stars == StarboardUtils.getMinimumStars(guild) - 1) {
			starboard.retrieveMessageById(showcaseInfo.starboardMessageID).queue(msg -> msg.delete().queue());
		}
	}

	@Override
	public void onGuildMessageReceived(final GuildMessageReceivedEvent event) {
		super.onGuildMessageReceived(event);
		final var guild = event.getGuild();
		if (!StarboardUtils.isStarboardEnabled(guild)
				|| event.getChannel().getIdLong() != StarboardUtils.getShowcasesFromGuild(guild))
			return;

		final var message = event.getMessage();
		var hasURL = false;
		for (final String item : message.getContentRaw().split("\\s+")) {
			if (Constants.URL_PATTERN.matcher(item).matches()) {
				hasURL = true;
			}
		}

		if (hasURL) {
			message.addReaction("⭐").queue();
			updateMessage(guild, event.getMessageIdLong(), StarboardUtils.includesBotStar(guild) ? 1 : 0, 0L);
			return;
		}

		if (message.getAttachments().isEmpty()) {
			message.delete().queue();
		} else if (message.getAttachments().get(0).isVideo()) {
			message.delete().queue();
		} else {
			message.addReaction("⭐").queue();
			updateMessage(guild, event.getMessageIdLong(), StarboardUtils.includesBotStar(guild) ? 1 : 0, 0L);
		}
	}
}
