package io.github.darealturtywurty.turtybot.commands.fun;

import java.util.List;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;

public class ReverseTextCommand implements IGuildCommand {

	@Override
	public List<String> getAliases() {
		return List.of("revtext", "rev-text", "rev_text", "reverse-text", "reverse_text");
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.FUN;
	}

	@Override
	public String getDescription() {
		return "Reverses the input text.";
	}

	@Override
	public String getName() {
		return "reversetext";
	}

	@Override
	public void handle(final CommandContext ctx) {
		if (ctx.getArgs().length < 1) {
			ctx.getMessage().reply("You must supply the text that you want to reverse!").mentionRepliedUser(false).queue();
			return;
		}

		final String text = String.join(" ", ctx.getArgs());
		ctx.getMessage().reply(new StringBuilder(text).reverse().toString()).mentionRepliedUser(false).queue();
	}
}
