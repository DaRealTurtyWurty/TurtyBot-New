package io.github.darealturtywurty.turtybot.managers.music;

import java.util.List;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;

public class VoteSkipCommand implements IGuildCommand {

	@Override
	public List<String> getAliases() {
		return List.of("vskip", "vote-skip", "vote_skip");
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.MUSIC;
	}

	@Override
	public String getDescription() {
		return "Starts a vote-skip for the current song.";
	}

	@Override
	public String getName() {
		return "voteskip";
	}

	@Override
	public void handle(final CommandContext ctx) {
		final var player = MusicManager.getPlayer(ctx.getGuild());
		if (player.getPlayingTrack() != null
				&& MusicManager.MUSIC_MANAGERS.get(ctx.getGuild().getIdLong()).scheduler.getQueue().peek() != null) {
			ctx.getMessage().delete().queue();
			ctx.getChannel().sendMessage("A vote-skip has been created. React with ⏭ if you want to skip (0/5).")
					.queue(msg -> msg.addReaction("⏭").queue());
			VoiceChannelListener.SKIPS
					.add(new Skip(ctx.getGuild().getIdLong(), ctx.getChannel().getIdLong(), ctx.getMessage().getIdLong()));
		}
	}
}
