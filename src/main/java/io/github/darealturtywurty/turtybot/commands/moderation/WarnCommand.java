package io.github.darealturtywurty.turtybot.commands.moderation;

import java.util.List;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import io.github.darealturtywurty.turtybot.util.BotUtils;
import io.github.darealturtywurty.turtybot.util.BotUtils.WarnUtils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class WarnCommand implements GuildCommand {

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.MODERATION;
    }

    @Override
    public String getDescription() {
        return "Warns a user.";
    }

    @Override
    public String getName() {
        return "warn";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(new OptionData(OptionType.USER, "user", "User to warn", true),
                new OptionData(OptionType.STRING, "reason", "Reason to warn", false));
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        final var guild = ctx.getGuild();
        final Member toWarn = ctx.getEvent().getOption("user").getAsMember();
        final OptionMapping reasonOption = ctx.getEvent().getOption("reason");
        final String reason = reasonOption == null ? "Unspecified" : reasonOption.getAsString();
        if (toWarn == null || !ctx.getMember().canInteract(toWarn)
                || !BotUtils.isModerator(guild, ctx.getMember())) {
            ctx.getEvent().deferReply(true).setContent("You do not have permission to warn this user.")
                    .mentionRepliedUser(false).queue();
            return;
        }

        if (toWarn.getIdLong() == ctx.getMember().getIdLong()) {
            ctx.getEvent().deferReply(true).setContent("You cannot warn yourself.").mentionRepliedUser(false)
                    .queue();
            return;
        }

        final var userWarns = WarnUtils.getUserWarns(ctx.getGuild(), toWarn);
        if (userWarns != null) {
            userWarns.addWarn(ctx.getGuild(), ctx.getMember(), reason);
        }
    }

    @Override
    public boolean isModeratorOnly() {
        return true;
    }
}
