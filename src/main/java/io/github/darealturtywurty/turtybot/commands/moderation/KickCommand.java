package io.github.darealturtywurty.turtybot.commands.moderation;

import java.awt.Color;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;
import io.github.darealturtywurty.turtybot.util.BotUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

public class KickCommand implements IGuildCommand {

	@Override
	public void handle(CommandContext ctx) {
		final var guild = ctx.getGuild();
		final var message = ctx.getMessage();
		final String strToKick = ctx.getArgs()[0];
		if (!strToKick.isBlank() && !strToKick.isEmpty()) {
			if (ctx.getMember().hasPermission(Permission.KICK_MEMBERS)) {
				Member toKick = null;
				try {
					toKick = message.getMentionedMembers().get(0);
				} catch (IndexOutOfBoundsException ex) {
					guild.retrieveMemberById(strToKick).queue();
					toKick = guild.getMemberById(strToKick);
				}

				if (toKick == null) {
					message.reply("Could not find user: " + strToKick).mentionRepliedUser(false).queue();
					return;
				}

				if (toKick.getIdLong() == ctx.getAuthor().getIdLong()) {
					message.reply("You cannot kick yourself!").mentionRepliedUser(false).queue();
					return;
				}

				if (!ctx.getMember().canInteract(toKick)) {
					message.reply("You cannot kick this user!").mentionRepliedUser(false).queue();
					return;
				}

				var reason = "Unspecified";

				if (ctx.getArgs().length > 1) {
					reason = String.join(" ", ctx.getArgs()).replace(strToKick, "").trim();
				}

				kickMember(guild, ctx.getMember(), toKick, ctx.getMessage(), reason);
				return;
			}

			message.reply("You do not have permission to use this command!").mentionRepliedUser(false).queue(reply -> {
				message.delete().queueAfter(15, TimeUnit.SECONDS);
				reply.delete().queueAfter(15, TimeUnit.SECONDS);
			});
			return;
		}

		message.reply("You must specify the member that you want to kick!").mentionRepliedUser(false).queue(reply -> {
			message.delete().queueAfter(15, TimeUnit.SECONDS);
			reply.delete().queueAfter(15, TimeUnit.SECONDS);
		});
	}

	public static void kickMember(Guild guild, Member kicker, Member toKick, @Nullable Message toDelete, String reason) {
		final var kickEmbed = new EmbedBuilder().setColor(Color.RED).setTitle("You were kicked from: " + guild.getName())
				.setDescription("**Reason**: " + reason + "\n**Kicked By**: " + kicker.getAsMention());
		toKick.getUser().openPrivateChannel().queueAfter(5, TimeUnit.SECONDS,
				channel -> channel.sendMessage(kickEmbed.build()).queue(msg -> toKick.kick(reason).queue()));

		final var kickLogEmbed = new EmbedBuilder().setColor(Color.RED).setTitle(toKick.getEffectiveName() + " was kicked!")
				.setDescription("**Reason**: " + reason + "\n**Kicked By**: " + kicker.getAsMention());
		BotUtils.getModLogChannel(guild).sendMessage(kickLogEmbed.build()).queue();

		if (toDelete != null)
			toDelete.delete().queue();
	}

	@Override
	public String getName() {
		return "kick";
	}

	@Override
	public String getDescription() {
		return "Kicks a member from the guild.";
	}

	@Override
	public boolean isModeratorOnly() {
		return true;
	}
}
