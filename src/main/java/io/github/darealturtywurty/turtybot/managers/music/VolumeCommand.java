package io.github.darealturtywurty.turtybot.managers.music;

import java.util.concurrent.TimeUnit;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;

public class VolumeCommand implements IGuildCommand {

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.MUSIC;
	}

	@Override
	public String getDescription() {
		return "Sets the volume for the music.";
	}

	@Override
	public String getName() {
		return "volume";
	}

	@Override
	public void handle(final CommandContext ctx) {
		if (ctx.getArgs().length >= 1) {
			try {
				MusicManager.getPlayer(ctx.getGuild()).setVolume(Integer.parseInt(ctx.getArgs()[0]));
			} catch (final NumberFormatException ex) {
				ctx.getMessage().reply("You must enter a valid volume!").mentionRepliedUser(false).queue(msg -> {
					msg.delete().queueAfter(15, TimeUnit.SECONDS);
					ctx.getMessage().delete().queueAfter(15, TimeUnit.SECONDS);
				});
			}
			return;
		}

		ctx.getMessage().reply("You must enter a valid volume!").mentionRepliedUser(false).queue(msg -> {
			msg.delete().queueAfter(15, TimeUnit.SECONDS);
			ctx.getMessage().delete().queueAfter(15, TimeUnit.SECONDS);
		});
	}

	@Override
	public boolean isModeratorOnly() {
		return true;
	}
}
