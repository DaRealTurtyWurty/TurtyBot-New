package io.github.darealturtywurty.turtybot.commands.core;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import io.github.darealturtywurty.turtybot.commands.fun.AdviceCommand;
import io.github.darealturtywurty.turtybot.commands.fun.InternetRuleCommand;
import io.github.darealturtywurty.turtybot.commands.fun.MemeCommand;
import io.github.darealturtywurty.turtybot.commands.fun.PutinCommand;
import io.github.darealturtywurty.turtybot.commands.minecraft.MojangStatusCommand;
import io.github.darealturtywurty.turtybot.commands.minecraft.UserUUIDCommand;
import io.github.darealturtywurty.turtybot.commands.moderation.BanCommand;
import io.github.darealturtywurty.turtybot.commands.moderation.ClearCommand;
import io.github.darealturtywurty.turtybot.commands.moderation.ClearWarnsCommand;
import io.github.darealturtywurty.turtybot.commands.moderation.KickCommand;
import io.github.darealturtywurty.turtybot.commands.moderation.MuteCommand;
import io.github.darealturtywurty.turtybot.commands.moderation.RemoveWarnCommand;
import io.github.darealturtywurty.turtybot.commands.moderation.UnbanCommand;
import io.github.darealturtywurty.turtybot.commands.moderation.UnmuteCommand;
import io.github.darealturtywurty.turtybot.commands.moderation.WarnCommand;
import io.github.darealturtywurty.turtybot.commands.moderation.WarningsCommand;
import io.github.darealturtywurty.turtybot.commands.utility.BotInfoCommand;
import io.github.darealturtywurty.turtybot.commands.utility.CommandListCommand;
import io.github.darealturtywurty.turtybot.commands.utility.ConfigCommand;
import io.github.darealturtywurty.turtybot.commands.utility.HelpCommand;
import io.github.darealturtywurty.turtybot.commands.utility.PingCommand;
import io.github.darealturtywurty.turtybot.commands.utility.RestartCommand;
import io.github.darealturtywurty.turtybot.commands.utility.RolesCommand;
import io.github.darealturtywurty.turtybot.commands.utility.ShutdownCommand;
import io.github.darealturtywurty.turtybot.commands.utility.SolutionsCommand;
import io.github.darealturtywurty.turtybot.managers.help_system.CloseChannelCommand;
import io.github.darealturtywurty.turtybot.managers.help_system.RequestHelpCommand;
import io.github.darealturtywurty.turtybot.managers.levelling_system.InventoryCommand;
import io.github.darealturtywurty.turtybot.managers.levelling_system.LeaderboardCommand;
import io.github.darealturtywurty.turtybot.managers.levelling_system.RankCardCommand;
import io.github.darealturtywurty.turtybot.managers.levelling_system.RankCommand;
import io.github.darealturtywurty.turtybot.managers.music.ClearQueueCommand;
import io.github.darealturtywurty.turtybot.managers.music.JoinCommand;
import io.github.darealturtywurty.turtybot.managers.music.LeaveCommand;
import io.github.darealturtywurty.turtybot.managers.music.NowPlayingCommand;
import io.github.darealturtywurty.turtybot.managers.music.PauseCommand;
import io.github.darealturtywurty.turtybot.managers.music.PlayCommand;
import io.github.darealturtywurty.turtybot.managers.music.QueueCommand;
import io.github.darealturtywurty.turtybot.managers.music.RemoveCommand;
import io.github.darealturtywurty.turtybot.managers.music.ResumeCommand;
import io.github.darealturtywurty.turtybot.managers.music.ShuffleCommand;
import io.github.darealturtywurty.turtybot.managers.music.SkipCommand;
import io.github.darealturtywurty.turtybot.managers.music.SkipPlayCommand;
import io.github.darealturtywurty.turtybot.managers.music.VolumeCommand;
import io.github.darealturtywurty.turtybot.managers.music.VoteSkipCommand;
import io.github.darealturtywurty.turtybot.managers.polls.PollCommand;
import io.github.darealturtywurty.turtybot.managers.starboard.StarStatsCommand;
import io.github.darealturtywurty.turtybot.util.BotUtils;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class CommandManager {
	private final Set<IGuildCommand> commands = new HashSet<>();

	public CommandManager() {
		addCommand(new PingCommand());
		addCommand(new HelpCommand(this));
		addCommand(new CommandListCommand(this));
		addCommand(new ShutdownCommand());
		addCommand(new RestartCommand());
		addCommand(new ConfigCommand());
		addCommand(new SolutionsCommand());
		addCommand(new RequestHelpCommand());
		addCommand(new CloseChannelCommand());
		addCommand(new PutinCommand());
		addCommand(new InternetRuleCommand());
		addCommand(new AdviceCommand());
		addCommand(new BotInfoCommand());
		addCommand(new RolesCommand());
		addCommand(new MojangStatusCommand());
		addCommand(new UserUUIDCommand());
		addCommand(new BanCommand());
		addCommand(new KickCommand());
		addCommand(new MuteCommand());
		addCommand(new UnmuteCommand());
		addCommand(new UnbanCommand());
		addCommand(new WarnCommand());
		addCommand(new RemoveWarnCommand());
		addCommand(new ClearWarnsCommand());
		addCommand(new WarningsCommand());
		addCommand(new ClearCommand());
		addCommand(new StarStatsCommand());
		addCommand(new RankCommand());
		addCommand(new LeaderboardCommand());
		addCommand(new RankCardCommand());
		addCommand(new InventoryCommand());
		addCommand(new PollCommand());

		// Music
		addCommand(new PlayCommand());
		addCommand(new SkipCommand());
		addCommand(new VolumeCommand());
		addCommand(new PauseCommand());
		addCommand(new NowPlayingCommand());
		addCommand(new QueueCommand());
		addCommand(new SkipPlayCommand());
		addCommand(new ResumeCommand());
		addCommand(new JoinCommand());
		addCommand(new LeaveCommand());
		addCommand(new ShuffleCommand());
		addCommand(new RemoveCommand());
		addCommand(new VoteSkipCommand());
		addCommand(new ClearQueueCommand());

		addCommand(new MemeCommand());
	}

	public void addCommand(final IGuildCommand cmd) {
		final boolean cmdExists = this.commands.stream()
				.anyMatch(command -> command.getName().equalsIgnoreCase(cmd.getName()));

		if (cmdExists)
			throw new IllegalArgumentException("A command with this name is already present! Command Name: " + cmd);

		this.commands.add(cmd);
	}

	@Nullable
	public IGuildCommand getCommand(final String search) {
		for (final IGuildCommand cmd : this.commands) {
			if (cmd.getName().equalsIgnoreCase(search) || cmd.getAliases().contains(search))
				return cmd;
		}
		return null;
	}

	public Set<IGuildCommand> getCommands() {
		return this.commands;
	}

	public void handle(final GuildMessageReceivedEvent event) {
		final var user = event.getAuthor();
		final var channel = event.getChannel();
		final var message = event.getMessage();
		final var guild = event.getGuild();

		final String prefix = BotUtils.getPrefixFromGuild(guild);
		final String[] split = message.getContentRaw().replaceFirst("(?i)" + Pattern.quote(prefix), "").split("\\s+");
		final String invoke = split[0].toLowerCase();

		final IGuildCommand cmd = getCommand(invoke);

		if (cmd != null) {
			if (cmd.isOwnerOnly() && BotUtils.isBotOwner(user) || !cmd.isOwnerOnly()) {
				if (cmd.isModeratorOnly() && BotUtils.isModerator(guild, user) || !cmd.isModeratorOnly()) {
					if (cmd.isBoosterOnly() && event.getMember().getTimeBoosted() != null
							|| user.getIdLong() == 411601775078932491L || !cmd.isBoosterOnly()) {
						if ((!cmd.validChannels().getLeft()
								|| cmd.validChannels().getRight().contains(channel.getName().toLowerCase()))
								&& (!cmd.validCategories().getLeft() || cmd.validCategories().getRight()
										.contains(channel.getParent().getName().toLowerCase()))) {
							final List<String> args = Arrays.asList(split).subList(1, split.length);
							final var ctx = new CommandContext(event, args.toArray(new String[0]));
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
	}
}
