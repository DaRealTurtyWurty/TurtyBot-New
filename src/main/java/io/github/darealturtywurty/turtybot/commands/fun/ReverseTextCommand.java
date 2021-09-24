package io.github.darealturtywurty.turtybot.commands.fun;

import java.util.List;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class ReverseTextCommand implements GuildCommand {

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.FUN;
    }

    @Override
    public String getDescription() {
        return "Reverses the input text.";
    }

    @Override
    public String getName() {
        return "reversetext";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(new OptionData(OptionType.STRING, "text", "The text to reverse", true));
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        final String text = ctx.getEvent().getOption("text").getAsString();
        ctx.getEvent().deferReply().setContent(new StringBuilder(text).reverse().toString())
                .mentionRepliedUser(false).queue();
    }

    @Override
    public boolean productionReady() {
        return true;
    }
}
