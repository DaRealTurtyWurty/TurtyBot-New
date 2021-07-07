package io.github.darealturtywurty.turtybot.commands.utility;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;
import io.github.darealturtywurty.turtybot.util.BotUtils;

public class RestartCommand implements IGuildCommand {

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.UTILITY;
	}

	@Override
	public String getDescription() {
		return "Restarts the bot!";
	}

	@Override
	public String getName() {
		return "restart";
	}

	@Override
	public void handle(final CommandContext ctx) {
		try {
			ctx.getChannel().sendMessage("Restarting bot!").queue();
			ctx.getMessage().delete().queue();
			Logger.getGlobal().log(Level.WARNING, "Restarting bot!");
			BotUtils.restartApplication(ctx.getJDA());
		} catch (final Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public boolean isOwnerOnly() {
		return true;
	}
}
