package io.github.darealturtywurty.turtybot.managers.music;

import java.util.concurrent.TimeUnit;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;

public class PauseCommand implements IGuildCommand {

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.MUSIC;
	}

	@Override
	public String getDescription() {
		return "Pauses the currently playing song.";
	}

	@Override
	public String getName() {
		return "pause";
	}

	@Override
	public void handle(final CommandContext ctx) {
		final AudioPlayer player = MusicManager.getPlayer(ctx.getGuild());
		if (!player.isPaused()) {
			player.setPaused(true);
			ctx.getMessage().reply("I have paused the music player!").mentionRepliedUser(false).queue(msg -> {
				ctx.getMessage().delete().queueAfter(15, TimeUnit.SECONDS);
				msg.delete().queueAfter(15, TimeUnit.SECONDS);
			});
			return;
		}

		ctx.getMessage().reply("The music player is already paused. You cannot pause it again!").mentionRepliedUser(false)
				.queue(msg -> {
					ctx.getMessage().delete().queueAfter(15, TimeUnit.SECONDS);
					msg.delete().queueAfter(15, TimeUnit.SECONDS);
				});
	}

	@Override
	public boolean isModeratorOnly() {
		return true;
	}
}
