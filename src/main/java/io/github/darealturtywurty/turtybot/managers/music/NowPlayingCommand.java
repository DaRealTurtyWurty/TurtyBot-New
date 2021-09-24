package io.github.darealturtywurty.turtybot.managers.music;

import java.util.List;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import io.github.darealturtywurty.turtybot.managers.music.core.MusicManager;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class NowPlayingCommand implements GuildCommand {

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
    public List<OptionData> getOptions() {
        return List.of();
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        try {
            ctx.getEvent().deferReply()
                    .setContent("Currently Playing: "
                            + MusicManager.getPlayer(ctx.getGuild()).getPlayingTrack().getInfo().title)
                    .mentionRepliedUser(false).queue();
        } catch (final Exception ex) {
            ctx.getEvent().deferReply(true)
                    .setContent("I am not currently playing anything. Use `" + "/"
                            + "join` in combination with `" + "/" + "play <url>` to play something!")
                    .mentionRepliedUser(false).queue();
        }
    }

    @Override
    public boolean productionReady() {
        return true;
    }
}
