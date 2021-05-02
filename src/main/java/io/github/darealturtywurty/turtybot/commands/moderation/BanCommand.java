package io.github.darealturtywurty.turtybot.commands.moderation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.internal.utils.tuple.Pair;

public class BanCommand implements IGuildCommand {
	@Override
	public void handle(CommandContext ctx) {
		Guild guild = ctx.getGuild();
		Message message = ctx.getMessage();
		String strToBan = ctx.getArgs()[0];
		if (!strToBan.isBlank() && !strToBan.isEmpty()) {
			if (ctx.getMember().hasPermission(Permission.BAN_MEMBERS, Permission.KICK_MEMBERS)) {
				Member toBan = null;
				if (message.getMentionedMembers(guild).size() > 0) {
					toBan = message.getMentionedMembers(guild).get(0);
				} else if (guild.getMembersByEffectiveName(strToBan, true).size() > 0) {
					toBan = guild.getMembersByEffectiveName(strToBan, false).get(0);
				}

				if (toBan == null) {
					try {
						toBan = guild.getMemberById(Long.parseLong(strToBan));
					} catch (IllegalArgumentException e) {
						String[] nameDiscrim = strToBan.split("#");
						if (nameDiscrim[0] != null) {
							if (guild.getMembersByEffectiveName(nameDiscrim[0], false).size() > 0) {
								toBan = guild.getMembersByEffectiveName(nameDiscrim[0], false).get(0);
							} else if (nameDiscrim.length > 1) {
								toBan = guild.getMemberByTag(nameDiscrim[0], nameDiscrim[1]);
							}
						}
					}
				}
				
				if(toBan != null) { 
					toBan.ban(0);
				}
				
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
	public String getName() {
		return "ban";
	}

	@Override
	public String getDescription() {
		return "Bans a member from the guild.";
	}

	@Override
	public boolean isModeratorOnly() {
		return true;
	}

	@Override
	public Pair<Boolean, List<String>> validChannels() {
		return Pair.of(false, new ArrayList<>());
	}
}
