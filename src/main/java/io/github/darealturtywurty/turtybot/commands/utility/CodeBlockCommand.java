package io.github.darealturtywurty.turtybot.commands.utility;

import java.util.List;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class CodeBlockCommand implements GuildCommand {

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.UTILITY;
    }

    @Override
    public String getDescription() {
        return "Shows how to format your code into code blocks.";
    }

    @Override
    public String getName() {
        return "code-block";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of();
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        ctx.getEvent().deferReply().setContent(
                "In order to correctly format your code, you should use \\`\\`\\`<language>\n//Code Here\n\\`\\`\\`")
                .queue();
    }

    @Override
    public boolean productionReady() {
        return true;
    }
}
