package io.github.darealturtywurty.turtybot.util.data;

import static io.github.darealturtywurty.turtybot.util.Constants.DATE_FORMAT;

import java.awt.Color;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.github.darealturtywurty.turtybot.commands.moderation.BanCommand;
import io.github.darealturtywurty.turtybot.commands.moderation.KickCommand;
import io.github.darealturtywurty.turtybot.commands.moderation.MuteCommand;
import io.github.darealturtywurty.turtybot.commands.moderation.UnbanCommand;
import io.github.darealturtywurty.turtybot.util.BotUtils;
import io.github.darealturtywurty.turtybot.util.BotUtils.CoreBotUtils;
import io.github.darealturtywurty.turtybot.util.Triple;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public class UserWarns {

	public final long userID;
	public Member member;
	public final Map<UUID, Triple<Long, Date, String>> warns;

	public UserWarns(final long userID, final Map<UUID, Triple<Long, Date, String>> warns) {
		this.userID = userID;
		this.warns = warns;
	}

	public UserWarns(final Member member) {
		this(member.getUser());
		this.member = member;
	}

	public UserWarns(final User user) {
		this(user.getIdLong(), new HashMap<>());
	}

	public void addWarn(final Guild guild, final Member warner, final String reason) {
		guild.retrieveMemberById(this.userID).queue();
		if (this.member == null) {
			this.member = guild.getMemberById(this.userID);
		}
		final var uuid = UUID.randomUUID();
		final var date = new Date();
		this.warns.put(uuid, Triple.create(warner.getIdLong(), date, reason));
		CoreBotUtils.writeGuildInfo();

		final var loggingChannel = BotUtils.getModLogChannel(guild);
		final var warnEmbed = new EmbedBuilder().setColor(Color.RED)
				.setTitle(this.member.getEffectiveName() + " has been warned!")
				.setDescription("**Warned By**: " + warner.getAsMention() + "\n**Reason**: " + reason + "\nUUID: " + uuid)
				.setTimestamp(date.toInstant());
		loggingChannel.sendMessageEmbeds(warnEmbed.build()).queue();

		final var dmWarnEmbed = new EmbedBuilder().setColor(Color.RED)
				.setTitle("You have been warned on server: " + guild.getName())
				.setDescription("**Warned By**: " + warner.getAsMention() + "\n**Reason**: " + reason + "\nUUID: " + uuid)
				.setTimestamp(date.toInstant());
		this.member.getUser().openPrivateChannel().queue(channel -> channel.sendMessageEmbeds(dmWarnEmbed.build()).queue());

		if (getNumberWarns() == BotUtils.getMuteThreshold(guild)) {
			MuteCommand.muteMember(guild, warner, this.member, null, "Reached Server Mute Threshold!", 9000000);
		} else if (getNumberWarns() == BotUtils.getKickThreshold(guild)) {
			KickCommand.kickMember(guild, warner, this.member, null, "Reached Server Kick Threshold!");
		} else if (getNumberWarns() == BotUtils.getBanThreshold(guild)) {
			BanCommand.banMember(guild, warner, this.member, null, "Reached Server Ban Threshold!");
		}
	}

	public EmbedBuilder getFormattedWarns(final Guild guild) {
		guild.retrieveMemberById(this.userID).queue();
		if (this.member == null) {
			this.member = guild.getMemberById(this.userID);
		}
		final var embed = new EmbedBuilder();
		embed.setTitle("Warnings for: " + this.member.getEffectiveName());
		embed.setDescription(this.member.getAsMention() + " has " + getNumberWarns() + " warns!");
		this.warns.forEach((uuid, warnInfo) -> {
			final var warner = guild.getMemberById(warnInfo.left);
			embed.addField("UUID: " + uuid.toString(), "Reason: " + warnInfo.right + "\nWarned By: " + warner.getAsMention()
					+ "\nDate-time: " + DATE_FORMAT.format(warnInfo.middle), false);
		});

		embed.setTimestamp(Instant.now());
		return embed;
	}

	public int getNumberWarns() {
		return this.warns.size();
	}

	public boolean removeWarn(final Guild guild, final Member warnRemover, final String strUUID) {
		if (this.member == null) {
			guild.retrieveMemberById(this.userID);
		}

		if (this.member != null && (warnRemover.getIdLong() == this.userID || !warnRemover.canInteract(this.member)))
			return false;

		final var uuid = UUID.fromString(strUUID);
		if (this.warns.containsKey(uuid)) {
			final Triple<Long, Date, String> warn = this.warns.get(uuid);
			this.warns.remove(uuid);

			final var loggingChannel = BotUtils.getModLogChannel(guild);
			final var removeWarnEmbed = new EmbedBuilder().setColor(Color.GREEN)
					.setTitle("User with ID: " + this.userID + " has had a warn removed!")
					.setDescription("**Removed By**: " + warnRemover.getAsMention() + "\n**Warn Info**:")
					.addField("Warned By (ID):", warn.left.toString(), false)
					.addField("Date:", DATE_FORMAT.format(warn.middle), false).addField("Reason:", warn.right, false)
					.setTimestamp(Instant.now());
			loggingChannel.sendMessageEmbeds(removeWarnEmbed.build()).queue();

			if (getNumberWarns() < BotUtils.getBanThreshold(guild)) {
				UnbanCommand.unbanUser(guild, warnRemover, this.userID, null, "Below guild maximum warn count!");
			}
			return true;
		}
		return false;
	}
}
