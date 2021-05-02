package io.github.darealturtywurty.turtybot.help_system;

import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;
import io.github.darealturtywurty.turtybot.util.BotUtils;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class CloseChannelCommand implements IGuildCommand {

	@Override
	public void handle(CommandContext ctx) {
		TextChannel channel = ctx.getChannel();
		User user = ctx.getAuthor();
		if (!channel.getParent().getName().contains("Support ") || !BotUtils.isModerator(ctx.getGuild(), ctx.getMember())
				|| !channel.getTopic().split("\n")[0].equalsIgnoreCase(user.getId()))
			return;

		channel.sendMessage("Are you sure you want to close this channel?").queue(message -> {
			message.addReaction("✔").queue(yesReaction -> message.addReaction("❌").queue(noReaction -> ctx.getJDA()
					.addEventListener(new HelpReactionEventListener(channel, message.getIdLong()))));
			ctx.getMessage().delete().queue();
		});
	}

	@Override
	public String getName() {
		return "close";
	}

	@Override
	public String getDescription() {
		return "Closes the channel.";
	}
}
