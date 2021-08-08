package io.github.darealturtywurty.turtybot.commands.moderation;

import java.awt.Color;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;
import io.github.darealturtywurty.turtybot.util.BotUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

public class BanCommand implements IGuildCommand {
	public static void banMember(final Guild guild, final Member banner, final Member toBan,
			@Nullable final Message toDelete, final String reason) {
		final var bannedEmbed = new EmbedBuilder().setColor(Color.RED).setTitle("You were banned from: " + guild.getName())
				.setDescription("**Reason**: " + reason + "\n**Banned By**: " + banner.getAsMention());
		toBan.getUser().openPrivateChannel().queueAfter(5, TimeUnit.SECONDS,
				channel -> channel.sendMessageEmbeds(bannedEmbed.build()).queue());

		final var banLogEmbed = new EmbedBuilder().setColor(Color.RED).setTitle(toBan.getEffectiveName() + " was banned!")
				.setDescription("**Reason**: " + reason + "\n**Banned By**: " + banner.getAsMention());
		BotUtils.getModLogChannel(guild).sendMessageEmbeds(banLogEmbed.build()).queue(msg -> toBan.ban(0, reason).queue());

		if (toDelete != null) {
			toDelete.delete().queue();
		}
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.MODERATION;
	}

	@Override
	public String getDescription() {
		return "Bans a member from the guild.";
	}

	@Override
	public String getName() {
		return "ban";
	}

	@Override
	public void handle(final CommandContext ctx) {
		final var guild = ctx.getGuild();
		final var message = ctx.getMessage();
		final String strToBan = ctx.getArgs()[0];
		if (!strToBan.isBlank() && !strToBan.isEmpty()) {
			if (ctx.getMember().hasPermission(Permission.BAN_MEMBERS, Permission.KICK_MEMBERS)) {
				Member toBan = null;
				try {
					toBan = message.getMentionedMembers().get(0);
				} catch (final IndexOutOfBoundsException ex) {
					guild.retrieveMemberById(strToBan).queue();
					toBan = guild.getMemberById(strToBan);
				}

				if (toBan == null) {
					message.reply("Could not find user: " + strToBan).mentionRepliedUser(false).queue();
					return;
				}

				if (toBan.getIdLong() == ctx.getAuthor().getIdLong()) {
					message.reply("You cannot ban yourself!").mentionRepliedUser(false).queue();
					return;
				}

				if (!ctx.getMember().canInteract(toBan)) {
					message.reply("You cannot ban this user!").mentionRepliedUser(false).queue();
					return;
				}

				var reason = "Unspecified";

				if (ctx.getArgs().length > 1) {
					reason = String.join(" ", ctx.getArgs()).replace(strToBan, "").trim();
				}

				banMember(guild, ctx.getMember(), toBan, message, reason);
				return;
			}

			message.reply("You do not have permission to use this command!").mentionRepliedUser(false).queue(reply -> {
				message.delete().queueAfter(15, TimeUnit.SECONDS);
				reply.delete().queueAfter(15, TimeUnit.SECONDS);
			});
			return;
		}

		message.reply("You must specify the member that you want to ban!").mentionRepliedUser(false).queue(reply -> {
			message.delete().queueAfter(15, TimeUnit.SECONDS);
			reply.delete().queueAfter(15, TimeUnit.SECONDS);
		});
	}

	@Override
	public boolean isModeratorOnly() {
		return true;
	}
}
