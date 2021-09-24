package io.github.darealturtywurty.turtybot.managers.help_system;

import java.util.List;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import io.github.darealturtywurty.turtybot.util.core.BotUtils;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.Button;

@RegisterBotCmd
public class CloseChannelCommand implements GuildCommand {

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.UTILITY;
    }

    @Override
    public String getDescription() {
        return "Closes the channel.";
    }

    @Override
    public String getName() {
        return "close";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of();
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        final TextChannel channel = ctx.getChannel();
        final var user = ctx.getAuthor();
        if (channel.getParent() != null && !channel.getParent().getName().toLowerCase().contains("support")
                || !BotUtils.isModerator(ctx.getGuild(), ctx.getMember())
                        && !channel.getTopic().split("\n")[0].toLowerCase().trim()
                                .equalsIgnoreCase(user.getId()))
            return;

        final var confirmButton = Button.danger("confirm-" + ctx.getEvent().getCommandIdLong(), "Confirm");
        final var cancelButton = Button.success("cancel-" + ctx.getEvent().getCommandIdLong(), "Cancel");

        ctx.getEvent().deferReply(true).setContent("Are you sure you want to close this channel?")
                .addActionRow(confirmButton, cancelButton).queue();
    }
}
