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
public class PlayCommand implements GuildCommand {

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.MUSIC;
    }

    @Override
    public String getDescription() {
        return "Plays a song.";
    }

    @Override
    public String getName() {
        return "play";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(new OptionData(OptionType.STRING, "music", "The music that you want to play", true));
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        if (ctx.getGuild().getAudioManager().getConnectedChannel() == null) {
            if (ctx.getMember().getVoiceState().inVoiceChannel()) {
                ctx.getGuild().getAudioManager()
                        .openAudioConnection(ctx.getMember().getVoiceState().getChannel());
                ctx.getEvent().deferReply().setContent("I have joined the voice channel!").queue();
            } else {
                ctx.getEvent().deferReply(true)
                        .setContent("You must be in a voice channel to use this command!").queue();
            }
        }

        final boolean success = MusicManager.loadAndPlay(ctx.getChannel(),
                ctx.getEvent().getOption("music").getAsString(), false);
        if (success) {
            ctx.getEvent().deferReply(true).setContent("I have successfully added \""
                    + ctx.getEvent().getOption("music").getAsString() + "\" to the queue!").queue();
        } else {
            ctx.getEvent().deferReply(true).setContent("There was an error adding \""
                    + ctx.getEvent().getOption("music").getAsString() + "\" to the queue!").queue();
        }
    }

    @Override
    public boolean productionReady() {
        return true;
    }
}
