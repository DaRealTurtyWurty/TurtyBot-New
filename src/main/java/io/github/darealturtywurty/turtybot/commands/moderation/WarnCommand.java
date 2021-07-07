package io.github.darealturtywurty.turtybot.commands.moderation;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;
import io.github.darealturtywurty.turtybot.util.BotUtils.WarnUtils;
import net.dv8tion.jda.api.entities.Member;

public class WarnCommand implements IGuildCommand {

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.MODERATION;
	}

	@Override
	public String getDescription() {
		return "Warns a user.";
	}

	@Override
	public String getName() {
		return "warn";
	}

	@Override
	public void handle(final CommandContext ctx) {
		final var message = ctx.getMessage();
		if (ctx.getArgs().length < 1) {
			message.reply("You must specify the user that you want to warn!").mentionRepliedUser(false).queue();
			return;
		}

		final String user = ctx.getArgs()[0];
		Member toWarn = null;

		try {
			toWarn = message.getMentionedMembers().get(0);
		} catch (final IndexOutOfBoundsException ex) {
			ctx.getGuild().retrieveMemberById(user).queue();
			toWarn = ctx.getGuild().getMemberById(user);
		}

		if (toWarn == null) {
			message.reply("Could not find user: " + user).mentionRepliedUser(false).queue();
			return;
		}

		if (toWarn.getIdLong() == ctx.getAuthor().getIdLong()) {
			message.reply("You cannot warn yourself!").mentionRepliedUser(false).queue();
			return;
		}

		if (!ctx.getMember().canInteract(toWarn)) {
			message.reply("You cannot warn this user!").mentionRepliedUser(false).queue();
			return;
		}

		var reason = "Unspecified";
		if (ctx.getArgs().length > 1) {
			reason = String.join(" ", ctx.getArgs()).replace(user, "").trim();
		}

		final var userWarns = WarnUtils.getUserWarns(ctx.getGuild(), toWarn);
		if (userWarns != null) {
			userWarns.addWarn(ctx.getGuild(), ctx.getMember(), reason);
		}

		if (ctx.getMessage() != null) {
			ctx.getMessage().delete().queue();
		}
	}

	@Override
	public boolean isModeratorOnly() {
		return true;
	}
}
