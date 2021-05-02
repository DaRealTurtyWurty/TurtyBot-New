package io.github.darealturtywurty.turtybot.commands.bot;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;
import io.github.darealturtywurty.turtybot.util.BotUtils;

public class RestartCommand implements IGuildCommand {

	@Override
	public void handle(CommandContext ctx) {
		try {
			ctx.getChannel().sendMessage("Restarting bot!").queue();
			ctx.getMessage().delete().queue();
			Logger.getGlobal().log(Level.WARNING, "Restarting bot!");
			BotUtils.restartApplication(ctx.getJDA());
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public String getName() {
		return "restart";
	}

	@Override
	public String getDescription() {
		return "Restarts the bot!";
	}

	@Override
	public boolean isOwnerOnly() {
		return true;
	}
}
