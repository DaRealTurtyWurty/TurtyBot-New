package io.github.darealturtywurty.turtybot.commands.moderation;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;
import io.github.darealturtywurty.turtybot.util.BotUtils.WarnUtils;
import io.github.darealturtywurty.turtybot.util.Constants;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;

public class WarningsCommand implements IGuildCommand {

	@Override
	public List<String> getAliases() {
		return Arrays.asList("history", "warns", "warnhistory", "allwarns");
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.MODERATION;
	}

	@Override
	public String getDescription() {
		return "Lists all the warnings for a user.";
	}

	@Override
	public String getName() {
		return "warnings";
	}

	@Override
	public void handle(final CommandContext ctx) {
		final var message = ctx.getMessage();
		final var guild = ctx.getGuild();
		Member toGet = null;
		if (ctx.getArgs().length >= 1) {
			final String toGetStr = ctx.getArgs()[0];

			try {
				toGet = message.getMentionedMembers().get(0);
			} catch (final IndexOutOfBoundsException ex) {
				guild.retrieveMemberById(toGetStr).queue();
				toGet = guild.getMemberById(toGetStr);
			}

			if (toGet == null) {
				message.reply("Could not find user: " + toGetStr).mentionRepliedUser(false).queue();
				return;
			}
		} else {
			toGet = guild.getMemberById(message.getAuthor().getIdLong());
		}

		final var userWarns = WarnUtils.getUserWarns(guild, toGet);
		final var warnsEmbed = new EmbedBuilder().setColor(toGet.getColorRaw())
				.setTitle("Warnings for: " + toGet.getEffectiveName())
				.setDescription(toGet.getEffectiveName() + " has " + userWarns.getNumberWarns() + " warnings!")
				.setTimestamp(Instant.now());
		final var counter = new AtomicInteger(1);
		userWarns.warns.forEach((uuid, warnInfo) -> warnsEmbed.addField(
				counter.getAndIncrement() + ".", "**UUID:** " + uuid.toString() + "\n**Warned By (ID):** " + warnInfo.left
						+ "\n**Date:** " + Constants.DATE_FORMAT.format(warnInfo.middle) + "\n**Reason:** " + warnInfo.right,
				false));

		message.replyEmbeds(warnsEmbed.build()).mentionRepliedUser(false).queue();
	}
}
