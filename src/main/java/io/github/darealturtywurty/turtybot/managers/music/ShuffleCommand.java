package io.github.darealturtywurty.turtybot.managers.music;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import io.github.darealturtywurty.turtybot.managers.music.core.MusicManager;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class ShuffleCommand implements GuildCommand {

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.MUSIC;
    }

    @Override
    public String getDescription() {
        return "Shuffles the music queue.";
    }

    @Override
    public String getName() {
        return "shuffle";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of();
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        final var trackScheduler = MusicManager.getMusicManager(ctx.getGuild()).scheduler;
        final var queue = new ArrayList<AudioTrack>(trackScheduler.getQueue());
        Collections.shuffle(queue);
        trackScheduler.getQueue().clear();
        queue.forEach(track -> trackScheduler.queue(ctx.getChannel(), track, false));
        ctx.getEvent().deferReply().setContent("I have shuffled the queue!").queue();
    }

    @Override
    public boolean productionReady() {
        return true;
    }
}
