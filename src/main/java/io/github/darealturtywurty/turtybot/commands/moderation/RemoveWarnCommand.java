package io.github.darealturtywurty.turtybot.commands.moderation;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;
import io.github.darealturtywurty.turtybot.util.BotUtils.WarnUtils;

public class RemoveWarnCommand implements IGuildCommand {

	@Override
	public void handle(CommandContext ctx) {
		if (ctx.getArgs().length < 1) {
			ctx.getMessage().reply("You must supply the UUID of the warn that you wish to remove!").mentionRepliedUser(false)
					.queue();
			return;
		}

		boolean complete = WarnUtils.removeWarnByUUID(ctx.getGuild(), ctx.getMember(), ctx.getArgs()[0]);
		if (!complete)
			ctx.getMessage().reply("You must provide a valid UUID.").mentionRepliedUser(false).queue(msg -> {
				msg.delete().queueAfter(30, TimeUnit.SECONDS);
				ctx.getMessage().delete().queueAfter(30, TimeUnit.SECONDS);
			});
		else
			ctx.getMessage().delete().queue();
	}

	@Override
	public String getName() {
		return "removewarn";
	}

	@Override
	public String getDescription() {
		return "Removes the warn with a specific UUID from a user.";
	}

	@Override
	public List<String> getAliases() {
		return Arrays.asList("delwarn", "deletewarn", "remwarn", "destroywarn");
	}

	@Override
	public boolean isModeratorOnly() {
		return true;
	}
}
