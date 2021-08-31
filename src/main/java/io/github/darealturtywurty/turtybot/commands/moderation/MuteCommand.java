package io.github.darealturtywurty.turtybot.commands.moderation;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Nullable;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import io.github.darealturtywurty.turtybot.util.BotUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class MuteCommand implements GuildCommand {

    private static final Timer MUTE_TIMER = new Timer();
    protected static final Map<Member, List<Role>> MUTED_ROLE_MAP = new HashMap<>();

    public static void muteMember(final Guild guild, final Member muter, final Member member,
            @Nullable final Message toDelete, final String reason, final long timeMillis) {
        final var loggingChannel = BotUtils.getModLogChannel(guild);
        final var mutedRole = BotUtils.getMutedRole(guild);
        final var memberRoles = member.getRoles();

        if (toDelete != null) {
            toDelete.delete().queue();
        }

        for (final var role : memberRoles) {
            if (role.getName().equalsIgnoreCase("@everyone")) {
                memberRoles.remove(role);
            }
        }
        MUTED_ROLE_MAP.put(member, memberRoles);

        guild.modifyMemberRoles(member, mutedRole).queue();

        final var muteEmbed = new EmbedBuilder().setColor(Color.DARK_GRAY)
                .setTitle(member.getEffectiveName() + " has been muted.")
                .setDescription("**Reason**: " + reason + "\n**Muted By**: " + muter.getAsMention());
        loggingChannel.sendMessageEmbeds(muteEmbed.build()).queue();

        MUTE_TIMER.schedule(new TimerTask() {
            @Override
            public void run() {
                UnmuteCommand.unmuteMember(guild, member);
            }
        }, timeMillis);
    }

    public static String removeLastChar(final String str) {
        return removeLastChars(str, 1);
    }

    public static String removeLastChars(final String str, final int chars) {
        return str.substring(0, str.length() - chars);
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.MODERATION;
    }

    @Override
    public String getDescription() {
        return "Mutes a member.";
    }

    @Override
    public String getName() {
        return "mute";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(new OptionData(OptionType.USER, "user", "User to mute", true),
                new OptionData(OptionType.STRING, "time", "Time to mute for. Example: 2h", false),
                new OptionData(OptionType.STRING, "reason", "The reason to mute for", false));
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        final Member toMute = ctx.getEvent().getOption("user").getAsMember();
        if (toMute == null || !ctx.getMember().canInteract(toMute)
                || !BotUtils.isModerator(ctx.getGuild(), ctx.getMember())) {
            ctx.getEvent().deferReply(true).setContent("You do not have permission to mute this user!")
                    .mentionRepliedUser(false).queue();
            return;
        }

        if (toMute.getIdLong() == ctx.getMember().getIdLong()) {
            ctx.getEvent().deferReply(true).setContent("You cannot mute yourself!").mentionRepliedUser(false)
                    .queue();
            return;
        }

        final OptionMapping timeOption = ctx.getEvent().getOption("time");
        final String timeStr = timeOption == null ? "2h" : timeOption.getAsString();
        final long multiplier = parseTimeUnit(timeStr);
        final long amount = Integer.parseInt(removeLastChar(timeStr));

        final OptionMapping reasonOption = ctx.getEvent().getOption("reason");
        final String reason = reasonOption == null ? "Unspecified" : reasonOption.getAsString();

        muteMember(ctx.getGuild(), ctx.getMember(), toMute, null, reason, amount * multiplier);
    }

    @Override
    public boolean isModeratorOnly() {
        return true;
    }

    private long parseTimeUnit(final String time) {
        var multiplier = 1000L;
        for (final char c : time.toCharArray()) {
            switch (c) {
            case 's', 'S':
                multiplier = 1000L;
                break;
            case 'm', 'M':
                multiplier = 60000L;
                break;
            case 'h', 'H':
                multiplier = 3600000L;
                break;
            case 'd', 'D':
                multiplier = 86400000L;
                break;
            default:
                continue;
            }
        }
        return multiplier;
    }
}
