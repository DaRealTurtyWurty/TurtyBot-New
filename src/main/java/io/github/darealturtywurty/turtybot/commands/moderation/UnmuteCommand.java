package io.github.darealturtywurty.turtybot.commands.moderation;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import io.github.darealturtywurty.turtybot.util.core.BotUtils;
import io.github.darealturtywurty.turtybot.util.core.CoreBotUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class UnmuteCommand implements GuildCommand {

    public static boolean unmuteMember(final Guild guild, final Member toUnmute) {
        final Map<Long, Set<Long>> mutedUserRoles = CoreBotUtils.GUILDS.get(guild.getIdLong()).mutedUserRoles;
        if (mutedUserRoles.containsKey(toUnmute.getIdLong())) {
            final var loggingChannel = BotUtils.getModLogChannel(guild);

            // Remove muted role and add back original roles
            final Set<Long> memberRoles = mutedUserRoles.get(toUnmute.getIdLong());
            guild.removeRoleFromMember(toUnmute, BotUtils.getMutedRole(guild)).queue(rem -> memberRoles
                    .forEach(roleId -> guild.addRoleToMember(toUnmute, guild.getRoleById(roleId)).queue()));

            // Log the unmute
            final var unmuteEmbed = new EmbedBuilder().setColor(toUnmute.getColorRaw())
                    .setTitle(toUnmute.getEffectiveName() + " has been automatically unmuted.");
            loggingChannel.sendMessageEmbeds(unmuteEmbed.build()).queue();

            // Remove member from role cache
            mutedUserRoles.remove(toUnmute.getIdLong(), memberRoles);
            return true;
        }
        return false;
    }

    public static boolean unmuteMember(final Guild guild, final Member unmuter, final Member toUnmute,
            @Nullable final Message toDelete) {
        if (toDelete != null) {
            toDelete.delete().queue();
        }

        final Map<Long, Set<Long>> mutedUserRoles = CoreBotUtils.GUILDS.get(guild.getIdLong()).mutedUserRoles;

        if (mutedUserRoles.containsKey(toUnmute.getIdLong())) {
            final var loggingChannel = BotUtils.getModLogChannel(guild);

            // Remove muted role and add back original roles
            final Set<Long> memberRoles = mutedUserRoles.get(toUnmute.getIdLong());
            guild.removeRoleFromMember(toUnmute, BotUtils.getMutedRole(guild)).queue(rem -> memberRoles
                    .forEach(roleId -> guild.addRoleToMember(toUnmute, guild.getRoleById(roleId)).queue()));

            // Log the unmute
            final var unmuteEmbed = new EmbedBuilder().setColor(toUnmute.getColorRaw())
                    .setTitle(toUnmute.getEffectiveName() + " has been unmuted.")
                    .setDescription("**Unmuted By**: " + unmuter.getAsMention());
            loggingChannel.sendMessageEmbeds(unmuteEmbed.build()).queue();

            // Remove member from role cache
            mutedUserRoles.remove(toUnmute.getIdLong(), memberRoles);
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
        ctx.getEvent().deferReply(true).setContent(toUnmute.getAsMention() + " has been unmuted!").queue();
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
