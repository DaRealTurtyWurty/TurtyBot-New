package io.github.darealturtywurty.turtybot.commands.moderation;

import java.util.Arrays;
import java.util.List;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;
import io.github.darealturtywurty.turtybot.util.BotUtils.WarnUtils;
import net.dv8tion.jda.api.entities.Member;

public class ClearWarnsCommand implements IGuildCommand {

	@Override
	public List<String> getAliases() {
		return Arrays.asList("wipewarns", "cleanwarns");
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.MODERATION;
	}

	@Override
	public String getDescription() {
		return "Clears all the warns from a user.";
	}

	@Override
	public String getName() {
		return "clearwarns";
	}

	@Override
	public void handle(final CommandContext ctx) {
		final var message = ctx.getMessage();
		final var guild = ctx.getGuild();
		if (ctx.getArgs().length < 1) {
			ctx.getMessage().reply("You must supply the user that you want to clear warns.").mentionRepliedUser(false)
					.queue();
			return;
		}

		final String toClearStr = ctx.getArgs()[0];

		Member toClear = null;
		try {
			toClear = message.getMentionedMembers().get(0);
		} catch (final IndexOutOfBoundsException ex) {
			guild.retrieveMemberById(toClearStr).queue();
			toClear = guild.getMemberById(toClearStr);
		}

		if (toClear == null) {
			message.reply("Could not find user: " + toClearStr).mentionRepliedUser(false).queue();
			return;
		}

		if (toClear.getIdLong() == ctx.getAuthor().getIdLong()) {
			message.reply("You cannot clear your own warns!").mentionRepliedUser(false).queue();
			return;
		}

		if (!ctx.getMember().canInteract(toClear)) {
			message.reply("You cannot clear warns for this user!").mentionRepliedUser(false).queue();
			return;
		}

		WarnUtils.clearWarns(ctx.getGuild(), ctx.getMember(), toClear);
		message.delete().queue();
	}

	@Override
	public boolean isModeratorOnly() {
		return true;
	}
}
