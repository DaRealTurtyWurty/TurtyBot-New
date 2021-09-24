package io.github.darealturtywurty.turtybot.util.core;

import static io.github.darealturtywurty.turtybot.util.Constants.LOGGER;

import java.util.logging.Level;

import com.typesafe.config.ConfigException;

import io.github.darealturtywurty.turtybot.util.data.GuildInfo;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

public final class StarboardUtils {

    private StarboardUtils() {
        throw new UnsupportedOperationException("Cannot construct a Utility Class");
    }

    public static int getMinimumStars(final Guild guild) {
        return CoreBotUtils.GUILDS.get(guild.getIdLong()) == null ? 5
                : CoreBotUtils.GUILDS.get(guild.getIdLong()).minimumStars;
    }

    public static TextChannel getShowcasesChannel(final Guild guild) {
        var channelID = 0L;
        try {
            channelID = getShowcasesFromGuild(guild);
        } catch (final ConfigException.Missing e) {
            LOGGER.log(Level.INFO, "No Showcases Channel found in config for guild: {0} [{1}]!",
                    new Object[] { guild.getName(), guild.getId() });
        }

        if (channelID <= 0) {
            final var channel = guild.getTextChannelsByName("showcases", true).get(0);
            if (channel == null) {
                LOGGER.log(Level.WARNING, "No Showcases Channel found in guild: {0} [{1}]!",
                        new Object[] { guild.getName(), guild.getId() });
                return null;
            }
            channelID = channel.getIdLong();
        }

        setShowcasesChannel(guild, channelID);

        final var showcasesChannel = guild.getTextChannelById(channelID);
        if (showcasesChannel == null) {
            LOGGER.log(Level.WARNING, "No Showcases Channel found in guild: {0} [{1}]!",
                    new Object[] { guild.getName(), guild.getId() });
            return null;
        }
        return showcasesChannel;
    }

    public static long getShowcasesFromGuild(final Guild guild) {
        return CoreBotUtils.GUILDS.get(guild.getIdLong()) == null ? 0L
                : CoreBotUtils.GUILDS.get(guild.getIdLong()).showcasesID;
    }

    public static int getStageStar(final Guild guild, int stage) {
        final GuildInfo info = CoreBotUtils.GUILDS.get(guild.getIdLong()) == null ? new GuildInfo(guild)
                : CoreBotUtils.GUILDS.get(guild.getIdLong());

        int count;
        if (stage >= info.stages.length) {
            stage = info.stages.length - 1;
        } else if (stage < 0) {
            stage = 0;
        }
        count = info.stages[stage];

        CoreBotUtils.GUILDS.put(guild.getIdLong(), info);
        CoreBotUtils.writeGuildInfo();
        return count;
    }

    public static TextChannel getStarboardChannel(final Guild guild) {
        var channelID = 0L;
        try {
            channelID = getStarboardFromGuild(guild);
        } catch (final ConfigException.Missing e) {
            LOGGER.log(Level.INFO, "No Starboard Channel found in config for guild: {0} [{1}]!",
                    new Object[] { guild.getName(), guild.getId() });
        }

        if (channelID <= 0) {
            final var channel = guild.getTextChannelsByName("starboard", true).get(0);
            if (channel == null) {
                LOGGER.log(Level.WARNING, "No Starboard Channel found in guild: {0} [{1}]!",
                        new Object[] { guild.getName(), guild.getId() });
                return null;
            }
            channelID = channel.getIdLong();
        }

        setStarboardChannel(guild, channelID);

        final var starboardChannel = guild.getTextChannelById(channelID);
        if (starboardChannel == null) {
            LOGGER.log(Level.WARNING, "No Starboard Channel found in guild: {0} [{1}]!",
                    new Object[] { guild.getName(), guild.getId() });
            return null;
        }
        return starboardChannel;
    }

    public static long getStarboardFromGuild(final Guild guild) {
        return CoreBotUtils.GUILDS.get(guild.getIdLong()) == null ? 0L
                : CoreBotUtils.GUILDS.get(guild.getIdLong()).starboardID;
    }

    public static boolean includesBotStar(final Guild guild) {
        return CoreBotUtils.GUILDS.get(guild.getIdLong()) != null
                && CoreBotUtils.GUILDS.get(guild.getIdLong()).includeBotStar;
    }

    public static boolean isStarboardEnabled(final Guild guild) {
        return CoreBotUtils.GUILDS.get(guild.getIdLong()) != null
                && CoreBotUtils.GUILDS.get(guild.getIdLong()).enableStarboard;
    }

    public static void setIncludeBotStar(final Guild guild, final boolean include) {
        final GuildInfo info = CoreBotUtils.GUILDS.get(guild.getIdLong()) == null ? new GuildInfo(guild)
                : CoreBotUtils.GUILDS.get(guild.getIdLong());
        info.includeBotStar = include;
        CoreBotUtils.GUILDS.put(guild.getIdLong(), info);
        CoreBotUtils.writeGuildInfo();
    }

    public static void setMinimumStars(final Guild guild, int minimum) {
        final GuildInfo info = CoreBotUtils.GUILDS.get(guild.getIdLong()) == null ? new GuildInfo(guild)
                : CoreBotUtils.GUILDS.get(guild.getIdLong());
        if (minimum <= 0) {
            minimum = 5;
        }
        info.minimumStars = minimum;
        CoreBotUtils.GUILDS.put(guild.getIdLong(), info);
        CoreBotUtils.writeGuildInfo();
    }

    public static void setShowcasesChannel(final Guild guild, final long channelID) {
        final GuildInfo info = CoreBotUtils.GUILDS.get(guild.getIdLong()) == null ? new GuildInfo(guild)
                : CoreBotUtils.GUILDS.get(guild.getIdLong());
        info.showcasesID = channelID;
        CoreBotUtils.GUILDS.put(guild.getIdLong(), info);
        CoreBotUtils.writeGuildInfo();
    }

    public static void setStarboardChannel(final Guild guild, final long channelID) {
        final GuildInfo info = CoreBotUtils.GUILDS.get(guild.getIdLong()) == null ? new GuildInfo(guild)
                : CoreBotUtils.GUILDS.get(guild.getIdLong());
        info.starboardID = channelID;
        CoreBotUtils.GUILDS.put(guild.getIdLong(), info);
        CoreBotUtils.writeGuildInfo();
    }

    public static void setStarboardEnabled(final Guild guild, final boolean enabled) {
        final GuildInfo info = CoreBotUtils.GUILDS.get(guild.getIdLong()) == null ? new GuildInfo(guild)
                : CoreBotUtils.GUILDS.get(guild.getIdLong());
        info.enableStarboard = enabled;
        CoreBotUtils.GUILDS.put(guild.getIdLong(), info);
        CoreBotUtils.writeGuildInfo();
    }
}