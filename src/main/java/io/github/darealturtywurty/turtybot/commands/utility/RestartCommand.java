package io.github.darealturtywurty.turtybot.commands.utility;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import io.github.darealturtywurty.turtybot.util.BotUtils;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class RestartCommand implements GuildCommand {

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.UTILITY;
    }

    @Override
    public String getDescription() {
        return "Restarts the bot!";
    }

    @Override
    public String getName() {
        return "restart";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of();
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        try {
            ctx.getEvent().reply("Restarting bot!").mentionRepliedUser(false).queue();
            Logger.getGlobal().log(Level.WARNING, "Restarting bot!");
            BotUtils.restartApplication(ctx.getJDA());
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public boolean isBotOwnerOnly() {
        return true;
    }
}
