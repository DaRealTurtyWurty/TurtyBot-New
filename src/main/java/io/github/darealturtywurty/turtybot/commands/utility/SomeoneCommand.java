package io.github.darealturtywurty.turtybot.commands.utility;

import java.util.List;
import java.util.Optional;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import io.github.darealturtywurty.turtybot.util.Constants;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

// TODO: Fix
@RegisterBotCmd
public class SomeoneCommand implements GuildCommand {

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.UTILITY;
    }

    @Override
    public String getDescription() {
        return "Someone come here!!!";
    }

    @Override
    public String getName() {
        return "someone";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(new OptionData(OptionType.BOOLEAN, "online-only",
                "Whether or not this only targets online users.", true));
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        final boolean onlineOnly = ctx.getEvent().getOption("online-only").getAsBoolean();
        final TextChannel channel = ctx.getChannel();
        final List<Member> members = channel.getMembers();
        if (onlineOnly) {
            final Optional<Member> member = members.stream()
                    .filter(m -> (m.getOnlineStatus() == OnlineStatus.ONLINE
                            || m.getOnlineStatus() == OnlineStatus.DO_NOT_DISTURB) && !m.getUser().isBot()
                            && m.getIdLong() != ctx.getMember().getIdLong())
                    .findAny();
            if (!member.isPresent()) {
                ctx.getEvent().deferReply(true)
                        .setContent("There are no online members with access to this channel.").queue();
            } else {
                ctx.getEvent().deferReply().setContent(member.get().getAsMention() + ", "
                        + ctx.getMember().getEffectiveName() + " is asking for your assistance!").queue();
            }
            return;
        }

        final Optional<Member> member = members.stream()
                .filter(m -> !m.getUser().isBot() && m.getIdLong() != ctx.getMember().getIdLong())
                .sorted((member1, member2) -> Constants.RANDOM.nextInt(1) == 0 ? -1 : 1).findAny();
        if (!member.isPresent()) {
            ctx.getEvent().deferReply(true).setContent("There are no members with access to this channel.")
                    .queue();
        } else {
            ctx.getEvent().deferReply().setContent(member.get().getAsMention() + ", "
                    + ctx.getMember().getEffectiveName() + " is asking for your assistance!").queue();
        }
    }

    @Override
    public boolean isDevelopmentOnly() {
        return true;
    }

    @Override
    public boolean isModeratorOnly() {
        return true;
    }
}
