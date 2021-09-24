package io.github.darealturtywurty.turtybot.commands.moderation;

import java.util.List;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import io.github.darealturtywurty.turtybot.util.core.WarnUtils;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class RemoveWarnCommand implements GuildCommand {

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.MODERATION;
    }

    @Override
    public String getDescription() {
        return "Removes the warn with a specific UUID from a user.";
    }

    @Override
    public String getName() {
        return "removewarn";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(new OptionData(OptionType.STRING, "uuid", "UUID of the warn", true));
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        final String uuid = ctx.getEvent().getOption("uuid").getAsString();

        final boolean complete = WarnUtils.removeWarnByUUID(ctx.getGuild(), ctx.getMember(), uuid);
        if (!complete) {
            ctx.getEvent().deferReply(true).setContent("You must provide a valid UUID.")
                    .mentionRepliedUser(false).queue();
            return;
        }

        ctx.getEvent().deferReply(true)
                .setContent("Warn with UUID: " + uuid + " has successfully been removed!").queue();
    }

    @Override
    public boolean isModeratorOnly() {
        return true;
    }

    @Override
    public boolean productionReady() {
        return true;
    }
}
