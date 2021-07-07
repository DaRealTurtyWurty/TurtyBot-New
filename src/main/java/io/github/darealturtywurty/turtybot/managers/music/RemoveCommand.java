package io.github.darealturtywurty.turtybot.managers.music;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;

public class RemoveCommand implements IGuildCommand {

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.MUSIC;
	}

	@Override
	public String getDescription() {
		return "Removes the song in the queue that is at the specified index or is equal to the specified name.";
	}

	@Override
	public String getName() {
		return "removequeue";
	}

	@Override
	public void handle(final CommandContext ctx) {
		if (ctx.getArgs().length >= 1) {
			final var scheduler = MusicManager.MUSIC_MANAGERS.get(ctx.getGuild().getIdLong()).scheduler;
			final var tracks = new ArrayList<AudioTrack>(scheduler.getQueue());
			scheduler.getQueue().clear();
			try {
				final var toRemove = Integer.parseInt(ctx.getArgs()[0]);
				if (toRemove < 0 || toRemove > tracks.size()) {
					ctx.getMessage().reply("No track was found in the queue at this index!").mentionRepliedUser(false)
							.queue(msg -> {
								msg.delete().queueAfter(15, TimeUnit.SECONDS);
								ctx.getMessage().delete().queueAfter(15, TimeUnit.SECONDS);
							});
				} else {
					tracks.remove(toRemove);
					ctx.getMessage().reply("Successfully removed track at index: '" + toRemove + "' from the queue!")
							.mentionRepliedUser(false).queue(msg -> {
								msg.delete().queueAfter(15, TimeUnit.SECONDS);
								ctx.getMessage().delete().queueAfter(15, TimeUnit.SECONDS);
							});
				}

			} catch (final NumberFormatException ex) {
				final List<AudioTrack> tracksFound = tracks.stream().filter(
						track -> track.getInfo().title.trim().equalsIgnoreCase(String.join(" ", ctx.getArgs()).trim()))
						.collect(Collectors.toList());
				if (!tracksFound.isEmpty()) {
					tracks.remove(tracksFound.get(0));
					ctx.getMessage()
							.reply("Successfully removed \"" + tracksFound.get(0).getInfo().title + "\" from the queue!")
							.mentionRepliedUser(false).queue(msg -> {
								msg.delete().queueAfter(15, TimeUnit.SECONDS);
								ctx.getMessage().delete().queueAfter(15, TimeUnit.SECONDS);
							});
				} else {
					ctx.getMessage().reply("Unable to find a track in the queue with the specified name!")
							.mentionRepliedUser(false).queue(msg -> {
								msg.delete().queueAfter(15, TimeUnit.SECONDS);
								ctx.getMessage().delete().queueAfter(15, TimeUnit.SECONDS);
							});
				}
			}
			tracks.forEach(scheduler::queue);
		} else {
			ctx.getMessage().reply("You must supply the index or name of the track you want to remove from the queue!")
					.mentionRepliedUser(false).queue(msg -> {
						msg.delete().queueAfter(15, TimeUnit.SECONDS);
						ctx.getMessage().delete().queueAfter(15, TimeUnit.SECONDS);
					});
		}
	}

	@Override
	public boolean isModeratorOnly() {
		return true;
	}
}
