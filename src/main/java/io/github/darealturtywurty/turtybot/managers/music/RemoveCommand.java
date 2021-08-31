package io.github.darealturtywurty.turtybot.managers.music;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import io.github.darealturtywurty.turtybot.managers.music.core.MusicManager;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class RemoveCommand implements GuildCommand {

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
    public List<OptionData> getOptions() {
        return List.of(new OptionData(OptionType.STRING, "to_remove",
                "The name or index of what you want to remove from the queue.", true));
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        final String toRemoveStr = ctx.getEvent().getOption("to_remove").getAsString();
        final var scheduler = MusicManager.MUSIC_MANAGERS.get(ctx.getGuild().getIdLong()).scheduler;
        final var tracks = new ArrayList<AudioTrack>(scheduler.getQueue());
        scheduler.getQueue().clear();
        try {
            final int toRemove = Integer.parseInt(toRemoveStr);
            if (toRemove < 0 || toRemove > tracks.size()) {
                ctx.getEvent().deferReply(true).setContent("No track was found in the queue at this index!")
                        .mentionRepliedUser(false).queue();
            } else {
                tracks.remove(toRemove);
                ctx.getEvent().deferReply(true)
                        .setContent("Successfully removed track at index: '" + toRemove + "' from the queue!")
                        .mentionRepliedUser(false).queue();
            }

        } catch (final NumberFormatException ex) {
            final List<AudioTrack> tracksFound = tracks.stream()
                    .filter(track -> track.getInfo().title.trim().equalsIgnoreCase(toRemoveStr.trim()))
                    .collect(Collectors.toList());
            if (!tracksFound.isEmpty()) {
                tracks.remove(tracksFound.get(0));
                ctx.getEvent().deferReply(true).setContent(
                        "Successfully removed \"" + tracksFound.get(0).getInfo().title + "\" from the queue!")
                        .mentionRepliedUser(false).queue();
            } else {
                ctx.getEvent().deferReply(true)
                        .setContent("Unable to find a track in the queue with the specified name!")
                        .mentionRepliedUser(false).queue();
            }
        }
        tracks.forEach(track -> scheduler.queue(ctx.getChannel(), track, false));
    }

    @Override
    public boolean isModeratorOnly() {
        return true;
    }
}
