package io.github.darealturtywurty.turtybot.commands.moderation;

import java.util.List;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import io.github.darealturtywurty.turtybot.managers.auto_mod.BotResponseListener;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class CleanBotCommand implements GuildCommand {

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.MODERATION;
    }

    @Override
    public String getDescription() {
        return "Cleans the bot responses in the current channel.";
    }

    @Override
    public String getName() {
        return "clean-bot";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(new OptionData(OptionType.INTEGER, "amount", "1-100", false));
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        int amount = 50;
        final OptionMapping amountOption = ctx.getEvent().getOption("amount");
        if (amountOption != null) {
            amount = Math.max(1, Math.min(100, (int) amountOption.getAsLong()));
        }

        BotResponseListener.cleanResponses(ctx.getChannel(), amount);
    }
}
