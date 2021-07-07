package io.github.darealturtywurty.turtybot.managers.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

public class GuildMusicManager {

	public final AudioPlayer player;

	public final TrackScheduler scheduler;

	/**
	 * Creates a player and a track scheduler.
	 *
	 * @param manager Audio player manager to use for creating the player.
	 */
	public GuildMusicManager(final AudioPlayerManager manager) {
		this.player = manager.createPlayer();
		this.scheduler = new TrackScheduler(this.player);
		this.player.addListener(this.scheduler);
	}

	/**
	 * @return Wrapper around AudioPlayer to use it as an AudioSendHandler.
	 */
	public AudioPlayerSendHandler getSendHandler() {
		return new AudioPlayerSendHandler(this.player);
	}
}
