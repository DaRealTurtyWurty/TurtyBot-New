package io.github.darealturtywurty.turtybot.managers.help_system;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;
import io.github.darealturtywurty.turtybot.util.BotUtils;
import net.dv8tion.jda.api.entities.TextChannel;

public class CloseChannelCommand implements IGuildCommand {

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.UTILITY;
	}

	@Override
	public String getDescription() {
		return "Closes the channel.";
	}

	@Override
	public String getName() {
		return "close";
	}

	@Override
	public void handle(final CommandContext ctx) {
		final TextChannel channel = ctx.getChannel();
		final var user = ctx.getAuthor();
		if (!channel.getParent().getName().toLowerCase().contains("support")
				&& !BotUtils.isModerator(ctx.getGuild(), ctx.getMember())
				&& !channel.getTopic().split("\n")[0].toLowerCase().trim().equalsIgnoreCase(user.getId()))
			return;

		channel.sendMessage("Are you sure you want to close this channel?").queue(message -> {
			message.addReaction("✔").queue(yesReaction -> message.addReaction("❌").queue(noReaction -> ctx.getJDA()
					.addEventListener(new HelpReactionEventListener(channel, message.getIdLong()))));
			ctx.getMessage().delete().queue();
		});
	}
}
