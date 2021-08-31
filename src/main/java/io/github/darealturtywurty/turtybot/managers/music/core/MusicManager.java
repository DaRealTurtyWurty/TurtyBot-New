package io.github.darealturtywurty.turtybot.managers.music.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

public final class MusicManager {

    private static final AudioPlayerManager PLAYER_MANAGER = new DefaultAudioPlayerManager();

    public static final Map<Long, GuildMusicManager> MUSIC_MANAGERS = new HashMap<>();

    private MusicManager() {
    }

    public static AudioPlayer getPlayer(final Guild guild) {
        return MUSIC_MANAGERS.get(guild.getIdLong()).player;
    }

    public static boolean loadAndPlay(final TextChannel channel, final String trackUrl, final boolean first) {
        final GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());

        PLAYER_MANAGER.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void loadFailed(final FriendlyException exception) {
                channel.sendMessage("Could not play: " + exception.getMessage()).queue();
            }

            @Override
            public void noMatches() {
                if (!trackUrl.contains("ytsearch:")) {
                    loadAndPlay(channel, "ytsearch:" + trackUrl, first);
                } else {
                    channel.sendMessage("Nothing found by " + trackUrl)
                            .queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
                }
            }

            @Override
            public void playlistLoaded(final AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getSelectedTrack();

                if (!trackUrl.contains("ytsearch:") && firstTrack == null) {
                    final var tracks = new ArrayList<AudioTrack>(playlist.getTracks());
                    play(channel, musicManager, tracks.get(0), first);
                    tracks.remove(0);
                    tracks.forEach(track -> musicManager.scheduler.queue(channel, track, true));
                    channel.sendMessage("Adding to queue: " + playlist.getName() + "("
                            + playlist.getTracks().size() + ") videos.").queue();
                    return;
                }

                if (firstTrack == null) {
                    firstTrack = playlist.getTracks().get(0);
                }
                play(channel, musicManager, firstTrack, first);
            }

            @Override
            public void trackLoaded(final AudioTrack track) {
                channel.sendMessage("Adding to queue " + track.getInfo().title).queue();

                play(channel, musicManager, track, first);
            }
        });

        return musicManager.player.getPlayingTrack() != null;
    }

    public static void register() {
        AudioSourceManagers.registerRemoteSources(PLAYER_MANAGER);
        AudioSourceManagers.registerLocalSource(PLAYER_MANAGER);
    }

    public static void skipTrack(final TextChannel channel) {
        final GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        musicManager.scheduler.nextTrack();

        if (!musicManager.scheduler.getQueue().isEmpty()) {
            channel.sendMessage("Skipped to next track.").queue();
        }
    }

    private static VoiceChannel connectToVoiceChannel(final AudioManager audioManager) {
        if (!audioManager.isConnected()) {
            final var chosenChannel = new AtomicReference<VoiceChannel>();
            for (final VoiceChannel voiceChannel : audioManager.getGuild().getVoiceChannels()) {
                if (chosenChannel.get() != null
                        && voiceChannel.getMembers().size() > chosenChannel.get().getMembers().size()) {
                    chosenChannel.set(voiceChannel);
                }
                break;
            }

            if (chosenChannel.get() != null) {
                audioManager.openAudioConnection(chosenChannel.get());
                return chosenChannel.get();
            }
        }
        return audioManager.getConnectedChannel();
    }

    private static synchronized GuildMusicManager getGuildAudioPlayer(final Guild guild) {
        final var guildId = Long.parseLong(guild.getId());
        GuildMusicManager musicManager = MUSIC_MANAGERS.get(guildId);

        if (musicManager == null) {
            musicManager = new GuildMusicManager(PLAYER_MANAGER);
            MUSIC_MANAGERS.put(guildId, musicManager);
        }

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }

    private static void play(final TextChannel channel, final GuildMusicManager musicManager,
            final AudioTrack track, final boolean first) {
        final var vc = connectToVoiceChannel(channel.getGuild().getAudioManager());
        if (vc != null) {
            if (first) {
                final var queue = new LinkedBlockingQueue<AudioTrack>(musicManager.scheduler.getQueue());
                musicManager.scheduler.getQueue().clear();
                musicManager.scheduler.queue(channel, track, true);
                queue.forEach(item -> musicManager.scheduler.queue(channel, item, true));
                return;
            }
            musicManager.scheduler.queue(channel, track, true);
        } else {
            channel.sendMessage(
                    "Unable to play: \"" + track + "\" because there is no voice channel to join!")
                    .queue(msg -> msg.delete().queueAfter(15, TimeUnit.SECONDS));
        }
    }
}
