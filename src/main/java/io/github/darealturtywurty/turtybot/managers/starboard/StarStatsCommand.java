package io.github.darealturtywurty.turtybot.managers.starboard;

import java.time.Instant;
import java.util.List;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import io.github.darealturtywurty.turtybot.util.BotUtils;
import io.github.darealturtywurty.turtybot.util.CoreBotUtils;
import io.github.darealturtywurty.turtybot.util.data.GuildInfo;
import io.github.darealturtywurty.turtybot.util.data.StarStats;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class StarStatsCommand implements GuildCommand {

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
    public List<OptionData> getOptions() {
        return List.of(new OptionData(OptionType.USER, "member", "The member to get star stats for.", false));
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        Member member = ctx.getMember();
        final OptionMapping memberOption = ctx.getEvent().getOption("user");
        if (memberOption != null && memberOption.getAsMember() != null) {
            member = memberOption.getAsMember();
        }

        final var guildInfo = getOrCreateGuildInfo(ctx.getGuild());
        var totalStars = 0;
        final StarStats stats = guildInfo.userStarStats.containsKey(member.getIdLong())
                ? guildInfo.userStarStats.get(member.getIdLong())
                : null;

        if (stats != null) {
            stats.retrieveTotalStars(ctx.getGuild());
            totalStars = stats.getTotalStars();
        }

        final var embed = new EmbedBuilder().setColor(BotUtils.generateRandomPastelColor())
                .setTimestamp(Instant.now()).setTitle("Star Stats for: " + member.getEffectiveName())
                .addField("Stars Recieved: ", String.valueOf(totalStars), true)
                .setAuthor(member.getEffectiveName(), null, member.getUser().getEffectiveAvatarUrl());
        ctx.getEvent().deferReply().addEmbeds(embed.build()).mentionRepliedUser(false).queue();
    }
}
