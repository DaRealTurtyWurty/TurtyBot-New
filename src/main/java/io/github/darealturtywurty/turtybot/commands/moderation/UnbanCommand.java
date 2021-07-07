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
import net.dv8tion.jda.api.exceptions.AccountTypeException;

public class UnbanCommand implements IGuildCommand {

	public static void unbanUser(final Guild guild, final Member unbanner, final long toUnban,
			@Nullable final Message toDelete, final String reason) {
		guild.unban(String.valueOf(toUnban)).queue();
		final var unbanLogEmbed = new EmbedBuilder().setColor(Color.RED)
				.setTitle("User with ID: \"" + toUnban + "\" was unbanned!")
				.setDescription("**Reason**: " + reason + "\n**Unbanned By**: " + unbanner.getAsMention());
		BotUtils.getModLogChannel(guild).sendMessageEmbeds(unbanLogEmbed.build()).queue();

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
		return "Unbans a member from the guild.";
	}

	@Override
	public String getName() {
		return "unban";
	}

	@Override
	public void handle(final CommandContext ctx) {
		final var guild = ctx.getGuild();
		final var message = ctx.getMessage();
		final String strToUnban = ctx.getArgs()[0];
		if (!strToUnban.isBlank() && !strToUnban.isEmpty()) {
			if (ctx.getMember().hasPermission(Permission.BAN_MEMBERS, Permission.KICK_MEMBERS)) {
				var user = 0L;

				try {
					ctx.getJDA().retrieveUserById(strToUnban).queue();
					user = ctx.getJDA().getUserById(strToUnban).getIdLong();
				} catch (IllegalArgumentException | AccountTypeException ex) {
					try {
						user = ctx.getMessage().getMentionedUsers().get(0).getIdLong();
					} catch (final IndexOutOfBoundsException exc) {
						message.reply("You must provide a valid user ID!").mentionRepliedUser(false).queue();
						return;
					}
				} catch (final NullPointerException ex) {
					user = Long.parseLong(strToUnban);
				}

				if (user == ctx.getAuthor().getIdLong()) {
					message.reply("You cannot unban yourself!").mentionRepliedUser(false).queue();
					return;
				}

				var reason = "Unspecified";

				if (ctx.getArgs().length > 1) {
					reason = String.join(" ", ctx.getArgs()).replace(strToUnban, "").trim();
				}

				unbanUser(guild, ctx.getMember(), user, message, reason);
				return;
			}

			message.reply("You do not have permission to use this command!").mentionRepliedUser(false).queue(reply -> {
				message.delete().queueAfter(15, TimeUnit.SECONDS);
				reply.delete().queueAfter(15, TimeUnit.SECONDS);
			});
			return;
		}

		message.reply("You must specify the member that you want to unban!").mentionRepliedUser(false).queue(reply -> {
			message.delete().queueAfter(15, TimeUnit.SECONDS);
			reply.delete().queueAfter(15, TimeUnit.SECONDS);
		});
	}

	@Override
	public boolean isModeratorOnly() {
		return true;
	}
}
