package io.github.darealturtywurty.turtybot.commands.moderation;

import java.util.List;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import io.github.darealturtywurty.turtybot.util.BotUtils;
import io.github.darealturtywurty.turtybot.util.BotUtils.WarnUtils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class ClearWarnsCommand implements GuildCommand {

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.MODERATION;
    }

    @Override
    public String getDescription() {
        return "Clears all the warns from a user.";
    }

    @Override
    public String getName() {
        return "clearwarns";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(new OptionData(OptionType.USER, "user", "The user to clear warns.", true));
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        final var guild = ctx.getGuild();
        final Member member = ctx.getEvent().getOption("user").getAsMember();

        if (member == null || !BotUtils.isModerator(guild, ctx.getMember())
                || !ctx.getMember().canInteract(member)) {
            ctx.getEvent().deferReply(true).setContent("You cannot clear warns for this user!")
                    .mentionRepliedUser(false).queue();
            return;
        }

        WarnUtils.clearWarns(ctx.getGuild(), ctx.getMember(), member);
    }

    @Override
    public boolean isModeratorOnly() {
        return true;
    }
}
