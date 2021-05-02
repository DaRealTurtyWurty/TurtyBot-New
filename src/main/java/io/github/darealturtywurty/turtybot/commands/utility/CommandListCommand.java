package io.github.darealturtywurty.turtybot.commands.utility;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
	public void handle(CommandContext ctx) {
		String prefix = BotUtils.getPrefixFromGuild(ctx.getGuild());
		ctx.getMessage()
				.reply(new EmbedBuilder().setTitle("My list of commands!").setColor(BotUtils.generateRandomColor())
						.setDescription(prefix + this.commandManager.getCommands().stream().map(IGuildCommand::getName)
								.collect(Collectors.joining("\n" + prefix)))
						.build())
				.mentionRepliedUser(false).queue(msg -> {
					ctx.getMessage().delete().queueAfter(30, TimeUnit.SECONDS);
					msg.delete().queueAfter(30, TimeUnit.SECONDS);
				});
	}

	@Override
	public String getName() {
		return "commands";
	}

	@Override
	public List<String> getAliases() {
		return Arrays.asList("commandlist", "cmds", "commandslist", "cmdlist");
	}

	@Override
	public Pair<Boolean, List<String>> validChannels() {
		return Pair.of(true, Arrays.asList("bot-stuff"));
	}

	@Override
	public String getDescription() {
		return "Gets the list of commands.";
	}
}
