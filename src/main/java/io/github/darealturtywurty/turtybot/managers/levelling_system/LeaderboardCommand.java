package io.github.darealturtywurty.turtybot.managers.levelling_system;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;
import io.github.darealturtywurty.turtybot.util.BotUtils;
import io.github.darealturtywurty.turtybot.util.Constants;
import net.dv8tion.jda.api.EmbedBuilder;

public class LeaderboardCommand implements IGuildCommand {

	@Override
	public List<String> getAliases() {
		return List.of("lb", "top", "scoreboard");
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.UTILITY;
	}

	@Override
	public String getDescription() {
		return "Gets the levelling leaderboard for the current guild!";
	}

	@Override
	public String getName() {
		return "leaderboard";
	}

	@Override
	public void handle(final CommandContext ctx) {
		final var guild = ctx.getGuild();
		final var embed = new EmbedBuilder().setColor(BotUtils.generateRandomPastelColor()).setTimestamp(Instant.now())
				.setAuthor(guild.getName(), null, guild.getIconUrl())
				.setTitle("Levelling Leaderboard for Guild: \"" + guild.getName() + "\"");

		final var rank = new AtomicInteger(1);
		Constants.LEVELLING_MANAGER.getLeaderboard(guild).stream().forEach(doc -> {
			if (rank.get() <= 12) {
				embed.addField(
						"Rank: " + rank.getAndIncrement(), "User: <@" + doc.getLong("ID") + "> Level: "
								+ LevellingManager.getLevelForXP(doc.getInteger("XP")) + " | XP: " + doc.getInteger("XP"),
						false);
			}
		});

		ctx.getMessage().replyEmbeds(embed.build()).mentionRepliedUser(false).queue();
	}
}
