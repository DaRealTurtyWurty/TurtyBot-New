package io.github.darealturtywurty.turtybot.commands.utility;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;
import net.dv8tion.jda.internal.utils.tuple.Pair;

public class PingCommand implements IGuildCommand {

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.UTILITY;
	}

	@Override
	public String getDescription() {
		return "Shows the ping between the discord bot and the discord servers.";
	}

	@Override
	public String getName() {
		return "ping";
	}

	@Override
	public void handle(final CommandContext ctx) {
		ctx.getJDA().getRestPing()
				.queue(ping -> ctx.getMessage()
						.replyFormat("Rest Ping: %sms\nWebsocket Ping: %sms", ping, ctx.getJDA().getGatewayPing())
						.mentionRepliedUser(false).queue(reply -> {
							ctx.getMessage().delete().queueAfter(15, TimeUnit.SECONDS);
							reply.delete().queueAfter(15, TimeUnit.SECONDS);
						}));
	}

	@Override
	public Pair<Boolean, List<String>> validChannels() {
		return Pair.of(true, Arrays.asList("bot-stuff"));
	}
}
