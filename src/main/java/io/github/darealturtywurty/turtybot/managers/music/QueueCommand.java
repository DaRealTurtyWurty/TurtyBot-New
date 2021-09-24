package io.github.darealturtywurty.turtybot.managers.music;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import io.github.darealturtywurty.turtybot.managers.music.core.MusicManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class QueueCommand implements GuildCommand {

    /**
     * Convert a millisecond duration to a string format
     *
     * @param millis A duration to convert to a string form
     * @return A string of the form "X Days Y Hours Z Minutes A Seconds".
     */
    public static String getDurationBreakdown(long millis) {
        if (millis < 0)
            throw new IllegalArgumentException("Duration must be greater than zero!");

        final long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        final long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        final long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        final long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        final var strBuilder = new StringBuilder(64);
        if (days > 0) {
            strBuilder.append(days);
            strBuilder.append(" Days, ");
        }

        if (hours > 0) {
            strBuilder.append(hours);
            strBuilder.append(" Hours, ");
        }

        if (minutes > 0) {
            strBuilder.append(minutes);
            strBuilder.append(" Minutes, ");
        }

        if (seconds > 0) {
            strBuilder.append(seconds);
            strBuilder.append(" Seconds ");
        } else {
            strBuilder.append(millis);
            strBuilder.append(", Milliseconds");
        }

        return strBuilder.toString();
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.MUSIC;
    }

    @Override
    public String getDescription() {
        return "Retreives the music queue from the current guild.";
    }

    @Override
    public String getName() {
        return "queue";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of();
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        final var embed = new EmbedBuilder();
        embed.setTitle("Music Queue for \"" + ctx.getGuild().getName() + "\"");
        embed.setTimestamp(Instant.now());
        embed.setFooter(ctx.getAuthor().getName() + "#" + ctx.getAuthor().getDiscriminator(),
                ctx.getAuthor().getEffectiveAvatarUrl());
        final AudioPlayer player = MusicManager.getPlayer(ctx.getGuild());
        if (player.getPlayingTrack() != null) {
            embed.addField(player.getPlayingTrack().getInfo().title,
                    getDurationBreakdown(player.getPlayingTrack().getInfo().length), false);
        }

        final var count = new AtomicInteger(0);
        MusicManager.getMusicManager(ctx.getGuild()).scheduler.getQueue().forEach(track -> {
            if (count.addAndGet(1) <= 25) {
                final AudioTrackInfo trackInfo = track.getInfo();
                embed.addField(trackInfo.title, getDurationBreakdown(trackInfo.length), false);
            }
        });
        ctx.getEvent().deferReply().addEmbeds(embed.build()).mentionRepliedUser(false).queue();
    }

    @Override
    public boolean productionReady() {
        return true;
    }
}
