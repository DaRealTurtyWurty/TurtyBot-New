package io.github.darealturtywurty.turtybot.commands.minecraft;

import java.util.List;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class CheckVanillaCommand implements GuildCommand {

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.MINECRAFT;
    }

    @Override
    public String getDescription() {
        return "Check vanilla!";
    }

    @Override
    public String getName() {
        return "check-vanilla";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of();
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        ctx.getEvent().deferReply().setContent("Please check vanilla!"
                + "\n\nIf using Eclipse IDE, you can do this through the shortcut: `Ctrl+Shift+T` and then typing "
                + "the file name of the class you want to view.\n\nIf using IntelliJ IDEA, you can do this by "
                + "pressing `Shift` 4 times and then typing the file name of the class that you want to view.")
                .queue();
    }

    @Override
    public boolean productionReady() {
        return true;
    }
}
