package io.github.darealturtywurty.turtybot.commands.bot;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;
import net.dv8tion.jda.internal.utils.tuple.Pair;

public class PingCommand implements IGuildCommand {

	@Override
	public void handle(CommandContext ctx) {
		ctx.getJDA().getRestPing()
				.queue(ping -> ctx.getMessage()
						.replyFormat("Rest Ping: %sms\nWebsocket Ping: %sms", ping, ctx.getJDA().getGatewayPing())
						.mentionRepliedUser(false).queue(reply -> {
							ctx.getMessage().delete().queueAfter(15, TimeUnit.SECONDS);
							reply.delete().queueAfter(15, TimeUnit.SECONDS);
						}));
	}

	@Override
	public String getName() {
		return "ping";
	}

	@Override
	public Pair<Boolean, List<String>> validChannels() {
		return Pair.of(true, Arrays.asList("bot-stuff"));
	}

	@Override
	public String getDescription() {
		return "Shows the ping between the discord bot and the discord servers.";
	}
}
