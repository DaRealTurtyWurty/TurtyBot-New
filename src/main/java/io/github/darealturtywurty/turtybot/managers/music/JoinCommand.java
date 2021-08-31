package io.github.darealturtywurty.turtybot.managers.music;

import java.util.List;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class JoinCommand implements GuildCommand {

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.MUSIC;
    }

    @Override
    public String getDescription() {
        return "Joins the user's current voice channel.";
    }

    @Override
    public String getName() {
        return "joinvc";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of();
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        if (ctx.getMember().getVoiceState().inVoiceChannel()) {
            ctx.getGuild().getAudioManager()
                    .openAudioConnection(ctx.getMember().getVoiceState().getChannel());
            ctx.getEvent().deferReply().setContent("Successfully joined "
                    + ctx.getMember().getVoiceState().getChannel().getAsMention() + "!").queue();
            return;
        }

        ctx.getEvent().deferReply(true).setContent("You must be in a voice channel to use this command!")
                .mentionRepliedUser(false).queue();
    }
}
