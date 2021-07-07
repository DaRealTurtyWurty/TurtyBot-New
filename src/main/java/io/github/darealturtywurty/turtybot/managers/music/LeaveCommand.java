package io.github.darealturtywurty.turtybot.managers.music;

import java.util.concurrent.TimeUnit;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;

public class LeaveCommand implements IGuildCommand {

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.MUSIC;
	}

	@Override
	public String getDescription() {
		return "Leaves the voice channel.";
	}

	@Override
	public String getName() {
		return "leave";
	}

	@Override
	public void handle(final CommandContext ctx) {
		if (ctx.getGuild().getAudioManager().isConnected()) {
			ctx.getGuild().getAudioManager().closeAudioConnection();
			MusicManager.MUSIC_MANAGERS.get(ctx.getGuild().getIdLong()).scheduler.getQueue().clear();
			ctx.getMessage().delete().queue();
			return;
		}

		ctx.getMessage().reply("I must be in a voice channel to be able to leave!").mentionRepliedUser(false).queue(msg -> {
			msg.delete().queueAfter(15, TimeUnit.SECONDS);
			ctx.getMessage().delete().queueAfter(15, TimeUnit.SECONDS);
		});
	}

	@Override
	public boolean isModeratorOnly() {
		return true;
	}
}
