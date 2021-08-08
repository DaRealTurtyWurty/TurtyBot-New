package io.github.darealturtywurty.turtybot.commands.fun;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;
import io.github.darealturtywurty.turtybot.util.Constants;

public class CoinFlipCommand implements IGuildCommand {

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.FUN;
	}

	@Override
	public String getDescription() {
		return "Flips a virtual coin!";
	}

	@Override
	public String getName() {
		return "coinflip";
	}

	@Override
	public void handle(final CommandContext ctx) {
		if (Constants.RANDOM.nextInt(1000) == 0) {
			ctx.getMessage().reply("It landed on it's side. It was neither heads or tails! 😔").mentionRepliedUser(false)
					.queue();
		} else {
			ctx.getMessage().reply("It was: " + (Constants.RANDOM.nextBoolean() ? "Heads 🗣" : "Tails 🐍") + "!")
					.mentionRepliedUser(false).queue();
		}
	}
}
