package io.github.darealturtywurty.turtybot.managers.music;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;

public class SkipCommand implements IGuildCommand {

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.MUSIC;
	}

	@Override
	public String getDescription() {
		return "Skips the currently playing song!";
	}

	@Override
	public String getName() {
		return "skip";
	}

	@Override
	public void handle(final CommandContext ctx) {
		MusicManager.skipTrack(ctx.getChannel());
	}

	@Override
	public boolean isModeratorOnly() {
		return true;
	}
}
