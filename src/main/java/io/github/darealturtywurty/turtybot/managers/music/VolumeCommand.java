package io.github.darealturtywurty.turtybot.managers.music;

import java.util.List;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import io.github.darealturtywurty.turtybot.managers.music.core.MusicManager;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class VolumeCommand implements GuildCommand {

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.MUSIC;
    }

    @Override
    public String getDescription() {
        return "Sets the volume for the music.";
    }

    @Override
    public String getName() {
        return "volume";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(new OptionData(OptionType.INTEGER, "volume", "The volume to set the music player to.",
                false));
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        final OptionMapping volumeOption = ctx.getEvent().getOption("volume");
        if (volumeOption != null) {
            final int newVolume = (int) ctx.getEvent().getOption("volume").getAsLong();
            MusicManager.getPlayer(ctx.getGuild()).setVolume(newVolume);
            final int volume = MusicManager.getPlayer(ctx.getGuild()).getVolume();
            ctx.getEvent().deferReply().setContent("I have changed the volume to: " + volume).queue();
        } else {
            final int volume = MusicManager.getPlayer(ctx.getGuild()).getVolume();
            ctx.getEvent().deferReply(true).setContent("The current volume is: " + volume).queue();
        }
    }

    @Override
    public boolean isBoosterOnly() {
        return true;
    }
}
