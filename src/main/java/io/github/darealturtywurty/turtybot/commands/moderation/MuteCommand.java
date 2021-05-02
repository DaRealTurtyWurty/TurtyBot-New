package io.github.darealturtywurty.turtybot.commands.moderation;

import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

public class MuteCommand implements IGuildCommand {

	@Override
	public void handle(CommandContext ctx) {
		if (ctx.getArgs().length <= 0)
			return;

		Member toMute = null;

		if (!ctx.getMessage().getMentionedMembers().isEmpty()) {
			int multiplier = 1000;
			if (ctx.getArgs().length >= 2) {
				multiplier = parseTimeUnit(ctx.getArgs()[1]);
			}
			muteMember(ctx.getGuild(), toMute, multiplier, 100);
		}

		String arg1 = ctx.getArgs()[0];
	}

	private int parseTimeUnit(String time) {
		int multiplier = 1000;
		for (char c : time.toCharArray()) {
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

	protected static void muteMember(Guild guild, Member toMute, int multiplier, int time) {

	}

	@Override
	public String getName() {
		return "mute";
	}

	@Override
	public String getDescription() {
		return "Mutes a member.";
	}

	@Override
	public boolean isModeratorOnly() {
		return true;
	}
}
