package io.github.darealturtywurty.turtybot.commands.fun;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import io.github.darealturtywurty.turtybot.TurtyBot;
import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import io.github.darealturtywurty.turtybot.util.Constants;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class InternetRuleCommand implements GuildCommand {

    protected static final List<String> RULES = new ArrayList<>();

    public InternetRuleCommand() {
        if (RULES.isEmpty()) {
            final InputStream stream = TurtyBot.class.getResourceAsStream("/rules_of_the_internet.txt");
            try {
                final var reader = new BufferedReader(new InputStreamReader(stream));
                if (reader.ready()) {
                    reader.lines().forEach(RULES::add);
                }
            } catch (final Exception e) {
                Constants.LOGGER.log(Level.WARNING, "There has been an issue parsing file: {0}", stream);
            }
        }
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.FUN;
    }

    @Override
    public String getDescription() {
        return "Gets a rule from The Rules of The Internet.";
    }

    @Override
    public String getName() {
        return "erule";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(new OptionData(OptionType.INTEGER, "rule_number", "Rule Number", true));
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        int number = 0;
        try {
            number = Integer.parseInt(ctx.getEvent().getOption("rule_number").getAsString());
            if (number < 1) {
                ctx.getEvent().deferReply().setContent("You must specify the rule number! (1-100)")
                        .mentionRepliedUser(false).queue();
                return;
            }

            if (number > RULES.size()) {
                ctx.getEvent().deferReply().setContent("You must specify the rule number! (1-100)")
                        .mentionRepliedUser(false).queue();
                return;
            }
        } catch (final NumberFormatException ex) {
            number = 0;
        }

        ctx.getEvent().deferReply().setContent(RULES.get(number - 1)).mentionRepliedUser(false).queue();
    }

    @Override
    public boolean productionReady() {
        return true;
    }
}
