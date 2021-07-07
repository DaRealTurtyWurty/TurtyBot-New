package io.github.darealturtywurty.turtybot.managers.music;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;
import io.github.darealturtywurty.turtybot.util.BotUtils;

public class NowPlayingCommand implements IGuildCommand {

	@Override
	public List<String> getAliases() {
		return List.of("np", "playing", "current");
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.MUSIC;
	}

	@Override
	public String getDescription() {
		return "Gets the currently playing song (if there is one).";
	}

	@Override
	public String getName() {
		return "nowplaying";
	}

	@Override
	public void handle(final CommandContext ctx) {
		try {
			ctx.getMessage()
					.reply("Currently Playing: " + MusicManager.getPlayer(ctx.getGuild()).getPlayingTrack().getInfo().title)
					.mentionRepliedUser(false).queue();
		} catch (final Exception ex) {
			ctx.getMessage()
					.reply("I am not currently playing anything. Use `" + BotUtils.getPrefixFromGuild(ctx.getGuild())
							+ "join` in combination with `" + BotUtils.getPrefixFromGuild(ctx.getGuild())
							+ "play <url>` to play something!")
					.mentionRepliedUser(false).queue(msg -> {
						msg.delete().queueAfter(15, TimeUnit.SECONDS);
						ctx.getMessage().delete().queueAfter(15, TimeUnit.SECONDS);
					});
		}
	}
}
