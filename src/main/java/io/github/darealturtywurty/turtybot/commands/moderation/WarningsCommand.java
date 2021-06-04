package io.github.darealturtywurty.turtybot.commands.moderation;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;
import io.github.darealturtywurty.turtybot.util.BotUtils.WarnUtils;
import io.github.darealturtywurty.turtybot.util.Constants;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;

public class WarningsCommand implements IGuildCommand {

	@Override
	public void handle(CommandContext ctx) {
		var message = ctx.getMessage();
		var guild = ctx.getGuild();
		if (ctx.getArgs().length < 1) {
			ctx.getMessage().reply("You must supply the user that you want to get warnings from.").mentionRepliedUser(false)
					.queue();
			return;
		}

		String toGetStr = ctx.getArgs()[0];

		Member toGet = null;
		try {
			toGet = message.getMentionedMembers().get(0);
		} catch (IndexOutOfBoundsException ex) {
			guild.retrieveMemberById(toGetStr).queue();
			toGet = guild.getMemberById(toGetStr);
		}

		if (toGet == null) {
			message.reply("Could not find user: " + toGetStr).mentionRepliedUser(false).queue();
			return;
		}

		var userWarns = WarnUtils.getUserWarns(guild, toGet);
		var warnsEmbed = new EmbedBuilder().setColor(toGet.getColorRaw())
				.setTitle("Warnings for: " + toGet.getEffectiveName())
				.setDescription(toGet.getEffectiveName() + " has " + userWarns.getNumberWarns() + " warnings!")
				.setTimestamp(Instant.now());
		userWarns.warns
				.forEach((uuid, warnInfo) -> warnsEmbed.addField("UUID:",
						uuid.toString() + "\n\n**Warned By (ID):**\n" + warnInfo.left + "\n\n**Date:**\n"
								+ Constants.DATE_FORMAT.format(warnInfo.middle) + "\n\n**Reason:**\n" + warnInfo.right,
						false));

		message.reply(warnsEmbed.build()).mentionRepliedUser(false).queue();
	}

	@Override
	public String getName() {
		return "warnings";
	}

	@Override
	public List<String> getAliases() {
		return Arrays.asList("history", "warns", "warnhistory", "allwarns");
	}

	@Override
	public String getDescription() {
		return "Lists all the warnings for a user.";
	}

}
