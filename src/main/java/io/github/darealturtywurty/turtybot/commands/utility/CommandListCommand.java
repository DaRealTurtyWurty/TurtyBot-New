package io.github.darealturtywurty.turtybot.commands.utility;

import java.util.List;
import java.util.Objects;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CommandManager;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import io.github.darealturtywurty.turtybot.util.core.BotUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd(needsManager = true)
public class CommandListCommand implements GuildCommand {

    private final CommandManager commandManager;

    public CommandListCommand(final CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.UTILITY;
    }

    @Override
    public String getDescription() {
        return "Gets the list of commands.";
    }

    @Override
    public String getName() {
        return "commands";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(new OptionData(OptionType.STRING, "category", "Category of commands", false));
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        final String prefix = "/";
        final OptionMapping categoryOption = ctx.getEvent().getOption("category");
        if (categoryOption == null) {
            final var embed = new EmbedBuilder().setTitle("My list of commands!")
                    .setColor(BotUtils.generateRandomPastelColor());
            for (final var category : CommandCategory.values()) {
                embed.addField(category.emoji + " " + category.name,
                        "`" + prefix + "commands " + category.name.toLowerCase() + "`", true);
            }

            ctx.getEvent().deferReply().addEmbeds(embed.build()).mentionRepliedUser(false).queue();
            return;
        }

        final var categoryStr = categoryOption.getAsString();
        final var category = CommandCategory.byName(categoryStr).isPresent()
                ? CommandCategory.byName(categoryStr).get()
                : null;
        if (category != null) {
            final var embed = new EmbedBuilder();
            embed.setTitle("Commands in category: " + category);

            final var strBuilder = new StringBuilder();
            this.commandManager.commands.stream().filter(cmd -> cmd.getCategory() == category)
                    .filter(Objects::nonNull).sorted((cmd1, cmd2) -> cmd1.getName().compareTo(cmd2.getName()))
                    .forEach(cmd -> strBuilder.append("`" + cmd.getName() + "`, "));
            if (strBuilder.lastIndexOf(",") != -1) {
                strBuilder.deleteCharAt(strBuilder.lastIndexOf(","));
            }

            embed.setDescription(strBuilder.toString());
            embed.setColor(BotUtils.generateRandomPastelColor());
            ctx.getEvent().deferReply(true).addEmbeds(embed.build()).mentionRepliedUser(false).queue();
        } else {
            ctx.getEvent().deferReply(true)
                    .setContent(
                            "You must supply a valid category! Use `/commands` for the list of categories.")
                    .queue();
        }
    }

    @Override
    public boolean productionReady() {
        return true;
    }
}
