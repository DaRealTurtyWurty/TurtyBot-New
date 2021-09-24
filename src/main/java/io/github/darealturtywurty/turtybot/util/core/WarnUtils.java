package io.github.darealturtywurty.turtybot.util.core;

import java.awt.Color;
import java.time.Instant;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import io.github.darealturtywurty.turtybot.util.data.GuildInfo;
import io.github.darealturtywurty.turtybot.util.data.UserWarns;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public final class WarnUtils {

    private WarnUtils() {
        throw new UnsupportedOperationException("Cannot construct a Utility Class");
    }

    public static void clearWarns(final Guild guild, final Member clearer, final Member toClear) {
        createUserWarns(guild, toClear);
        CoreBotUtils.GUILDS.get(guild.getIdLong()).userWarns.clear();

        final var loggingChannel = BotUtils.getModLogChannel(guild);
        if (loggingChannel != null) {
            final var embed = new EmbedBuilder().setColor(Color.BLUE)
                    .setTitle("Warns cleared for: " + toClear.getEffectiveName())
                    .setDescription("**Cleared By**: " + clearer.getAsMention()).setTimestamp(Instant.now());
            loggingChannel.sendMessageEmbeds(embed.build()).queue();
        }
    }

    public static UserWarns createUserWarns(final Guild guild, final long userID) {
        GuildInfo info = CoreBotUtils.GUILDS.get(guild.getIdLong());
        if (info == null) {
            info = new GuildInfo(guild);
            CoreBotUtils.GUILDS.put(guild.getIdLong(), info);
        }
        info.userWarns.put(userID, new UserWarns(userID, new HashMap<>()));
        CoreBotUtils.writeGuildInfo();
        return info.userWarns.get(userID);
    }

    public static UserWarns createUserWarns(final Guild guild, final Member member) {
        GuildInfo info = CoreBotUtils.GUILDS.get(guild.getIdLong());
        if (info == null) {
            info = new GuildInfo(guild);
            CoreBotUtils.GUILDS.put(guild.getIdLong(), info);
        }
        info.userWarns.put(member.getIdLong(), new UserWarns(member));
        CoreBotUtils.writeGuildInfo();
        final var userWarns = info.userWarns.get(member.getIdLong());
        if (userWarns.member == null) {
            userWarns.member = member;
        }
        return userWarns;
    }

    public static UserWarns createUserWarns(final Guild guild, final User user) {
        GuildInfo info = CoreBotUtils.GUILDS.get(guild.getIdLong());
        if (info == null) {
            info = new GuildInfo(guild);
            CoreBotUtils.GUILDS.put(guild.getIdLong(), info);
        }
        info.userWarns.put(user.getIdLong(), new UserWarns(user));
        CoreBotUtils.writeGuildInfo();
        return info.userWarns.get(user.getIdLong());
    }

    public static UserWarns getUserWarns(final Guild guild, final long userID) {
        final GuildInfo info = CoreBotUtils.GUILDS.get(guild.getIdLong());
        if (info == null)
            return createUserWarns(guild, userID);
        return info.userWarns.get(userID) == null ? createUserWarns(guild, userID)
                : info.userWarns.get(userID);
    }

    public static UserWarns getUserWarns(final Guild guild, final Member member) {
        final GuildInfo info = CoreBotUtils.GUILDS.get(guild.getIdLong());
        if (info == null)
            return createUserWarns(guild, member);
        final UserWarns userWarns = info.userWarns.get(member.getIdLong()) == null
                ? createUserWarns(guild, member)
                : info.userWarns.get(member.getIdLong());
        if (userWarns.member == null) {
            userWarns.member = member;
        }
        return userWarns;
    }

    public static UserWarns getUserWarns(final Guild guild, final User user) {
        final GuildInfo info = CoreBotUtils.GUILDS.get(guild.getIdLong());
        if (info == null)
            return createUserWarns(guild, user);
        return info.userWarns.get(user.getIdLong()) == null ? createUserWarns(guild, user)
                : info.userWarns.get(user.getIdLong());
    }

    public static boolean removeWarnByUUID(final Guild guild, final Member remover, final String strUUID) {
        final var uuid = UUID.fromString(strUUID);
        if (uuid == null)
            return false;

        final AtomicReference<UserWarns> atomicUserWarns = new AtomicReference<>();
        CoreBotUtils.GUILDS.get(guild.getIdLong()).userWarns.forEach((user, warns) -> {
            if (atomicUserWarns.get() == null && warns.warns.containsKey(uuid)) {
                atomicUserWarns.set(warns);
            }
        });

        if (atomicUserWarns.get() != null)
            return atomicUserWarns.get().removeWarn(guild, remover, strUUID);

        return atomicUserWarns.get() != null;
    }
}