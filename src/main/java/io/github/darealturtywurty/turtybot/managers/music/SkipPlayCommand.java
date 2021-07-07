package io.github.darealturtywurty.turtybot.managers.music;

import java.util.concurrent.TimeUnit;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;

public class SkipPlayCommand implements IGuildCommand {

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.MUSIC;
	}

	@Override
	public String getDescription() {
		return "Skips the current song and puts the given song at the top of the queue, therefore playing it next.";
	}

	@Override
	public String getName() {
		return "skip-play";
	}

	@Override
	public void handle(final CommandContext ctx) {
		if (ctx.getArgs().length >= 1) {
			MusicManager.loadAndPlay(ctx.getChannel(), String.join(" ", ctx.getArgs()), true);
			ctx.getMessage().delete().queue();
		} else {
			ctx.getMessage().reply("You have not supplied a song to play next, so only the song was skipped.")
					.mentionRepliedUser(false).queue(msg -> {
						msg.delete().queueAfter(15, TimeUnit.SECONDS);
						ctx.getMessage().delete().queueAfter(15, TimeUnit.SECONDS);
					});
		}
		MusicManager.skipTrack(ctx.getChannel());
	}

	@Override
	public boolean isModeratorOnly() {
		return true;
	}
}
