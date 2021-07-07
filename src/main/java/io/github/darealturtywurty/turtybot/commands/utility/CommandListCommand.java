package io.github.darealturtywurty.turtybot.commands.utility;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.CommandManager;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;
import io.github.darealturtywurty.turtybot.util.BotUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.internal.utils.tuple.Pair;

public class CommandListCommand implements IGuildCommand {

	private final CommandManager commandManager;

	public CommandListCommand(final CommandManager commandManager) {
		this.commandManager = commandManager;
	}

	@Override
	public List<String> getAliases() {
		return Arrays.asList("commandlist", "cmds", "commandslist", "cmdlist");
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.UTILITY;
	}

	@Override
	public String getDescription() {
		return "Gets the list of commands.";
	}

	@Override
	public String getName() {
		return "commands";
	}

	@Override
	public void handle(final CommandContext ctx) {
		final String prefix = BotUtils.getPrefixFromGuild(ctx.getGuild());
		if (ctx.getArgs().length == 0) {
			final var embed = new EmbedBuilder().setTitle("My list of commands!")
					.setColor(BotUtils.generateRandomPastelColor());
			for (final var category : CommandCategory.values()) {
				embed.addField(category.emoji + " " + category.name,
						"`" + prefix + "commands " + category.name.toLowerCase() + "`", true);
			}

			ctx.getMessage().replyEmbeds(embed.build()).mentionRepliedUser(false).queue();
			return;
		}

		final var categoryStr = String.join(" ", ctx.getArgs());
		final var category = CommandCategory.byName(categoryStr).isPresent() ? CommandCategory.byName(categoryStr).get()
				: null;
		if (category != null) {
			final var embed = new EmbedBuilder();
			embed.setTitle("Commands in category: " + category);

			this.commandManager.getCommands().stream().filter(cmd -> cmd.getCategory() == category).filter(Objects::nonNull)
					.sorted((cmd1, cmd2) -> cmd1.getName().compareTo(cmd2.getName()))
					.forEach(cmd -> embed.addField(prefix + cmd.getName(), cmd.getDescription(), true));
			embed.setColor(BotUtils.generateRandomPastelColor());
			ctx.getMessage().replyEmbeds(embed.build()).mentionRepliedUser(false).queue();
		}
	}

	@Override
	public Pair<Boolean, List<String>> validChannels() {
		return Pair.of(true, Arrays.asList("bot-stuff", "test"));
	}
}
