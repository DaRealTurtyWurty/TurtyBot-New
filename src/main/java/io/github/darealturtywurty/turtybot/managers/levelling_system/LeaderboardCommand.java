package io.github.darealturtywurty.turtybot.managers.levelling_system;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import io.github.darealturtywurty.turtybot.util.Constants;
import io.github.darealturtywurty.turtybot.util.core.BotUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class LeaderboardCommand implements GuildCommand {

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
    public List<OptionData> getOptions() {
        return List.of();
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        final var guild = ctx.getGuild();
        final var embed = new EmbedBuilder().setColor(BotUtils.generateRandomPastelColor())
                .setTimestamp(Instant.now()).setAuthor(guild.getName(), null, guild.getIconUrl())
                .setTitle("Levelling Leaderboard for Guild: \"" + guild.getName() + "\"");

        final var rank = new AtomicInteger(1);
        Constants.LEVELLING_MANAGER.getLeaderboard(guild).stream().forEach(doc -> {
            if (rank.get() <= 12) {
                embed.addField("Rank: " + rank.getAndIncrement(),
                        "User: <@" + doc.getLong("ID") + "> Level: "
                                + LevellingManager.getLevelForXP(doc.getInteger("XP")) + " | XP: "
                                + doc.getInteger("XP"),
                        false);
            }
        });

        ctx.getEvent().deferReply().addEmbeds(embed.build()).mentionRepliedUser(false).queue();
    }
}
