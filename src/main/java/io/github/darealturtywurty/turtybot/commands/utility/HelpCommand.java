package io.github.darealturtywurty.turtybot.commands.utility;

import java.util.List;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CommandManager;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import io.github.darealturtywurty.turtybot.util.BotUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class HelpCommand implements GuildCommand {

    private final CommandManager commandManager;

    public HelpCommand(final CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.UTILITY;
    }

    @Override
    public String getDescription() {
        return "Gets the information about the current command!";
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.STRING, "command", "Command to receive information about", false));
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        final String prefix = "/";
        final OptionMapping commandOption = ctx.getEvent().getOption("command");
        if (commandOption == null) {
            ctx.getEvent().deferReply(true)
                    .setContent("To get a list of commands, use `" + prefix + "commands`.\n").queue();
            return;
        }

        final GuildCommand command = this.commandManager.getCommand(commandOption.getAsString());
        if (command == null) {
            ctx.getEvent().deferReply(true).setContent("No command found for " + commandOption.getAsString())
                    .queue();
            return;
        }

        final var embed = new EmbedBuilder();
        embed.setTitle("Information about command: " + command.getName());
        embed.setDescription(command.getDescription());
        embed.addField("Is owner only?", BotUtils.trueFalseToYesNo(command.isBotOwnerOnly()), false);
        embed.addField("Is NSFW?", BotUtils.trueFalseToYesNo(command.isNSFW()), false);
        embed.addField("Is moderator only?", BotUtils.trueFalseToYesNo(command.isModeratorOnly()), false);
        embed.addField("Is server booster only?", BotUtils.trueFalseToYesNo(command.isBoosterOnly()), false);
        embed.addField("Is developer mode only?", BotUtils.trueFalseToYesNo(command.isDevelopmentOnly()),
                false);
        embed.setColor(BotUtils.generateRandomPastelColor());
        ctx.getEvent().deferReply(true).addEmbeds(embed.build()).mentionRepliedUser(false).queue();
    }
}
