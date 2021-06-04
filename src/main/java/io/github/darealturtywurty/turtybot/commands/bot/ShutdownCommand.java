package io.github.darealturtywurty.turtybot.commands.bot;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;
import io.github.darealturtywurty.turtybot.util.BotUtils;
import net.dv8tion.jda.api.entities.TextChannel;

public class ShutdownCommand implements IGuildCommand {

	@Override
	public void handle(CommandContext ctx) {
		ctx.getJDA().getGuilds().forEach(guild -> {
			List<TextChannel> channels = guild.getTextChannels().stream()
					.filter(channel -> channel.getName().contains("general")).collect(Collectors.toList());
			if (!channels.isEmpty())
				channels.get(0).sendMessage("I am now going offline for maintenance. Apologies for any inconveniences!")
						.queue();
		});
		ctx.getMessage().delete().queue(deletion -> {
			Logger.getGlobal().log(Level.WARNING, "Shutting down bot!");
			BotUtils.shutdownApplication(ctx.getJDA());
		});

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
