package io.github.darealturtywurty.turtybot.commands.moderation;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Nullable;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;
import io.github.darealturtywurty.turtybot.util.BotUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;

public class MuteCommand implements IGuildCommand {

	private static final Timer MUTE_TIMER = new Timer();
	protected static final Map<Member, List<Role>> MUTED_ROLE_MAP = new HashMap<>();

	public static void muteMember(final Guild guild, final Member muter, final Member member,
			@Nullable final Message toDelete, final String reason, final long timeMillis) {
		final var loggingChannel = BotUtils.getModLogChannel(guild);
		final var mutedRole = BotUtils.getMutedRole(guild);
		final var memberRoles = member.getRoles();

		if (toDelete != null) {
			toDelete.delete().queue();
		}

		for (final var role : memberRoles) {
			if (role.getName().equalsIgnoreCase("@everyone")) {
				memberRoles.remove(role);
			}
		}
		MUTED_ROLE_MAP.put(member, memberRoles);

		guild.modifyMemberRoles(member, mutedRole).queue();

		final var muteEmbed = new EmbedBuilder().setColor(Color.DARK_GRAY)
				.setTitle(member.getEffectiveName() + " has been muted.")
				.setDescription("**Reason**: " + reason + "\n**Muted By**: " + muter.getAsMention());
		loggingChannel.sendMessageEmbeds(muteEmbed.build()).queue();

		MUTE_TIMER.schedule(new TimerTask() {
			@Override
			public void run() {
				UnmuteCommand.unmuteMember(guild, member);
			}
		}, timeMillis);
	}

	public static String removeLastChar(final String str) {
		return removeLastChars(str, 1);
	}

	public static String removeLastChars(final String str, final int chars) {
		return str.substring(0, str.length() - chars);
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.MODERATION;
	}

	@Override
	public String getDescription() {
		return "Mutes a member.";
	}

	@Override
	public String getName() {
		return "mute";
	}

	@Override
	public void handle(final CommandContext ctx) {
		final var message = ctx.getMessage();
		if (ctx.getArgs().length < 1) {
			message.reply("You must specify the user that you want to mute!").mentionRepliedUser(false).queue();
			return;
		}

		final String user = ctx.getArgs()[0];

		Member toMute = null;

		try {
			toMute = message.getMentionedMembers().get(0);
		} catch (final IndexOutOfBoundsException ex) {
			ctx.getGuild().retrieveMemberById(user).queue();
			toMute = ctx.getGuild().getMemberById(user);
		}

		if (toMute == null) {
			message.reply("Could not find user: " + user).mentionRepliedUser(false).queue();
			return;
		}

		if (toMute.getIdLong() == ctx.getAuthor().getIdLong()) {
			message.reply("You cannot mute yourself!").mentionRepliedUser(false).queue();
			return;
		}

		if (!ctx.getMember().canInteract(toMute)) {
			message.reply("You cannot mute this user!").mentionRepliedUser(false).queue();
			return;
		}

		var multiplier = 1000;
		var amount = 60;
		if (ctx.getArgs().length > 1) {
			final String str = ctx.getArgs()[1];
			multiplier = parseTimeUnit(str);
			amount = Integer.parseInt(removeLastChar(str));
		}

		var reason = "Unspecified";

		if (ctx.getArgs().length > 2) {
			reason = String.join(" ", ctx.getArgs()).replace(user, "").replace(ctx.getArgs()[1], "").trim();
		}

		muteMember(ctx.getGuild(), ctx.getMember(), toMute, message, reason, amount * multiplier);
	}

	@Override
	public boolean isModeratorOnly() {
		return true;
	}

	private int parseTimeUnit(final String time) {
		var multiplier = 1000;
		for (final char c : time.toCharArray()) {
			switch (c) {
			case 's', 'S':
				multiplier = 1000;
				break;
			case 'm', 'M':
				multiplier = 60000;
				break;
			case 'h', 'H':
				multiplier = 3600000;
				break;
			case 'd', 'D':
				multiplier = 86400000;
				break;
			default:
				continue;
			}
		}
		return multiplier;
	}
}
