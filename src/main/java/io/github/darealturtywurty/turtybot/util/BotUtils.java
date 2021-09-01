package io.github.darealturtywurty.turtybot.util;

import static io.github.darealturtywurty.turtybot.util.Constants.LOGGER;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.logging.Level;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;

import io.github.darealturtywurty.turtybot.TurtyBot;
import io.github.darealturtywurty.turtybot.util.data.GuildInfo;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public final class BotUtils {

    private static Role mutedRole;

    private BotUtils() {
        throw new UnsupportedOperationException("Cannot construct a Utility Class");
    }

    public static Color generateRandomColor() {
        final var red = Constants.RANDOM.nextFloat();
        final var green = Constants.RANDOM.nextFloat();
        final var blue = Constants.RANDOM.nextFloat();
        return new Color(red, green, blue);
    }

    public static Color generateRandomPastelColor() {
        final var rand = Constants.RANDOM;
        final var hue = rand.nextFloat();
        // Saturation between 0.1 and 0.3
        final var saturation = (rand.nextInt(2000) + 1000) / 10000f;
        final var luminance = 0.9f;
        return Color.getHSBColor(hue, saturation, luminance);
    }

    public static Role getAdvModderRole(final Guild guild) {
        var roleID = 0L;
        try {
            roleID = getAdvModderRoleFromGuild(guild);
        } catch (final ConfigException.Missing e) {
            LOGGER.log(Level.INFO, "No Advanced Modder Role found in config for guild: {0} [{1}]!",
                    new Object[] { guild.getName(), guild.getId() });
        }

        if (roleID <= 0) {
            final var modderRole = guild.getRolesByName("advanced modder", true).get(0);
            if (modderRole == null) {
                LOGGER.log(Level.WARNING, "No Advanced Modder Role found in guild: {0} [{1}]!",
                        new Object[] { guild.getName(), guild.getId() });
                return null;
            }
            roleID = modderRole.getIdLong();
        }

        setAdvModderRoleForGuild(guild, roleID);

        final var modderRole = guild.getRoleById(roleID);
        if (modderRole == null) {
            LOGGER.log(Level.WARNING, "No Advanced Modder Role found in guild: {0} [{1}]!",
                    new Object[] { guild.getName(), guild.getId() });
            return null;
        }
        return modderRole;
    }

    public static long getAdvModderRoleFromGuild(final Guild guild) {
        return CoreBotUtils.GUILDS.get(guild).advModderRoleID;
    }

    public static int getBanThreshold(final Guild guild) {
        return CoreBotUtils.GUILDS.get(guild) == null ? 5 : CoreBotUtils.GUILDS.get(guild).banThreshold;
    }

    public static String getBotToken() {
        try {
            return getImportantConfig().getString("BotToken");
        } catch (final ConfigException.Missing e) {
            LOGGER.log(Level.WARNING, "No Bot Token found in config!");
            throw new IllegalArgumentException(e);
        }
    }

    public static String getGithubToken() {
        try {
            return getImportantConfig().getString("GithubToken");
        } catch (final ConfigException.Missing e) {
            LOGGER.log(Level.WARNING, "No GitHub Token found in config!");
            throw new IllegalArgumentException(e);
        }
    }

    public static Config getImportantConfig() {
        return Constants.CONFIG.getConfig("important");
    }

    public static int getKickThreshold(final Guild guild) {
        return CoreBotUtils.GUILDS.get(guild) == null ? 3 : CoreBotUtils.GUILDS.get(guild).kickThreshold;
    }

    public static Role getModeratorRole(final Guild guild) {
        var roleID = 0L;
        try {
            roleID = getModRoleFromGuild(guild);
        } catch (final ConfigException.Missing e) {
            LOGGER.log(Level.INFO, "No Moderation Role found in config for guild: {0} [{1}]!",
                    new Object[] { guild.getName(), guild.getId() });
        }

        if (roleID <= 0) {
            final var modRole = guild.getRolesByName("moderator", true).get(0);
            if (modRole == null) {
                LOGGER.log(Level.WARNING, "No Moderation Role found in guild: {0} [{1}]!",
                        new Object[] { guild.getName(), guild.getId() });
                return null;
            }
            roleID = modRole.getIdLong();
        }

        setModRoleForGuild(guild, roleID);

        final var moderatorRole = guild.getRoleById(roleID);
        if (moderatorRole == null) {
            LOGGER.log(Level.WARNING, "No Moderation Role found in guild: {0} [{1}]!",
                    new Object[] { guild.getName(), guild.getId() });
        }
        return moderatorRole;
    }

    public static TextChannel getModLogChannel(final Guild guild) {
        var channelID = 0L;
        try {
            channelID = getModLogFromGuild(guild);
        } catch (final ConfigException.Missing e) {
            LOGGER.log(Level.INFO, "No Moderation Log Channel found in config for guild: {0} [{1}]!",
                    new Object[] { guild.getName(), guild.getId() });
        }

        if (channelID <= 0) {
            final var channel = guild.getTextChannelsByName("mod-log", true).get(0);
            if (channel == null) {
                LOGGER.log(Level.WARNING, "No Moderation Log Channel found in guild: {0} [{1}]!",
                        new Object[] { guild.getName(), guild.getId() });
                return null;
            }
            channelID = channel.getIdLong();
        }

        setModLogForGuild(guild, channelID);

        final var modLogChannel = guild.getTextChannelById(channelID);
        if (modLogChannel == null) {
            LOGGER.log(Level.WARNING, "No Moderation Log Channel found in guild: {0} [{1}]!",
                    new Object[] { guild.getName(), guild.getId() });
            return null;
        }
        return modLogChannel;
    }

    public static long getModLogFromGuild(final Guild guild) {
        return CoreBotUtils.GUILDS.get(guild).modLogID;
    }

    public static long getModRoleFromGuild(final Guild guild) {
        return CoreBotUtils.GUILDS.get(guild).modRoleID;
    }

    public static String getMongoConnection() {
        try {
            return getImportantConfig().getString("MongoConnection");
        } catch (final ConfigException.Missing e) {
            LOGGER.log(Level.WARNING, "No Mongo Connection found in config!");
            return "";
        }
    }

    public static Role getMutedRole(final Guild guild) {
        var roleID = 0L;
        try {
            roleID = getMutedRoleFromGuild(guild);
        } catch (final ConfigException.Missing e) {
            LOGGER.log(Level.INFO, "No Muted Role found in config for guild: {0} [{1}]!",
                    new Object[] { guild.getName(), guild.getId() });
        }

        if (roleID <= 0L) {
            Role mutedRole = null;
            try {
                mutedRole = guild.getRolesByName("Muted", true).get(0);
            } catch (final IndexOutOfBoundsException ex) {
                LOGGER.log(Level.WARNING, "No Muted Role found in guild: {0} [{1}]!",
                        new Object[] { guild.getName(), guild.getId() });
                return createMutedRole(guild);
            }
            roleID = mutedRole.getIdLong();
        }

        setMutedRoleForGuild(guild, roleID);

        final var mutedRole = guild.getRoleById(roleID);
        if (mutedRole == null) {
            LOGGER.log(Level.WARNING, "No Muted Role found in guild: {0} [{1}]!",
                    new Object[] { guild.getName(), guild.getId() });
            return createMutedRole(guild);
        }
        return mutedRole;
    }

    public static long getMutedRoleFromGuild(final Guild guild) {
        return CoreBotUtils.GUILDS.get(guild).mutedRoleID;
    }

    public static int getMuteThreshold(final Guild guild) {
        return CoreBotUtils.GUILDS.get(guild) == null ? 2 : CoreBotUtils.GUILDS.get(guild).muteThreshold;
    }

    public static long getOwnerID() {
        try {
            return getImportantConfig().getLong("OwnerID");
        } catch (final ConfigException.Missing e) {
            LOGGER.log(Level.WARNING, "No Bot Owner found in config!");
            return Constants.DEFAULT_OWNER_ID;
        }
    }

    public static String getPrefixFromGuild(final Guild guild) {
        return CoreBotUtils.GUILDS.get(guild) == null ? "!" : CoreBotUtils.GUILDS.get(guild).prefix;
    }

    public static String getRedditID() {
        try {
            return getImportantConfig().getString("RedditID");
        } catch (final ConfigException.Missing e) {
            throw new NullPointerException("No Reddit ID found in config!");
        }
    }

    public static String getRedditSecret() {
        try {
            return getImportantConfig().getString("RedditSecret");
        } catch (final ConfigException.Missing e) {
            throw new NullPointerException("No Reddit Secret found in config!");
        }
    }

    public static boolean isBotOwner(final User user) {
        return getOwnerID() == user.getIdLong();
    }

    public static boolean isModerator(final Guild guild, final Member member) {
        if (member == null)
            return false;

        if (!member.isOwner()) {
            final var modRole = getModeratorRole(guild);
            return modRole != null && member.getRoles().contains(modRole);
        }
        return true;
    }

    public static boolean isModerator(final Guild guild, final User user) {
        final var member = guild.getMember(user);
        if (member == null) {
            LOGGER.log(Level.WARNING, "Cannot find user: {0} [{1}] in guild: {2} [{3}]!",
                    new Object[] { user.getName() + "#" + user.getDiscriminator(), user.getId(),
                            guild.getName(), guild.getId() });
            return false;
        }

        if (!member.isOwner()) {
            final var modRole = getModeratorRole(guild);
            return modRole != null && member.getRoles().contains(modRole);
        }
        return true;
    }

    public static void restartApplication(final JDA jda) throws URISyntaxException, IOException {
        shutdownApplication(jda);
        final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator
                + "java";
        final var currentJar = new File(
                TurtyBot.class.getProtectionDomain().getCodeSource().getLocation().toURI());

        /* is it a jar file? */
        if (!currentJar.getName().endsWith(".jar"))
            return;

        /* Build command: java -jar application.jar */
        final ArrayList<String> command = new ArrayList<>();
        command.add(javaBin);
        command.add("-jar");
        command.add(currentJar.getPath());

        final var builder = new ProcessBuilder(command);
        builder.start();
        System.exit(0);
    }

    public static void setAdvModderRoleForGuild(final Guild guild, final long roleID) {
        final GuildInfo info = CoreBotUtils.GUILDS.get(guild) == null ? new GuildInfo(guild)
                : CoreBotUtils.GUILDS.get(guild);
        info.advModderRoleID = roleID;
        CoreBotUtils.GUILDS.put(guild, info);
        CoreBotUtils.writeGuildInfo();
    }

    public static void setBanThreshold(final Guild guild, int maxWarns) {
        final int kickThreshold = getKickThreshold(guild);
        final GuildInfo info = CoreBotUtils.GUILDS.get(guild) == null ? new GuildInfo(guild)
                : CoreBotUtils.GUILDS.get(guild);
        if (maxWarns <= 0) {
            maxWarns = 5;
        }
        if (maxWarns <= kickThreshold) {
            maxWarns = kickThreshold + 1;
        }
        info.banThreshold = maxWarns;
        CoreBotUtils.GUILDS.put(guild, info);
        CoreBotUtils.writeGuildInfo();
    }

    public static void setKickThreshold(final Guild guild, int maxWarns) {
        final int banThreshold = getBanThreshold(guild);
        final GuildInfo info = CoreBotUtils.GUILDS.get(guild) == null ? new GuildInfo(guild)
                : CoreBotUtils.GUILDS.get(guild);
        if (maxWarns <= 0) {
            maxWarns = 3;
        }
        if (maxWarns >= banThreshold && banThreshold > 1) {
            maxWarns = banThreshold - 1;
        } else if (banThreshold <= 1) {
            resetThresholds(guild);
            return;
        }
        info.kickThreshold = maxWarns;
        CoreBotUtils.GUILDS.put(guild, info);
        CoreBotUtils.writeGuildInfo();
    }

    public static void setModLogForGuild(final Guild guild, final long channelID) {
        final GuildInfo info = CoreBotUtils.GUILDS.get(guild) == null ? new GuildInfo(guild)
                : CoreBotUtils.GUILDS.get(guild);
        info.modLogID = channelID;
        CoreBotUtils.GUILDS.put(guild, info);
        CoreBotUtils.writeGuildInfo();
    }

    public static void setModRoleForGuild(final Guild guild, final long roleID) {
        final GuildInfo info = CoreBotUtils.GUILDS.get(guild) == null ? new GuildInfo(guild)
                : CoreBotUtils.GUILDS.get(guild);
        info.modRoleID = roleID;
        CoreBotUtils.GUILDS.put(guild, info);
        CoreBotUtils.writeGuildInfo();
    }

    public static void setMutedRoleForGuild(final Guild guild, final long roleID) {
        final GuildInfo info = CoreBotUtils.GUILDS.get(guild) == null ? new GuildInfo(guild)
                : CoreBotUtils.GUILDS.get(guild);
        info.mutedRoleID = roleID;
        CoreBotUtils.GUILDS.put(guild, info);
        CoreBotUtils.writeGuildInfo();
    }

    public static void setMuteThreshold(final Guild guild, int maxWarns) {
        final int kickThreshold = getKickThreshold(guild);
        final GuildInfo info = CoreBotUtils.GUILDS.get(guild) == null ? new GuildInfo(guild)
                : CoreBotUtils.GUILDS.get(guild);
        if (maxWarns <= 0) {
            maxWarns = 2;
        }
        if (maxWarns >= kickThreshold && kickThreshold > 1) {
            maxWarns = kickThreshold - 1;
        } else if (kickThreshold <= 1) {
            resetThresholds(guild);
            return;
        }
        info.muteThreshold = maxWarns;
        CoreBotUtils.GUILDS.put(guild, info);
        CoreBotUtils.writeGuildInfo();
    }

    public static void setPrefixForGuild(final Guild guild, final String prefix) {
        final GuildInfo info = CoreBotUtils.GUILDS.get(guild) == null ? new GuildInfo(guild)
                : CoreBotUtils.GUILDS.get(guild);
        info.prefix = prefix;
        CoreBotUtils.GUILDS.put(guild, info);
        CoreBotUtils.writeGuildInfo();
    }

    public static void shutdownApplication(final JDA jda) {
        CoreBotUtils.writeGuildInfo();
        jda.shutdown();
        jda.getHttpClient().connectionPool().evictAll();
        jda.getHttpClient().dispatcher().executorService().shutdown();
        System.exit(0);
    }

    public static String trueFalseToYesNo(final boolean value) {
        return String.valueOf(value).replace("true", "Yes").replace("false", "No");
    }

    private static Role createMutedRole(final Guild guild) {
        guild.createRole().setName("Muted").setColor(Color.DARK_GRAY).setMentionable(false)
                .setPermissions(Permission.EMPTY_PERMISSIONS).setHoisted(true)
                .queue(r -> guild.getTextChannels().forEach(channel -> channel.createPermissionOverride(r)
                        .deny(Permission.MESSAGE_WRITE).queue(role -> mutedRole = role.getRole())));
        final var role = mutedRole;
        mutedRole = null;
        return role;
    }

    private static void resetThresholds(final Guild guild) {
        final GuildInfo info = CoreBotUtils.GUILDS.get(guild) == null ? new GuildInfo(guild)
                : CoreBotUtils.GUILDS.get(guild);
        info.muteThreshold = 2;
        info.kickThreshold = 3;
        info.banThreshold = 5;
        CoreBotUtils.GUILDS.put(guild, info);
        CoreBotUtils.writeGuildInfo();
    }
}
