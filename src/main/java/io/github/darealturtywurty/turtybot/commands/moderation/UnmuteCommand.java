package io.github.darealturtywurty.turtybot.commands.moderation;

import java.util.List;

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

public class UnmuteCommand implements IGuildCommand {

	public static boolean unmuteMember(final Guild guild, final Member toUnmute) {
		if (MuteCommand.MUTED_ROLE_MAP.containsKey(toUnmute)) {
			final var loggingChannel = BotUtils.getModLogChannel(guild);

			// Remove muted role and add back original roles
			final List<Role> memberRoles = MuteCommand.MUTED_ROLE_MAP.get(toUnmute);
			guild.removeRoleFromMember(toUnmute, BotUtils.getMutedRole(guild))
					.queue(rem -> memberRoles.forEach(role -> guild.addRoleToMember(toUnmute, role).queue()));

			// Log the unmute
			final var unmuteEmbed = new EmbedBuilder().setColor(toUnmute.getColorRaw())
					.setTitle(toUnmute.getEffectiveName() + " has been automatically unmuted.");
			loggingChannel.sendMessageEmbeds(unmuteEmbed.build()).queue();

			// Remove member from role cache
			MuteCommand.MUTED_ROLE_MAP.remove(toUnmute, memberRoles);
			return true;
		}
		return false;
	}

	public static boolean unmuteMember(final Guild guild, final Member unmuter, final Member toUnmute,
			@Nullable final Message toDelete) {
		if (toDelete != null) {
			toDelete.delete().queue();
		}

		if (MuteCommand.MUTED_ROLE_MAP.containsKey(toUnmute)) {
			final var loggingChannel = BotUtils.getModLogChannel(guild);

			// Remove muted role and add back original roles
			final List<Role> memberRoles = MuteCommand.MUTED_ROLE_MAP.get(toUnmute);
			guild.removeRoleFromMember(toUnmute, BotUtils.getMutedRole(guild))
					.queue(rem -> memberRoles.forEach(role -> guild.addRoleToMember(toUnmute, role).queue()));

			// Log the unmute
			final var unmuteEmbed = new EmbedBuilder().setColor(toUnmute.getColorRaw())
					.setTitle(toUnmute.getEffectiveName() + " has been unmuted.")
					.setDescription("**Unmuted By**: " + unmuter.getAsMention());
			loggingChannel.sendMessageEmbeds(unmuteEmbed.build()).queue();

			// Remove member from role cache
			MuteCommand.MUTED_ROLE_MAP.remove(toUnmute, memberRoles);
			return true;
		}
		return false;
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.MODERATION;
	}

	@Override
	public String getDescription() {
		return "Unmutes a member.";
	}

	@Override
	public String getName() {
		return "unmute";
	}

	@Override
	public void handle(final CommandContext ctx) {
		final var message = ctx.getMessage();
		if (ctx.getArgs().length < 1) {
			message.reply("You must specify the user that you want to unmute!").mentionRepliedUser(false).queue();
			return;
		}

		final String user = ctx.getArgs()[0];

		Member toUnmute = null;

		try {
			toUnmute = message.getMentionedMembers().get(0);
		} catch (final IndexOutOfBoundsException ex) {
			ctx.getGuild().retrieveMemberById(user).queue();
			toUnmute = ctx.getGuild().getMemberById(user);
		}

		if (toUnmute == null) {
			message.reply("Could not find user: " + user).mentionRepliedUser(false).queue();
			return;
		}

		if (toUnmute.getIdLong() == ctx.getAuthor().getIdLong()) {
			message.reply("You cannot unmute yourself!").mentionRepliedUser(false).queue();
			return;
		}

		if (!ctx.getMember().canInteract(toUnmute)) {
			message.reply("You cannot unmute this user!").mentionRepliedUser(false).queue();
			return;
		}

		if (!unmuteMember(ctx.getGuild(), ctx.getMember(), toUnmute, message)) {
			message.reply("You cannot unmute a member that is not muted!").mentionRepliedUser(false).queue();
		}
	}

	@Override
	public boolean isModeratorOnly() {
		return true;
	}
}
