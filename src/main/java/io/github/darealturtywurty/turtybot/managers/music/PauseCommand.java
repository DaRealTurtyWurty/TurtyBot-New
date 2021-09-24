package io.github.darealturtywurty.turtybot.managers.music;

import java.util.List;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import io.github.darealturtywurty.turtybot.managers.music.core.MusicManager;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class PauseCommand implements GuildCommand {

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.MUSIC;
    }

    @Override
    public String getDescription() {
        return "Pauses the currently playing song.";
    }

    @Override
    public String getName() {
        return "pause";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of();
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        final AudioPlayer player = MusicManager.getPlayer(ctx.getGuild());
        if (!player.isPaused()) {
            player.setPaused(true);
            ctx.getEvent().deferReply().setContent("I have paused the music player!")
                    .mentionRepliedUser(false).queue();
            return;
        }

        ctx.getEvent().deferReply(true)
                .setContent("The music player is already paused. You cannot pause it again!")
                .mentionRepliedUser(false).queue();
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
