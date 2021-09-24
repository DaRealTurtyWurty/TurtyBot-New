package io.github.darealturtywurty.turtybot.commands.fun;

import java.util.List;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import io.github.darealturtywurty.turtybot.util.Constants;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
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
        return List.of(
                new OptionData(OptionType.STRING, "choice", "Whether you are choosing heads or tails", false)
                        .addChoice("heads", "heads").addChoice("tails", "tails"));
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        final OptionMapping choice = ctx.getEvent().getOption("choice");
        if (choice == null) {
            if (Constants.RANDOM.nextInt(1000) == 69) {
                ctx.getEvent().deferReply()
                        .setContent("It landed on it's side. It was neither heads or tails! 😔").queue();
            } else {
                ctx.getEvent().deferReply()
                        .setContent(
                                "It was: " + (Constants.RANDOM.nextBoolean() ? "Heads 🗣" : "Tails 🐍") + "!")
                        .queue();
            }
        } else {
            String choiceStr = choice.getAsString();
            if (!choiceStr.contains("head") && !choiceStr.contains("tail") && !choiceStr.contains("side")) {
                ctx.getEvent().deferReply(true).setContent("You must supply either `heads` or `tails`!")
                        .queue();
                return;
            }

            if (choiceStr.contains("head")) {
                choiceStr = "heads";
            } else if (choiceStr.contains("tail")) {
                choiceStr = "tails";
            } else {
                choiceStr = "side";
            }

            String botChoice = "";
            if (Constants.RANDOM.nextInt(1000) == 69) {
                botChoice = "side";
            } else {
                botChoice = Constants.RANDOM.nextBoolean() ? "heads" : "tails";
            }

            String reply = "";
            if (botChoice.equalsIgnoreCase(choiceStr)) {
                if (choiceStr.contains("head")) {
                    reply = "You were correct! It was Heads 🗣.";
                } else if (choiceStr.contains("tail")) {
                    reply = "You were correct! It was Tails 🐍.";
                } else {
                    reply = "You were correct! It landed on it's side 😲.";
                }
            } else if (botChoice.contains("head")) {
                reply = "You were incorrect! It was Heads 🗣.";
            } else if (botChoice.contains("tail")) {
                reply = "You were incorrect! It was Tails 🐍.";
            } else {
                reply = "You were incorrect! It landed on it's side 😲.";
            }

            ctx.getEvent().deferReply().setContent("You chose `" + choiceStr + "`. " + reply).queue();
        }
    }

    @Override
    public boolean productionReady() {
        return true;
    }
}
