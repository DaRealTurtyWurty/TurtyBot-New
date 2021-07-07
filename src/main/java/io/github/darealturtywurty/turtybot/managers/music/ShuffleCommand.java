package io.github.darealturtywurty.turtybot.managers.music;

import java.util.ArrayList;
import java.util.Collections;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;

public class ShuffleCommand implements IGuildCommand {

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.MUSIC;
	}

	@Override
	public String getDescription() {
		return "Shuffles the music queue.";
	}

	@Override
	public String getName() {
		return "shuffle";
	}

	@Override
	public void handle(final CommandContext ctx) {
		final var trackScheduler = MusicManager.MUSIC_MANAGERS.get(ctx.getGuild().getIdLong()).scheduler;
		final var queue = new ArrayList<AudioTrack>(trackScheduler.getQueue());
		Collections.shuffle(queue);
		trackScheduler.getQueue().clear();
		queue.forEach(trackScheduler::queue);
	}
}
