package io.github.darealturtywurty.turtybot.managers.music;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;

public class ClearQueueCommand implements IGuildCommand {

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.MUSIC;
	}

	@Override
	public String getDescription() {
		return "Clears the current music queue!";
	}

	@Override
	public String getName() {
		return "clearqueue";
	}

	@Override
	public void handle(final CommandContext ctx) {
		MusicManager.MUSIC_MANAGERS.get(ctx.getGuild().getIdLong()).scheduler.getQueue().clear();
		ctx.getGuild().getAudioManager().closeAudioConnection();
		ctx.getMessage().delete().queue();
	}

	@Override
	public boolean isModeratorOnly() {
		return true;
	}
}
