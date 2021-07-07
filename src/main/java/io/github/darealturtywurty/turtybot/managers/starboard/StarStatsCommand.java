package io.github.darealturtywurty.turtybot.managers.starboard;

import java.time.Instant;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;
import io.github.darealturtywurty.turtybot.data.GuildInfo;
import io.github.darealturtywurty.turtybot.data.StarStats;
import io.github.darealturtywurty.turtybot.util.BotUtils;
import io.github.darealturtywurty.turtybot.util.BotUtils.CoreBotUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

public class StarStatsCommand implements IGuildCommand {

	private static GuildInfo getOrCreateGuildInfo(final Guild guild) {
		return CoreBotUtils.GUILDS.get(guild) == null ? new GuildInfo(guild) : CoreBotUtils.GUILDS.get(guild);
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.UTILITY;
	}

	@Override
	public String getDescription() {
		return "Gets the starboard statistics for this specified user.";
	}

	@Override
	public String getName() {
		return "starstats";
	}

	@Override
	public void handle(final CommandContext ctx) {
		final var message = ctx.getMessage();

		Member toGet = null;
		if (ctx.getArgs().length < 1) {
			toGet = ctx.getMember();
		} else {
			final String user = ctx.getArgs()[0];

			try {
				toGet = message.getMentionedMembers().get(0);
			} catch (final IndexOutOfBoundsException ex) {
				ctx.getGuild().retrieveMemberById(user).queue();
				toGet = ctx.getGuild().getMemberById(user);
			}

			if (toGet == null) {
				toGet = ctx.getMember();
			}
		}

		final var guildInfo = getOrCreateGuildInfo(ctx.getGuild());
		var totalStars = 0;
		final StarStats stats = guildInfo.userStarStats.containsKey(toGet.getIdLong())
				? guildInfo.userStarStats.get(toGet.getIdLong())
				: null;

		if (stats != null) {
			stats.retrieveTotalStars(ctx.getGuild());
			totalStars = stats.getTotalStars();
		}

		final var embed = new EmbedBuilder().setColor(BotUtils.generateRandomPastelColor()).setTimestamp(Instant.now())
				.setTitle("Star Stats for: " + toGet.getEffectiveName())
				.addField("Stars Recieved: ", String.valueOf(totalStars), true)
				.setAuthor(toGet.getEffectiveName(), null, toGet.getUser().getEffectiveAvatarUrl());
		ctx.getMessage().replyEmbeds(embed.build()).mentionRepliedUser(false).queue();
	}
}
