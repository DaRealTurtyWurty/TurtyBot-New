package io.github.darealturtywurty.turtybot.commands.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.CommandManager;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;
import io.github.darealturtywurty.turtybot.util.BotUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.internal.utils.tuple.Pair;

public class HelpCommand implements IGuildCommand {

	private final CommandManager commandManager;

	public HelpCommand(final CommandManager commandManager) {
		this.commandManager = commandManager;
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.UTILITY;
	}

	@Override
	public String getDescription() {
		return "Gets the information about the current command!";
	}

	@Override
	public String getName() {
		return "help";
	}

	@Override
	public void handle(final CommandContext ctx) {
		final String prefix = BotUtils.getPrefixFromGuild(ctx.getGuild());
		if (ctx.getArgs().length <= 0) {
			ctx.getChannel().sendMessage("To get a list of commands, use `" + prefix + "commands`.\n").queue();
			return;
		}

		final IGuildCommand command = this.commandManager.getCommand(ctx.getArgs()[0]);
		if (command == null) {
			ctx.getChannel().sendMessage("No command found for " + ctx.getArgs()[0]).queue();
			return;
		}

		final List<String> channels = new ArrayList<>();
		String channelStr;
		if (command.validChannels().getLeft()) {
			command.validChannels().getRight().stream()
					.map(channel -> ctx.getGuild().getTextChannelsByName(channel, true).stream()
							.map(TextChannel::getAsMention).collect(Collectors.toList()))
					.collect(Collectors.toList()).forEach(channels::addAll);
			channelStr = String.join(", ", channels);
			channelStr = channelStr.isEmpty() ? "This command cannot be used in any channel!" : channelStr;
		} else {
			channelStr = "This command can be used in any channel!";
		}

		final var embed = new EmbedBuilder();
		embed.setTitle("Information about command: " + command.getName());
		embed.setDescription(command.getDescription());
		embed.addField("Aliases: ", command.getAliases().isEmpty() ? "There are no aliases for this command!"
				: "`" + prefix + String.join("`, `" + prefix, command.getAliases()) + "`", false);
		embed.addField("Valid Channels for this command: ", channelStr, false);
		embed.addField("Is owner only?", BotUtils.trueFalseToYesNo(command.isOwnerOnly()), false);
		embed.addField("Is NSFW?", BotUtils.trueFalseToYesNo(command.isNSFW()), false);
		embed.addField("Is moderator only?", BotUtils.trueFalseToYesNo(command.isModeratorOnly()), false);
		embed.addField("Is server booster only?", BotUtils.trueFalseToYesNo(command.isBoosterOnly()), false);
		embed.addField("Is developer mode only?", BotUtils.trueFalseToYesNo(command.isDevelopmentOnly()), false);
		embed.addField("Should private message?", BotUtils.trueFalseToYesNo(command.shouldPrivateMessage()), false);
		embed.setColor(BotUtils.generateRandomPastelColor());
		ctx.getMessage().replyEmbeds(embed.build()).mentionRepliedUser(false).queue(msg -> {
			ctx.getMessage().delete().queueAfter(30, TimeUnit.SECONDS);
			msg.delete().queueAfter(30, TimeUnit.SECONDS);
		});
	}

	@Override
	public Pair<Boolean, List<String>> validChannels() {
		return Pair.of(true, Arrays.asList("bot-stuff"));
	}
}
