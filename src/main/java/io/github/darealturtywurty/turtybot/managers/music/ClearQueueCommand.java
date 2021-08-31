package io.github.darealturtywurty.turtybot.managers.music;

import java.util.List;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import io.github.darealturtywurty.turtybot.managers.music.core.MusicManager;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class ClearQueueCommand implements GuildCommand {

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.MUSIC;
    }

    @Override
    public String getDescription() {
        return "Clears the current music queue!";
    }

    @Override
    public String getName() {
        return "clearqueue";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of();
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        MusicManager.MUSIC_MANAGERS.get(ctx.getGuild().getIdLong()).scheduler.getQueue().clear();
        ctx.getGuild().getAudioManager().closeAudioConnection();
        ctx.getEvent().deferReply().setContent("I have cleared the queue!").queue();
    }

    @Override
    public boolean isModeratorOnly() {
        return true;
    }
}
