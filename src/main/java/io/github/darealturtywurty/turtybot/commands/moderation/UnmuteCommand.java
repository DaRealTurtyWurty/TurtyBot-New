package io.github.darealturtywurty.turtybot.commands.moderation;

import java.util.List;

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
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class UnmuteCommand implements GuildCommand {

    public static boolean unmuteMember(final Guild guild, final Member toUnmute) {
        if (MuteCommand.MUTED_ROLE_MAP.containsKey(toUnmute)) {
            final var loggingChannel = BotUtils.getModLogChannel(guild);

            // Remove muted role and add back original roles
            final List<Role> memberRoles = MuteCommand.MUTED_ROLE_MAP.get(toUnmute);
            guild.removeRoleFromMember(toUnmute, BotUtils.getMutedRole(guild))
                    .queue(rem -> memberRoles.forEach(role -> guild.addRoleToMember(toUnmute, role).queue()));

            // Log the unmute
            final var unmuteEmbed = new EmbedBuilder().setColor(toUnmute.getColorRaw())
                    .setTitle(toUnmute.getEffectiveName() + " has been automatically unmuted.");
            loggingChannel.sendMessageEmbeds(unmuteEmbed.build()).queue();

            // Remove member from role cache
            MuteCommand.MUTED_ROLE_MAP.remove(toUnmute, memberRoles);
            return true;
        }
        return false;
    }

    public static boolean unmuteMember(final Guild guild, final Member unmuter, final Member toUnmute,
            @Nullable final Message toDelete) {
        if (toDelete != null) {
            toDelete.delete().queue();
        }

        if (MuteCommand.MUTED_ROLE_MAP.containsKey(toUnmute)) {
            final var loggingChannel = BotUtils.getModLogChannel(guild);

            // Remove muted role and add back original roles
            final List<Role> memberRoles = MuteCommand.MUTED_ROLE_MAP.get(toUnmute);
            guild.removeRoleFromMember(toUnmute, BotUtils.getMutedRole(guild))
                    .queue(rem -> memberRoles.forEach(role -> guild.addRoleToMember(toUnmute, role).queue()));

            // Log the unmute
            final var unmuteEmbed = new EmbedBuilder().setColor(toUnmute.getColorRaw())
                    .setTitle(toUnmute.getEffectiveName() + " has been unmuted.")
                    .setDescription("**Unmuted By**: " + unmuter.getAsMention());
            loggingChannel.sendMessageEmbeds(unmuteEmbed.build()).queue();

            // Remove member from role cache
            MuteCommand.MUTED_ROLE_MAP.remove(toUnmute, memberRoles);
            return true;
        }
        return false;
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.MODERATION;
    }

    @Override
    public String getDescription() {
        return "Unmutes a member.";
    }

    @Override
    public String getName() {
        return "unmute";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(new OptionData(OptionType.USER, "user", "User to unmute", true));
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        final Member toUnmute = ctx.getEvent().getOption("user").getAsMember();
        if (toUnmute == null || !ctx.getMember().canInteract(toUnmute)
                || !BotUtils.isModerator(ctx.getGuild(), ctx.getMember())) {
            ctx.getEvent().deferReply(true).setContent("You do not have permission to unmute this user!")
                    .mentionRepliedUser(false).queue();
            return;
        }

        if (toUnmute.getIdLong() == ctx.getMember().getIdLong()) {
            ctx.getEvent().deferReply(true).setContent("You cannot unmute yourself!")
                    .mentionRepliedUser(false).queue();
            return;
        }

        unmuteMember(ctx.getGuild(), ctx.getMember(), toUnmute, null);
    }

    @Override
    public boolean isModeratorOnly() {
        return true;
    }
}
