package io.github.darealturtywurty.turtybot.commands.utility;

import java.util.List;

import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;

public class RolesCommand implements IGuildCommand {

	@Override
	public void handle(CommandContext ctx) {
		ctx.getMessage().reply("```" + getRoles(ctx.getGuild()) + "```").mentionRepliedUser(false).queue();
	}

	@Override
	public String getName() {
		return "roles";
	}

	@Override
	public String getDescription() {
		return "Gets all the roles in the current guild";
	}

	private String getRoles(Guild guild) {
		var strBuilder = new StringBuilder();
		List<Role> roles = guild.getRoles();
		for (var index = 0; index < roles.size(); index++) {
			strBuilder.append(padRight(roles.get(index).getName().replace("@everyone", "Members with no role") + ":", 25)
					+ guild.getMembersWithRoles(roles.get(index)).size() + "\n");
		}
		return strBuilder.toString();
	}

	public static String padRight(String s, int n) {
		return String.format("%-" + n + "s", s);
	}
}
