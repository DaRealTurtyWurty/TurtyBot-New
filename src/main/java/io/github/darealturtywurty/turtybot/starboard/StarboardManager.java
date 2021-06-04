package io.github.darealturtywurty.turtybot.starboard;

import java.time.Instant;

import io.github.darealturtywurty.turtybot.TurtyBot;
import io.github.darealturtywurty.turtybot.data.GuildInfo;
import io.github.darealturtywurty.turtybot.data.ShowcaseInfo;
import io.github.darealturtywurty.turtybot.util.BotUtils.StarboardUtils;
import io.github.darealturtywurty.turtybot.util.Constants;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class StarboardManager extends ListenerAdapter {

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		super.onGuildMessageReceived(event);
		var guild = event.getGuild();
		if (!StarboardUtils.isStarboardEnabled(guild)
				|| event.getChannel().getIdLong() != StarboardUtils.getShowcasesFromGuild(guild))
			return;

		var message = event.getMessage();
		var hasURL = false;
		for (String item : message.getContentRaw().split("\\s+")) {
			if (Constants.URL_PATTERN.matcher(item).matches())
				hasURL = true;
		}

		if (hasURL) {
			message.addReaction("⭐").queue();
			updateMessage(guild, event.getMessageIdLong(), StarboardUtils.includesBotStar(guild) ? 1 : 0, 0L);
			return;
		}

		if (message.getAttachments().isEmpty()) {
			message.delete().queue();
		} else if (!(message.getAttachments().get(0).isImage() || message.getAttachments().get(0).isVideo())) {
			message.delete().queue();
		} else {
			message.addReaction("⭐").queue();
			updateMessage(guild, event.getMessageIdLong(), StarboardUtils.includesBotStar(guild) ? 1 : 0, 0L);
		}
	}

	public static ShowcaseInfo getOrCreateShowcaseInfo(GuildInfo guildInfo, long messageID) {
		boolean exists = guildInfo.showcaseInfos.containsKey(messageID);
		ShowcaseInfo info = null;
		if (!exists) {
			info = new ShowcaseInfo(messageID);
			guildInfo.showcaseInfos.put(messageID, info);
		} else {
			info = guildInfo.showcaseInfos.get(messageID);
		}
		return info;
	}

	private static GuildInfo getOrCreateGuildInfo(Guild guild) {
		var bot = TurtyBot.getOrCreateInstance();
		return bot.guilds.get(guild) == null ? new GuildInfo(guild) : bot.guilds.get(guild);
	}

	public static void updateMessage(Guild guild, long messageID, int stars, long starboardMsgID) {
		var bot = TurtyBot.getOrCreateInstance();
		var guildInfo = getOrCreateGuildInfo(guild);
		var showcaseInfo = getOrCreateShowcaseInfo(guildInfo, messageID);
		showcaseInfo.setStars(stars);
		showcaseInfo.starboardMessageID = starboardMsgID;
		bot.guilds.put(guild, guildInfo);
		bot.writeGuildInfo();
	}

	public static int getStars(Guild guild, long messageID) {
		var guildInfo = getOrCreateGuildInfo(guild);
		return getOrCreateShowcaseInfo(guildInfo, messageID).getStars();
	}

	@Override
	public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
		super.onGuildMessageReactionAdd(event);
		var guild = event.getGuild();
		var showcases = StarboardUtils.getShowcasesChannel(guild);
		if (!StarboardUtils.isStarboardEnabled(guild) || event.getUser().isBot()
				|| event.getChannel().getIdLong() != showcases.getIdLong()
				|| !event.getReactionEmote().getEmoji().equalsIgnoreCase("⭐"))
			return;

		var messageID = event.getMessageIdLong();

		var guildInfo = getOrCreateGuildInfo(guild);
		var showcaseInfo = getOrCreateShowcaseInfo(guildInfo, messageID);
		int stars = getStars(guild, messageID) + 1;
		updateMessage(guild, messageID, stars, showcaseInfo.starboardMessageID);

		if (stars != StarboardUtils.getMinimumStars(guild))
			return;

		var starboard = StarboardUtils.getStarboardChannel(guild);
		showcases.retrieveMessageById(messageID).queue(msg -> {
			var starboardEmbed = new EmbedBuilder()
					.setAuthor(msg.getAuthor().getName(), null, msg.getAuthor().getEffectiveAvatarUrl())
					.setDescription(msg.getContentRaw())
					.setImage(msg.getAttachments().isEmpty() ? "" : msg.getAttachments().get(0).getUrl())
					.addField("**Source**", "[Jump](" + msg.getJumpUrl() + ")", false)
					.setFooter("Message ID: " + event.getMessageId()).setTimestamp(Instant.now())
					.setColor(msg.getMember().getColorRaw());
			starboard.sendMessage(getStarEmoji(guild, stars) + " " + stars + showcases.getAsMention())
					.queue(starboardMsg -> {
						updateMessage(guild, messageID, stars, starboardMsg.getIdLong());
						starboardMsg.editMessage(starboardEmbed.build()).queue();
					});
		});
	}

	public static String getStarEmoji(Guild guild, int stars) {
		if (stars < StarboardUtils.getStageStar(guild, 0))
			return "⭐";
		if (stars < StarboardUtils.getStageStar(guild, 1))
			return "🌟";
		if (stars < StarboardUtils.getStageStar(guild, 2))
			return "✨";
		if (stars < StarboardUtils.getStageStar(guild, 3))
			return "🌠";
		if (stars < StarboardUtils.getStageStar(guild, 4))
			return "🌃";
		if (stars < StarboardUtils.getStageStar(guild, 5))
			return "🤩";
		else
			return "💫";
	}

	@Override
	public void onGuildMessageReactionRemove(GuildMessageReactionRemoveEvent event) {
		super.onGuildMessageReactionRemove(event);
		var guild = event.getGuild();
		if (!StarboardUtils.isStarboardEnabled(guild) || event.getUser().isBot()
				|| event.getChannel().getIdLong() != StarboardUtils.getShowcasesFromGuild(guild)
				|| !event.getReactionEmote().getEmoji().equalsIgnoreCase("⭐"))
			return;

		var messageID = event.getMessageIdLong();
		int stars = getStars(guild, messageID) - 1;

		var guildInfo = getOrCreateGuildInfo(guild);
		var showcaseInfo = getOrCreateShowcaseInfo(guildInfo, messageID);
		updateMessage(guild, messageID, stars, showcaseInfo.starboardMessageID);

		var starboard = StarboardUtils.getStarboardChannel(guild);
		if (stars >= StarboardUtils.getMinimumStars(guild)) {
			starboard.retrieveMessageById(showcaseInfo.starboardMessageID)
					.queue(msg -> msg.editMessage(new EmbedBuilder(msg.getEmbeds().get(0)).build()).queue());
			return;
		}

		if (stars == StarboardUtils.getMinimumStars(guild) - 1) {
			starboard.retrieveMessageById(showcaseInfo.starboardMessageID).queue(msg -> msg.delete().queue());
		}
	}

	@Override
	public void onGuildMessageDelete(GuildMessageDeleteEvent event) {
		super.onGuildMessageDelete(event);
		var guild = event.getGuild();
		if (!StarboardUtils.isStarboardEnabled(guild))
			return;
		if (event.getChannel().getIdLong() != StarboardUtils.getShowcasesFromGuild(guild))
			return;

	}
}
