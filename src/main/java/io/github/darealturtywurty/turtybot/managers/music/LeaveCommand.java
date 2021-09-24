package io.github.darealturtywurty.turtybot.managers.music;

import java.util.List;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import io.github.darealturtywurty.turtybot.managers.music.core.MusicManager;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class LeaveCommand implements GuildCommand {

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
    public List<OptionData> getOptions() {
        return List.of();
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        if (ctx.getGuild().getAudioManager().isConnected()) {
            ctx.getGuild().getAudioManager().closeAudioConnection();
            MusicManager.getMusicManager(ctx.getGuild()).scheduler.getQueue().clear();
            ctx.getEvent().deferReply().setContent("I have left the voice channel. 😢").queue();
            return;
        }

        ctx.getEvent().deferReply(true).setContent("I must be in a voice channel to be able to leave!")
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
