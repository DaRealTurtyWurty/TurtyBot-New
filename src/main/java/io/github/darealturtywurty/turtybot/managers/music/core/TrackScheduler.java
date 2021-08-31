package io.github.darealturtywurty.turtybot.managers.music.core;

import java.awt.Color;
import java.time.Instant;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;

public class TrackScheduler extends AudioEventAdapter {

    private final AudioPlayer player;
    private final LinkedBlockingQueue<AudioTrack> queue;

    /**
     * @param player The audio player this scheduler uses
     */
    public TrackScheduler(final AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
    }

    public static long queueLength(final TrackScheduler scheduler) {
        long length = scheduler.queue.stream().map(AudioTrack::getDuration).reduce(Long::sum).orElse(0L);
        if (length > 0) {
            length -= scheduler.player.getPlayingTrack().getDuration()
                    - scheduler.player.getPlayingTrack().getPosition();
            length -= scheduler.queue.stream().reduce((first, second) -> second).get().getDuration();
        }

        return length;
    }

    public MessageEmbed addedQueue(final AudioTrack track) {
        final var embed = new EmbedBuilder();
        embed.setColor(Color.BLUE);
        embed.setTimestamp(Instant.now());
        embed.setDescription(
                "Added [" + track.getInfo().title + "](" + track.getInfo().uri + ") to the queue.");
        return embed.build();
    }

    public LinkedBlockingQueue<AudioTrack> getQueue() {
        return this.queue;
    }

    /**
     * Start the next track, stopping the current one if it is playing.
     */
    public void nextTrack() {
        this.player.startTrack(this.queue.poll(), false);
    }

    @Override
    public void onTrackEnd(final AudioPlayer player, final AudioTrack track,
            final AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            nextTrack();
        }
    }

    public MessageEmbed playingEmbed(final AudioTrack track) {
        final var embed = new EmbedBuilder();
        embed.setColor(Color.BLUE);
        embed.setTimestamp(Instant.now());
        embed.setDescription("Now Playing: [" + track.getInfo().title + "](" + track.getInfo().uri + ").");
        return embed.build();
    }

    /**
     * Add the next track to queue or play right away if nothing is in the queue.
     *
     * @param track The track to play or add to queue.
     */
    public boolean queue(final TextChannel channel, final AudioTrack track, final boolean announce) {
        if (!this.player.startTrack(track, true)) {
            if (announce) {
                channel.sendMessageEmbeds(addedQueue(track))
                        .queue(msg -> msg.delete().queueAfter(queueLength(this), TimeUnit.MILLISECONDS));
            }
            return this.queue.offer(track);
        }

        if (announce) {
            channel.sendMessageEmbeds(playingEmbed(track)).queue();
        }

        return true;
    }
}
