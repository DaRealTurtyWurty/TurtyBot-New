package io.github.darealturtywurty.turtybot.commands.fun;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import io.github.darealturtywurty.turtybot.TurtyBot;
import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;
import io.github.darealturtywurty.turtybot.util.Constants;

public class InternetRuleCommand implements IGuildCommand {

	protected static final List<String> RULES = new ArrayList<>();

	public InternetRuleCommand() {
		final InputStream stream = TurtyBot.class.getResourceAsStream("/rules_of_the_internet.txt");
		try {
			final var reader = new BufferedReader(new InputStreamReader(stream));
			if (reader.ready()) {
				reader.lines().forEach(RULES::add);
			}
		} catch (final Exception e) {
			Constants.LOGGER.log(Level.WARNING, "There has been an issue parsing file: {0}", stream);
		}
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.FUN;
	}

	@Override
	public String getDescription() {
		return "Gets a rule from The Rules of The Internet.";
	}

	@Override
	public String getName() {
		return "erule";
	}

	@Override
	public void handle(final CommandContext ctx) {
		if (ctx.getArgs().length < 1) {
			ctx.getMessage().reply("You must specify the rule number! (1-100)").mentionRepliedUser(false).queue();
			return;
		}

		int number = 0;
		try {
			number = Integer.parseInt(ctx.getArgs()[0]);
			if (number < 1) {
				ctx.getMessage().reply("You must specify the rule number! (1-100)").mentionRepliedUser(false).queue();
				return;
			}

			if (number > RULES.size()) {
				ctx.getMessage().reply("You must specify the rule number! (1-100)").mentionRepliedUser(false).queue();
				return;
			}
		} catch (final NumberFormatException ex) {
			number = 0;
		}

		ctx.getMessage().reply(RULES.get(number - 1)).mentionRepliedUser(false).queue();
	}
}
