package io.github.darealturtywurty.turtybot.util.core;

import static io.github.darealturtywurty.turtybot.util.Constants.LOGGER;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.logging.Level;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;

import io.github.darealturtywurty.turtybot.TurtyBot;
import io.github.darealturtywurty.turtybot.util.Constants;
import io.github.darealturtywurty.turtybot.util.Constants.ColorConstants;
import io.github.darealturtywurty.turtybot.util.data.GuildInfo;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public final class BotUtils {

    private static Role mutedRole;

    private BotUtils() {
        throw new UnsupportedOperationException("Cannot construct a Utility Class");
    }

    public static int findFirstLetter(final String str) {
        int index = 0;
        for (int charIndex = 0; charIndex < str.toCharArray().length; charIndex++) {
            index++;
            final char c = str.charAt(charIndex);
            if (Character.isLetter(c)) {
                break;
            }
        }
        return index;
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
        return CoreBotUtils.GUILDS.get(guild.getIdLong()).advModderRoleID;
    }

    public static int getBanThreshold(final Guild guild) {
        return CoreBotUtils.GUILDS.get(guild.getIdLong()) == null ? 5
                : CoreBotUtils.GUILDS.get(guild.getIdLong()).banThreshold;
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

    public static TextChannel getInformationChannel(final Guild guild) {
        var channelID = 0L;
        try {
            channelID = getInformationFromGuild(guild);
        } catch (final ConfigException.Missing e) {
            LOGGER.log(Level.INFO, "No Information Channel found in config for guild: {0} [{1}]!",
                    new Object[] { guild.getName(), guild.getId() });
        }

        if (channelID <= 0) {
            final var channel = guild.getTextChannelsByName("📧information", true).get(0);
            if (channel == null) {
                LOGGER.log(Level.WARNING, "No Information Channel found in guild: {0} [{1}]!",
                        new Object[] { guild.getName(), guild.getId() });
                return null;
            }
            channelID = channel.getIdLong();
        }

        setInformationForGuild(guild, channelID);

        final var informationChannel = guild.getTextChannelById(channelID);
        if (informationChannel == null) {
            LOGGER.log(Level.WARNING, "No Information Channel found in guild: {0} [{1}]!",
                    new Object[] { guild.getName(), guild.getId() });
            return null;
        }
        return informationChannel;
    }

    public static long getInformationFromGuild(final Guild guild) {
        return CoreBotUtils.GUILDS.get(guild.getIdLong()).informationID;
    }

    public static int getKickThreshold(final Guild guild) {
        return CoreBotUtils.GUILDS.get(guild.getIdLong()) == null ? 3
                : CoreBotUtils.GUILDS.get(guild.getIdLong()).kickThreshold;
    }

    public static AtomicReference<Message> getMessageBeforeId(final TextChannel channel, final long messageId,
            final Function<Message, Void> function) {
        final var atomicReference = new AtomicReference<Message>();
        channel.getHistoryBefore(messageId, 1).queue(msgs -> {
            final Message message = msgs.getRetrievedHistory().get(0);
            atomicReference.set(message);
            function.apply(message);
        });
        return atomicReference;
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
        return CoreBotUtils.GUILDS.get(guild.getIdLong()).modLogID;
    }

    public static long getModRoleFromGuild(final Guild guild) {
        return CoreBotUtils.GUILDS.get(guild.getIdLong()).modRoleID;
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
        return CoreBotUtils.GUILDS.get(guild.getIdLong()).mutedRoleID;
    }

    public static int getMuteThreshold(final Guild guild) {
        return CoreBotUtils.GUILDS.get(guild.getIdLong()) == null ? 2
                : CoreBotUtils.GUILDS.get(guild.getIdLong()).muteThreshold;
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
        return CoreBotUtils.GUILDS.get(guild.getIdLong()) == null ? "!"
                : CoreBotUtils.GUILDS.get(guild.getIdLong()).prefix;
    }

    public static String getR6StatsKey() {
        try {
            return getImportantConfig().getString("R6StatsKey");
        } catch (final ConfigException.Missing e) {
            throw new NullPointerException("No R6Stats Key found in config!");
        }
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

    public static String getStrawpollKey() {
        try {
            return getImportantConfig().getString("StrawpollKey");
        } catch (final ConfigException.Missing e) {
            LOGGER.log(Level.WARNING, "No Bot Owner found in config!");
            return "";
        }
    }

    public static TextChannel getSuggestionsChannel(final Guild guild) {
        var channelID = 0L;
        try {
            channelID = getSuggestionsFromGuild(guild);
        } catch (final ConfigException.Missing e) {
            LOGGER.log(Level.INFO, "No Suggestion Channel found in config for guild: {0} [{1}]!",
                    new Object[] { guild.getName(), guild.getId() });
        }

        if (channelID <= 0) {
            final List<TextChannel> channels = guild.getTextChannelsByName("suggestions", true);
            if (channels.isEmpty())
                return null;

            final var channel = channels.get(0);
            if (channel == null) {
                LOGGER.log(Level.WARNING, "No Suggestions Channel found in guild: {0} [{1}]!",
                        new Object[] { guild.getName(), guild.getId() });
                return null;
            }
            channelID = channel.getIdLong();
        }

        setSuggestionsForGuild(guild, channelID);

        final var suggestionsChannel = guild.getTextChannelById(channelID);
        if (suggestionsChannel == null) {
            LOGGER.log(Level.WARNING, "No Suggestions Channel found in guild: {0} [{1}]!",
                    new Object[] { guild.getName(), guild.getId() });
            return null;
        }
        return suggestionsChannel;
    }

    public static long getSuggestionsFromGuild(final Guild guild) {
        return CoreBotUtils.GUILDS.get(guild.getIdLong()).suggestionsID;
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

    public static String millisecondsFormatted(final long millis) {
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        final long years = days / 365;
        days %= 365;
        final long months = days / 30;
        days %= 30;
        final long weeks = days / 7;
        days %= 7;
        final long hours = TimeUnit.MILLISECONDS.toHours(millis)
                - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(millis));
        final long minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
                - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis));
        final long seconds = TimeUnit.MILLISECONDS.toSeconds(millis)
                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis));
        final long milliseconds = TimeUnit.MILLISECONDS.toMillis(millis)
                - TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(millis));
        return String.format("%dyrs %dm %dw %ddays %dhrs %dmins %dsecs %dms", years, months, weeks, days,
                hours, minutes, seconds, milliseconds);
    }

    public static boolean notTestServer(final Guild guild) {
        return guild.getIdLong() != 819294753732296776L;
    }

    public static Color parseColor(final String asString) {
        Color retColor = Color.BLACK;
        try {
            retColor = Color.decode(asString);
        } catch (final NumberFormatException ex) {
            try {
                final String[] parts = asString.split("[^0-9]+");
                if (parts.length < 3)
                    throw new NumberFormatException("Must be at least RGB!");

                if (parts.length == 3)
                    return new Color(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]),
                            Integer.parseInt(parts[2]));
                return new Color(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
            } catch (final NumberFormatException exc) {
                retColor = switch (asString.trim().toLowerCase().replace("\s", "_")) {
                    case "blue" -> Color.BLUE;
                    case "brown" -> ColorConstants.BROWN;
                    case "cyan" -> Color.CYAN;
                    case "dark_blue" -> ColorConstants.DARK_BLUE;
                    case "dark_brown" -> ColorConstants.DARK_BROWN;
                    case "dark_grey", "dark_gray" -> Color.DARK_GRAY;
                    case "dark_green" -> ColorConstants.DARK_GREEN;
                    case "dark_red" -> ColorConstants.DARK_RED;
                    case "dark_yellow" -> ColorConstants.DARK_YELLOW;
                    case "grey", "gray" -> Color.GRAY;
                    case "green" -> Color.GREEN;
                    case "gold" -> ColorConstants.GOLD;
                    case "light_blue" -> ColorConstants.LIGHT_BLUE;
                    case "light_brown" -> ColorConstants.LIGHT_BROWN;
                    case "light_green" -> ColorConstants.LIGHT_GREEN;
                    case "light_grey", "light_gray" -> Color.LIGHT_GRAY;
                    case "light_orange" -> ColorConstants.LIGHT_ORANGE;
                    case "light_red" -> ColorConstants.LIGHT_RED;
                    case "light_yellow" -> ColorConstants.LIGHT_YELLOW;
                    case "magenta" -> Color.MAGENTA;
                    case "orange" -> Color.ORANGE;
                    case "pink" -> Color.PINK;
                    case "purple" -> ColorConstants.PURPLE;
                    case "red" -> Color.RED;
                    case "white" -> Color.WHITE;
                    case "yellow" -> Color.YELLOW;
                    default -> retColor;
                };
            }
        }

        return retColor;
    }

    public static long parseTime(final String asString) {
        if (asString.split(",").length > 1) {
            final String[] parts = asString.split(",");
            long totalTime = 0L;
            for (final var part : parts) {
                final String strippedPart = part.trim().toLowerCase();
                final int splitIndex = findFirstLetter(strippedPart);

                final String quantityStr = strippedPart.substring(0, splitIndex);
                int quantity = 0;
                try {
                    quantity = Integer.parseInt(quantityStr);
                } catch (final NumberFormatException e) {
                    quantity = 1;
                }

                final String unit = strippedPart.substring(splitIndex);
                totalTime += switch (unit) {
                    case "millis", "milli", "ms", "millisecond", "milliseconds", "milisecond", "miliseconds" -> quantity;
                    case "secs", "sec", "s", "second", "seconds" -> quantity * Constants.SECOND_TO_MILLI;
                    case "min", "mins", "minutes", "minute", "m" -> quantity * Constants.MINUTE_TO_MILLI;
                    case "hr", "h", "hrs", "hours", "hour" -> quantity * Constants.HOUR_TO_MILLI;
                    case "d", "dy", "days", "day", "ds" -> quantity * Constants.DAY_TO_MILLI;
                    case "w", "ws", "wks", "wk", "weeks", "week" -> quantity * Constants.WEEK_TO_MILLI;
                    case "months", "mths", "mth", "mnth", "mnths", "month" -> quantity
                            * Constants.WEEK_TO_MILLI;
                    case "yr", "year", "yrs", "y", "ys", "years" -> quantity * Constants.YEAR_TO_MILLI;
                    default -> 0L;
                };
            }

            return totalTime;
        }

        try {
            return Instant.parse(asString).toEpochMilli();
        } catch (final DateTimeParseException e) {
            try {
                return Constants.DATE_FORMAT.parse(asString).toInstant().toEpochMilli();
            } catch (final ParseException ex) {
                return 0L;
            }
        }
    }

    public static JsonElement readJsonFromUrl(final String url)
            throws IOException, JsonParseException, JsonSyntaxException {
        final InputStream is = new URL(url).openStream();
        try (var reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            final String jsonText = BotUtils.readString(reader);
            return JsonParser.parseString(jsonText);
        } finally {
            is.close();
        }
    }

    public static String readString(final Reader rd) throws IOException {
        final var sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
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
        final GuildInfo info = CoreBotUtils.GUILDS.get(guild.getIdLong()) == null ? new GuildInfo(guild)
                : CoreBotUtils.GUILDS.get(guild.getIdLong());
        info.advModderRoleID = roleID;
        CoreBotUtils.GUILDS.put(guild.getIdLong(), info);
        CoreBotUtils.writeGuildInfo();
    }

    public static void setBanThreshold(final Guild guild, int maxWarns) {
        final int kickThreshold = getKickThreshold(guild);
        final GuildInfo info = CoreBotUtils.GUILDS.get(guild.getIdLong()) == null ? new GuildInfo(guild)
                : CoreBotUtils.GUILDS.get(guild.getIdLong());
        if (maxWarns <= 0) {
            maxWarns = 5;
        }
        if (maxWarns <= kickThreshold) {
            maxWarns = kickThreshold + 1;
        }
        info.banThreshold = maxWarns;
        CoreBotUtils.GUILDS.put(guild.getIdLong(), info);
        CoreBotUtils.writeGuildInfo();
    }

    public static void setInformationForGuild(final Guild guild, final long channelID) {
        final GuildInfo info = CoreBotUtils.GUILDS.get(guild.getIdLong()) == null ? new GuildInfo(guild)
                : CoreBotUtils.GUILDS.get(guild.getIdLong());
        info.informationID = channelID;
        CoreBotUtils.GUILDS.put(guild.getIdLong(), info);
        CoreBotUtils.writeGuildInfo();
    }

    public static void setKickThreshold(final Guild guild, int maxWarns) {
        final int banThreshold = getBanThreshold(guild);
        final GuildInfo info = CoreBotUtils.GUILDS.get(guild.getIdLong()) == null ? new GuildInfo(guild)
                : CoreBotUtils.GUILDS.get(guild.getIdLong());
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
        CoreBotUtils.GUILDS.put(guild.getIdLong(), info);
        CoreBotUtils.writeGuildInfo();
    }

    public static void setModLogForGuild(final Guild guild, final long channelID) {
        final GuildInfo info = CoreBotUtils.GUILDS.get(guild.getIdLong()) == null ? new GuildInfo(guild)
                : CoreBotUtils.GUILDS.get(guild.getIdLong());
        info.modLogID = channelID;
        CoreBotUtils.GUILDS.put(guild.getIdLong(), info);
        CoreBotUtils.writeGuildInfo();
    }

    public static void setModRoleForGuild(final Guild guild, final long roleID) {
        final GuildInfo info = CoreBotUtils.GUILDS.get(guild.getIdLong()) == null ? new GuildInfo(guild)
                : CoreBotUtils.GUILDS.get(guild.getIdLong());
        info.modRoleID = roleID;
        CoreBotUtils.GUILDS.put(guild.getIdLong(), info);
        CoreBotUtils.writeGuildInfo();
    }

    public static void setMutedRoleForGuild(final Guild guild, final long roleID) {
        final GuildInfo info = CoreBotUtils.GUILDS.get(guild.getIdLong()) == null ? new GuildInfo(guild)
                : CoreBotUtils.GUILDS.get(guild.getIdLong());
        info.mutedRoleID = roleID;
        CoreBotUtils.GUILDS.put(guild.getIdLong(), info);
        CoreBotUtils.writeGuildInfo();
    }

    public static void setMuteThreshold(final Guild guild, int maxWarns) {
        final int kickThreshold = getKickThreshold(guild);
        final GuildInfo info = CoreBotUtils.GUILDS.get(guild.getIdLong()) == null ? new GuildInfo(guild)
                : CoreBotUtils.GUILDS.get(guild.getIdLong());
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
        CoreBotUtils.GUILDS.put(guild.getIdLong(), info);
        CoreBotUtils.writeGuildInfo();
    }

    public static void setPrefixForGuild(final Guild guild, final String prefix) {
        final GuildInfo info = CoreBotUtils.GUILDS.get(guild.getIdLong()) == null ? new GuildInfo(guild)
                : CoreBotUtils.GUILDS.get(guild.getIdLong());
        info.prefix = prefix;
        CoreBotUtils.GUILDS.put(guild.getIdLong(), info);
        CoreBotUtils.writeGuildInfo();
    }

    public static void setSuggestionsForGuild(final Guild guild, final long channelID) {
        final GuildInfo info = CoreBotUtils.GUILDS.get(guild.getIdLong()) == null ? new GuildInfo(guild)
                : CoreBotUtils.GUILDS.get(guild.getIdLong());
        info.suggestionsID = channelID;
        CoreBotUtils.GUILDS.put(guild.getIdLong(), info);
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
        final GuildInfo info = CoreBotUtils.GUILDS.get(guild.getIdLong()) == null ? new GuildInfo(guild)
                : CoreBotUtils.GUILDS.get(guild.getIdLong());
        info.muteThreshold = 2;
        info.kickThreshold = 3;
        info.banThreshold = 5;
        CoreBotUtils.GUILDS.put(guild.getIdLong(), info);
        CoreBotUtils.writeGuildInfo();
    }
}
