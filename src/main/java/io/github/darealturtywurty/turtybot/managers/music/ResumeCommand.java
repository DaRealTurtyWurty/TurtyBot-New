package io.github.darealturtywurty.turtybot.managers.music;

import java.util.List;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;

public class ResumeCommand implements IGuildCommand {

	@Override
	public List<String> getAliases() {
		return List.of("continue");
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.MUSIC;
	}

	@Override
	public String getDescription() {
		return "Resumes the current song, if paused.";
	}

	@Override
	public String getName() {
		return "resume";
	}

	@Override
	public void handle(final CommandContext ctx) {
		MusicManager.MUSIC_MANAGERS.get(ctx.getGuild().getIdLong()).player.setPaused(false);
	}
}
