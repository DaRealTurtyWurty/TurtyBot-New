package io.github.darealturtywurty.turtybot.data;

import static io.github.darealturtywurty.turtybot.util.Constants.DATE_FORMAT;

import java.awt.Color;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.github.darealturtywurty.turtybot.TurtyBot;
import io.github.darealturtywurty.turtybot.commands.moderation.BanCommand;
import io.github.darealturtywurty.turtybot.commands.moderation.KickCommand;
import io.github.darealturtywurty.turtybot.commands.moderation.MuteCommand;
import io.github.darealturtywurty.turtybot.commands.moderation.UnbanCommand;
import io.github.darealturtywurty.turtybot.util.BotUtils;
import io.github.darealturtywurty.turtybot.util.Triple;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public class UserWarns {

	public final long userID;
	public Member member;
	public final Map<UUID, Triple<Long, Date, String>> warns;

	public UserWarns(long userID, Map<UUID, Triple<Long, Date, String>> warns) {
		this.userID = userID;
		this.warns = warns;
	}

	public UserWarns(User user) {
		this(user.getIdLong(), new HashMap<>());
	}

	public UserWarns(Member member) {
		this(member.getUser());
		this.member = member;
	}

	public int getNumberWarns() {
		return this.warns.size();
	}

	public boolean removeWarn(Guild guild, Member warnRemover, String strUUID) {
		if (this.member == null)
			guild.retrieveMemberById(this.userID);

		if (this.member != null && (warnRemover.getIdLong() == this.userID || !warnRemover.canInteract(this.member)))
			return false;

		var uuid = UUID.fromString(strUUID);
		if (this.warns.containsKey(uuid)) {
			Triple<Long, Date, String> warn = this.warns.get(uuid);
			this.warns.remove(uuid);

			var loggingChannel = BotUtils.getModLogChannel(guild);
			var removeWarnEmbed = new EmbedBuilder().setColor(Color.GREEN)
					.setTitle("User with ID: " + this.userID + " has had a warn removed!")
					.setDescription("**Removed By**: " + warnRemover.getAsMention() + "\n**Warn Info**:")
					.addField("Warned By (ID):", warn.left.toString(), false)
					.addField("Date:", DATE_FORMAT.format(warn.middle), false).addField("Reason:", warn.right, false)
					.setTimestamp(Instant.now());
			loggingChannel.sendMessage(removeWarnEmbed.build()).queue();

			if (this.getNumberWarns() < BotUtils.getBanThreshold(guild)) {
				UnbanCommand.unbanUser(guild, warnRemover, userID, null, "Below guild maximum warn count!");
			}
			return true;
		}
		return false;
	}

	public void addWarn(Guild guild, Member warner, String reason) {
		guild.retrieveMemberById(this.userID).queue();
		if (this.member == null)
			this.member = guild.getMemberById(this.userID);
		var uuid = UUID.randomUUID();
		var date = new Date();
		this.warns.put(uuid, Triple.create(warner.getIdLong(), date, reason));
		TurtyBot.getOrCreateInstance().writeGuildInfo();

		var loggingChannel = BotUtils.getModLogChannel(guild);
		var warnEmbed = new EmbedBuilder().setColor(Color.RED).setTitle(member.getEffectiveName() + " has been warned!")
				.setDescription("**Warned By**: " + warner.getAsMention() + "\n**Reason**: " + reason + "\nUUID: " + uuid)
				.setTimestamp(date.toInstant());
		loggingChannel.sendMessage(warnEmbed.build()).queue();

		var dmWarnEmbed = new EmbedBuilder().setColor(Color.RED)
				.setTitle("You have been warned on server: " + guild.getName())
				.setDescription("**Warned By**: " + warner.getAsMention() + "\n**Reason**: " + reason + "\nUUID: " + uuid)
				.setTimestamp(date.toInstant());
		member.getUser().openPrivateChannel().queue(channel -> channel.sendMessage(dmWarnEmbed.build()).queue());

		if (this.getNumberWarns() == BotUtils.getMuteThreshold(guild))
			MuteCommand.muteMember(guild, warner, member, null, "Reached Server Mute Threshold!", 9000000);

		else if (this.getNumberWarns() == BotUtils.getKickThreshold(guild))
			KickCommand.kickMember(guild, warner, member, null, "Reached Server Kick Threshold!");

		else if (this.getNumberWarns() == BotUtils.getBanThreshold(guild))
			BanCommand.banMember(guild, warner, member, null, "Reached Server Ban Threshold!");
	}

	public EmbedBuilder getFormattedWarns(Guild guild) {
		guild.retrieveMemberById(this.userID).queue();
		if (this.member == null)
			this.member = guild.getMemberById(this.userID);
		var embed = new EmbedBuilder();
		embed.setTitle("Warnings for: " + member.getEffectiveName());
		embed.setDescription(member.getAsMention() + " has " + this.getNumberWarns() + " warns!");
		this.warns.forEach((uuid, warnInfo) -> {
			var warner = guild.getMemberById(warnInfo.left);
			embed.addField("UUID: " + uuid.toString(), "Reason: " + warnInfo.right + "\nWarned By: " + warner.getAsMention()
					+ "\nDate-time: " + DATE_FORMAT.format(warnInfo.middle), false);
		});

		embed.setTimestamp(Instant.now());
		return embed;
	}
}
