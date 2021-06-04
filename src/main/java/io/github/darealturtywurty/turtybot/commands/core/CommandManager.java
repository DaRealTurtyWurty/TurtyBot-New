package io.github.darealturtywurty.turtybot.commands.core;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import io.github.darealturtywurty.turtybot.commands.bot.ConfigCommand;
import io.github.darealturtywurty.turtybot.commands.bot.PingCommand;
import io.github.darealturtywurty.turtybot.commands.bot.RestartCommand;
import io.github.darealturtywurty.turtybot.commands.bot.ShutdownCommand;
import io.github.darealturtywurty.turtybot.commands.fun.InternetRuleCommand;
import io.github.darealturtywurty.turtybot.commands.moderation.BanCommand;
import io.github.darealturtywurty.turtybot.commands.utility.CommandListCommand;
import io.github.darealturtywurty.turtybot.commands.utility.HelpCommand;
import io.github.darealturtywurty.turtybot.commands.utility.SolutionsCommand;
import io.github.darealturtywurty.turtybot.help_system.CloseChannelCommand;
import io.github.darealturtywurty.turtybot.help_system.RequestHelpCommand;
import io.github.darealturtywurty.turtybot.util.BotUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class CommandManager {
	private final Set<IGuildCommand> commands = new HashSet<>();

	public CommandManager() {
		this.addCommand(new PingCommand());
		this.addCommand(new HelpCommand(this));
		this.addCommand(new CommandListCommand(this));
		this.addCommand(new ShutdownCommand());
		this.addCommand(new RestartCommand());
		this.addCommand(new ConfigCommand());
		this.addCommand(new SolutionsCommand());
		this.addCommand(new RequestHelpCommand());
		this.addCommand(new CloseChannelCommand());
		this.addCommand(new PutinCommand());
		this.addCommand(new BanCommand());
		this.addCommand(new InternetRuleCommand());
	}

	public void addCommand(IGuildCommand cmd) {
		boolean cmdExists = this.commands.stream().anyMatch(command -> command.getName().equalsIgnoreCase(cmd.getName()));

		if (cmdExists) {
			throw new IllegalArgumentException("A command with this name is already present! Command Name: " + cmd);
		}

		this.commands.add(cmd);
	}

	public Set<IGuildCommand> getCommands() {
		return this.commands;
	}

	@Nullable
	public IGuildCommand getCommand(String search) {
		for (IGuildCommand cmd : commands) {
			if (cmd.getName().equalsIgnoreCase(search) || cmd.getAliases().contains(search)) {
				return cmd;
			}
		}
		return null;
	}

	public void handle(GuildMessageReceivedEvent event) {
		User user = event.getAuthor();
		TextChannel channel = event.getChannel();
		Message message = event.getMessage();
		Guild guild = event.getGuild();

		String prefix = BotUtils.getPrefixFromGuild(guild);
		String[] split = message.getContentRaw().replaceFirst("(?i)" + Pattern.quote(prefix), "").split("\\s+");
		String invoke = split[0].toLowerCase();

		IGuildCommand cmd = this.getCommand(invoke);

		if (cmd != null) {
			if ((cmd.isOwnerOnly() && BotUtils.isBotOwner(user)) || !cmd.isOwnerOnly()) {
				if ((cmd.isModeratorOnly() && BotUtils.isModerator(guild, user)) || !cmd.isModeratorOnly()) {
					if ((cmd.isBoosterOnly() && event.getMember().getTimeBoosted() != null)
							|| user.getIdLong() == 411601775078932491L || !cmd.isBoosterOnly()) {
						if ((!cmd.validChannels().getLeft()
								|| cmd.validChannels().getRight().contains(channel.getName().toLowerCase()))
								&& (!cmd.validCategories().getLeft() || cmd.validCategories().getRight()
										.contains(channel.getParent().getName().toLowerCase()))) {
							// channel.sendTyping().queue();
							List<String> args = Arrays.asList(split).subList(1, split.length);
							CommandContext ctx = new CommandContext(event, args.toArray(new String[0]));
							cmd.handle(ctx);
							return;
						}
						message.delete().queueAfter(15, TimeUnit.SECONDS);
						return;
					}
					message.reply("You must be a \"cool and nice\" server booster to use this command!")
							.mentionRepliedUser(false).queue(reply -> {
								message.delete().queueAfter(15, TimeUnit.SECONDS);
								reply.delete().queueAfter(15, TimeUnit.SECONDS);
							});
					return;
				}
				message.reply("You must be a moderator in order to use this command!").mentionRepliedUser(false)
						.queue(reply -> {
							message.delete().queueAfter(15, TimeUnit.SECONDS);
							reply.delete().queueAfter(15, TimeUnit.SECONDS);
						});
				return;
			}
			message.reply("You must be the bot owner in order to use this command!").mentionRepliedUser(false)
					.queue(reply -> {
						message.delete().queueAfter(15, TimeUnit.SECONDS);
						reply.delete().queueAfter(15, TimeUnit.SECONDS);
					});
			return;
		}
		message.reply("It seems that the command you were looking for does not exist!").mentionRepliedUser(false)
				.queue(reply -> {
					message.delete().queueAfter(15, TimeUnit.SECONDS);
					reply.delete().queueAfter(15, TimeUnit.SECONDS);
				});
		return;
	}
}