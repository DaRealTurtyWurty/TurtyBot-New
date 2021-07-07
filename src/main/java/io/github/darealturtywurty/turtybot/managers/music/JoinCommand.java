package io.github.darealturtywurty.turtybot.managers.music;

import java.util.concurrent.TimeUnit;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;

public class JoinCommand implements IGuildCommand {

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.MUSIC;
	}

	@Override
	public String getDescription() {
		return "Joins the user's current voice channel.";
	}

	@Override
	public String getName() {
		return "joinvc";
	}

	@Override
	public void handle(final CommandContext ctx) {
		if (ctx.getMember().getVoiceState().inVoiceChannel()) {
			ctx.getGuild().getAudioManager().openAudioConnection(ctx.getMember().getVoiceState().getChannel());
			ctx.getMessage().delete().queue();
			return;
		}

		ctx.getMessage().reply("You must be in a voice channel to use this command!").mentionRepliedUser(false)
				.queue(msg -> {
					msg.delete().queueAfter(15, TimeUnit.SECONDS);
					ctx.getMessage().delete().queueAfter(15, TimeUnit.SECONDS);
				});
	}
}
