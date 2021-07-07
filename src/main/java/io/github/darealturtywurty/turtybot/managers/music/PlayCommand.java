package io.github.darealturtywurty.turtybot.managers.music;

import java.util.concurrent.TimeUnit;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;

public class PlayCommand implements IGuildCommand {

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.MUSIC;
	}

	@Override
	public String getDescription() {
		return "Plays a song.";
	}

	@Override
	public String getName() {
		return "play";
	}

	@Override
	public void handle(final CommandContext ctx) {
		if (ctx.getArgs().length >= 1) {
			MusicManager.loadAndPlay(ctx.getChannel(), String.join(" ", ctx.getArgs()), false);
			ctx.getMessage().delete().queue();
		} else {
			ctx.getMessage().reply("You must supply the URL or name of the song that you want to play!")
					.mentionRepliedUser(false).queue(msg -> {
						msg.delete().queueAfter(15, TimeUnit.SECONDS);
						ctx.getMessage().delete().queueAfter(15, TimeUnit.SECONDS);
					});
		}
	}
}
