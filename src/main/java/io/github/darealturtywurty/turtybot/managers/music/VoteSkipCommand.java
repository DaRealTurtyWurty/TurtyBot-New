package io.github.darealturtywurty.turtybot.managers.music;

import java.util.List;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import io.github.darealturtywurty.turtybot.managers.music.core.MusicManager;
import io.github.darealturtywurty.turtybot.managers.music.core.VoiceChannelListener;
import io.github.darealturtywurty.turtybot.util.data.Skip;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class VoteSkipCommand implements GuildCommand {

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.MUSIC;
    }

    @Override
    public String getDescription() {
        return "Starts a vote-skip for the current song.";
    }

    @Override
    public String getName() {
        return "voteskip";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of();
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        final var player = MusicManager.getPlayer(ctx.getGuild());
        if (player.getPlayingTrack() != null
                && MusicManager.MUSIC_MANAGERS.get(ctx.getGuild().getIdLong()).scheduler.getQueue()
                        .peek() != null) {
            ctx.getChannel()
                    .sendMessage("A vote-skip has been created. React with ⏭ if you want to skip (0/5).")
                    .queue(msg -> msg.addReaction("⏭").queue());
            VoiceChannelListener.SKIPS.add(new Skip(ctx.getGuild().getIdLong(), ctx.getChannel().getIdLong(),
                    ctx.getChannel().getLatestMessageIdLong()));
        }
    }
}
