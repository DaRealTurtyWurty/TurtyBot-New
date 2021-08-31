package io.github.darealturtywurty.turtybot.commands.fun;

import java.util.List;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import io.github.darealturtywurty.turtybot.util.Constants;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class CoinFlipCommand implements GuildCommand {

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.FUN;
    }

    @Override
    public String getDescription() {
        return "Flips a virtual coin!";
    }

    @Override
    public String getName() {
        return "coinflip";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of();
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        if (Constants.RANDOM.nextInt(1000) == 0) {
            ctx.getEvent().deferReply()
                    .setContent("It landed on it's side. It was neither heads or tails! 😔")
                    .mentionRepliedUser(false).queue();
        } else {
            ctx.getEvent().deferReply()
                    .setContent("It was: " + (Constants.RANDOM.nextBoolean() ? "Heads 🗣" : "Tails 🐍") + "!")
                    .mentionRepliedUser(false).queue();
        }
    }
}
