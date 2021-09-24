package io.github.darealturtywurty.turtybot.managers.music;

import java.util.List;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import io.github.darealturtywurty.turtybot.managers.music.core.MusicManager;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class SkipPlayCommand implements GuildCommand {

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.MUSIC;
    }

    @Override
    public String getDescription() {
        return "Skips the current song and puts the given song at the top of the queue, therefore playing it next.";
    }

    @Override
    public String getName() {
        return "skip-play";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(new OptionData(OptionType.STRING, "to_play", "The song to play after skipping", true));
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        final String toPlay = ctx.getEvent().getOption("to_play").getAsString();
        MusicManager.loadAndPlay(ctx.getChannel(), toPlay, true);
        MusicManager.skipTrack(ctx.getChannel());
    }

    @Override
    public boolean isModeratorOnly() {
        return true;
    }

    @Override
    public boolean productionReady() {
        return true;
    }
}
