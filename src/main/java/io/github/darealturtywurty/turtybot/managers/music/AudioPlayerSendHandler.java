package io.github.darealturtywurty.turtybot.managers.music;

import java.nio.ByteBuffer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;

import net.dv8tion.jda.api.audio.AudioSendHandler;

public class AudioPlayerSendHandler implements AudioSendHandler {

	private final AudioPlayer audioPlayer;
	private final ByteBuffer buffer;
	private final MutableAudioFrame frame;

	/**
	 * @param audioPlayer Audio player to wrap.
	 */
	public AudioPlayerSendHandler(final AudioPlayer audioPlayer) {
		this.audioPlayer = audioPlayer;
		this.buffer = ByteBuffer.allocate(1024);
		this.frame = new MutableAudioFrame();
		this.frame.setBuffer(this.buffer);
	}

	@Override
	public boolean canProvide() {
		// returns true if audio was provided
		return this.audioPlayer.provide(this.frame);
	}

	@Override
	public boolean isOpus() {
		return true;
	}

	@Override
	public ByteBuffer provide20MsAudio() {
		// flip to make it a read buffer
		this.buffer.flip();
		return this.buffer;
	}

}
