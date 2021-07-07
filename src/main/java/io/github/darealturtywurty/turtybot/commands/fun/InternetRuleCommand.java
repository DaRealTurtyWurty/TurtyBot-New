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
import net.dv8tion.jda.api.entities.TextChannel;

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
		final TextChannel channel = ctx.getChannel();
		var noNumber = false;
		if (ctx.getArgs().length < 1) {
			noNumber = true;
		} else {
			try {
				Integer.parseInt(ctx.getArgs()[0]);
				noNumber = false;
			} catch (final NumberFormatException e) {
				noNumber = true;
			}
		}

		if (!noNumber && RULES.size() > Integer.parseInt(ctx.getArgs()[0])) {
			channel.sendMessage(RULES.get(Integer.parseInt(ctx.getArgs()[0]) - 1)).queue();
		} else {
			channel.sendMessage("Invalid rule number! There are only " + RULES.size() + " rules.").queue();
		}

		ctx.getMessage().delete().queue();
	}
}
