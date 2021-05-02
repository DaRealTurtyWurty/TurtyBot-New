package io.github.darealturtywurty.turtybot.commands.bot;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;
import io.github.darealturtywurty.turtybot.util.BotUtils;

public class ShutdownCommand implements IGuildCommand {

	@Override
	public void handle(CommandContext ctx) {
		ctx.getChannel().sendMessage("Shutting down bot!").queue();
		ctx.getMessage().delete().queue();
		Logger.getGlobal().log(Level.WARNING, "Shutting down bot!");
		BotUtils.shutdownApplication(ctx.getJDA());
		return;
	}

	@Override
	public String getName() {
		return "shutdown";
	}

	@Override
	public String getDescription() {
		return "Shuts the bot down.";
	}

	@Override
	public boolean isOwnerOnly() {
		return true;
	}
}
