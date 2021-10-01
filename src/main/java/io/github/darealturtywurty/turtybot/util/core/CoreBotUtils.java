package io.github.darealturtywurty.turtybot.util.core;

import static io.github.darealturtywurty.turtybot.util.Constants.GSON;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.github.darealturtywurty.turtybot.util.Constants;
import io.github.darealturtywurty.turtybot.util.data.GuildInfo;
import io.github.darealturtywurty.turtybot.util.data.ShowcaseInfo;
import io.github.darealturtywurty.turtybot.util.data.UserWarns;
import io.github.darealturtywurty.turtybot.util.math.Triple;
import net.dv8tion.jda.api.entities.Guild;

public final class CoreBotUtils {
    private static final Path GUILD_INFO = Path.of("guildInfo.json");

    public static final Map<Long, GuildInfo> GUILDS = new HashMap<>();

    private static Date readTime;

    private CoreBotUtils() {
        throw new IllegalAccessError("Attempted to construct utility class!");
    }

    public static void readGuildInfo(final Guild guild) {
        try {
            if (!Files.exists(GUILD_INFO))
                return;
            final JsonArray json = GSON.fromJson(Files.readString(GUILD_INFO), JsonArray.class);
            for (var guildIndex = 0; guildIndex < json.size(); guildIndex++) {
                final var guildObject = json.get(guildIndex).getAsJsonObject();
                final var info = new GuildInfo(guild);
                info.prefix = getOrDefaultJson(guildObject, "Prefix", "!").getAsString();
                info.modRoleID = getOrDefaultJson(guildObject, "ModeratorRoleID", 819294753732296776L)
                        .getAsLong();
                info.advModderRoleID = getOrDefaultJson(guildObject, "AdvancedModderRoleID",
                        835892413113434132L).getAsLong();
                info.mutedRoleID = getOrDefaultJson(guildObject, "MutedRoleID", 852502130588909568L)
                        .getAsLong();
                info.modLogID = getOrDefaultJson(guildObject, "ModLogChannelID", 820451030684008499L)
                        .getAsLong();
                info.muteThreshold = getOrDefaultJson(guildObject, "MuteThreshold", 2).getAsInt();
                info.kickThreshold = getOrDefaultJson(guildObject, "KickThreshold", 3).getAsInt();
                info.banThreshold = getOrDefaultJson(guildObject, "BanThreshold", 5).getAsInt();

                final var usersWarnsArray = getOrDefaultJson(guildObject, "UserWarns", new JsonArray())
                        .getAsJsonArray();
                usersWarnsArray.forEach(object -> {
                    final var user = object.getAsJsonObject();
                    final var userID = user.get("ID").getAsLong();
                    final var userWarns = user.get("Warns").getAsJsonArray();
                    final Map<UUID, Triple<Long, Date, String>> warnMap = new HashMap<>();
                    userWarns.forEach(warning -> {
                        final var warningObj = warning.getAsJsonObject();
                        final var uuid = UUID.fromString(warningObj.get("UUID").getAsString());
                        final var warner = warningObj.get("Warner").getAsLong();
                        Date date = null;
                        try {
                            date = Constants.DATE_FORMAT.parse(warningObj.get("Date").getAsString());
                        } catch (final ParseException e) {
                            CoreBotUtils.readTime = Date.from(Instant.now());
                            date = CoreBotUtils.readTime;
                        }
                        final var reason = warningObj.get("Reason").getAsString();
                        warnMap.put(uuid, new Triple<>(warner, date, reason));
                    });
                    final var userWarnsObj = new UserWarns(userID, warnMap);
                    info.userWarns.put(userID, userWarnsObj);
                });

                info.showcasesID = getOrDefaultJson(guildObject, "ShowcasesChannelID", 820454246923108352L)
                        .getAsLong();
                info.starboardID = getOrDefaultJson(guildObject, "StarboardChannelID", 820451994606239765L)
                        .getAsLong();
                info.minimumStars = getOrDefaultJson(guildObject, "MinimumStars", 8).getAsInt();
                info.includeBotStar = getOrDefaultJson(guildObject, "ShouldIncludeBotStar", true)
                        .getAsBoolean();
                info.enableStarboard = getOrDefaultJson(guildObject, "StarboardEnabled", true).getAsBoolean();

                final var stages = getOrDefaultJson(guildObject, "StarboardStages", new JsonObject())
                        .getAsJsonObject();
                info.stages[0] = stages.get("Stage0").getAsInt();
                info.stages[1] = stages.get("Stage2").getAsInt();
                info.stages[2] = stages.get("Stage3").getAsInt();
                info.stages[3] = stages.get("Stage4").getAsInt();
                info.stages[4] = stages.get("Stage5").getAsInt();

                final var showcaseInfos = getOrDefaultJson(guildObject, "ShowcaseInfo", new JsonArray())
                        .getAsJsonArray();
                showcaseInfos.forEach(object -> {
                    final var jsonObj = object.getAsJsonObject();
                    final var originalMessageID = jsonObj.get("OriginalMessageID").getAsLong();
                    final var starCount = jsonObj.get("StarCount").getAsInt();
                    final var starboardMessageID = jsonObj.get("StarboardMessageID").getAsLong();

                    final var showcaseInfo = new ShowcaseInfo(originalMessageID);
                    showcaseInfo.starboardMessageID = starboardMessageID;
                    showcaseInfo.setStars(starCount);

                    info.showcaseInfos.put(originalMessageID, showcaseInfo);
                });
                GUILDS.put(guild.getIdLong(), info);
            }
        } catch (final IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static void writeGuildInfo() {
        try {
            if (!Files.exists(GUILD_INFO)) {
                Files.createFile(GUILD_INFO);
            }
            final var json = new JsonArray();
            GUILDS.forEach((guild, info) -> {
                final var newGuildInfo = GUILDS.get(guild);
                final var guildObject = new JsonObject();
                guildObject.addProperty("GuildID", guild);
                guildObject.addProperty("Prefix", newGuildInfo.prefix);
                guildObject.addProperty("ModeratorRoleID", newGuildInfo.modRoleID);
                guildObject.addProperty("ModLogChannelID", newGuildInfo.modLogID);
                guildObject.addProperty("AdvancedModderRoleID", newGuildInfo.advModderRoleID);
                guildObject.addProperty("MutedRoleID", newGuildInfo.mutedRoleID);
                guildObject.addProperty("MuteThreshold", newGuildInfo.muteThreshold);
                guildObject.addProperty("KickThreshold", newGuildInfo.kickThreshold);
                guildObject.addProperty("BanThreshold", newGuildInfo.banThreshold);

                final var userWarnsArray = new JsonArray();
                newGuildInfo.userWarns.forEach((userID, userWarns) -> {
                    final var user = new JsonObject();
                    user.addProperty("ID", userWarns.userID);

                    final var userWarnings = new JsonArray();
                    userWarns.warns.forEach((uuid, warnInfo) -> {
                        final var warn = new JsonObject();
                        warn.addProperty("UUID", uuid.toString());
                        warn.addProperty("Warner", warnInfo.left);
                        warn.addProperty("Date", Constants.DATE_FORMAT.format(warnInfo.middle));
                        warn.addProperty("Reason", warnInfo.right);
                        userWarnings.add(warn);

                        if (warnInfo.middle.equals(CoreBotUtils.readTime)) {
                            Constants.LOGGER.severe(
                                    "Experienced an error with the warning date-time: " + uuid.toString()
                                            + " | " + Constants.DATE_FORMAT.format(warnInfo.middle));
                        }
                    });
                    user.add("Warns", userWarnings);
                    userWarnsArray.add(user);
                });
                guildObject.add("UserWarns", userWarnsArray);

                guildObject.addProperty("ShowcasesChannelID", newGuildInfo.showcasesID);
                guildObject.addProperty("StarboardChannelID", newGuildInfo.starboardID);
                guildObject.addProperty("MinimumStars", newGuildInfo.minimumStars);
                guildObject.addProperty("ShouldIncludeBotStar", newGuildInfo.includeBotStar);
                guildObject.addProperty("StarboardEnabled", newGuildInfo.enableStarboard);

                final var stages = new JsonObject();
                for (var stage = 0; stage < newGuildInfo.stages.length; stage++) {
                    stages.addProperty("Stage" + stage, newGuildInfo.stages[stage]);
                }

                guildObject.add("StarboardStages", stages);

                final var showcaseInfos = new JsonArray();
                newGuildInfo.showcaseInfos.forEach((originalMessageID, showcaseInfo) -> {
                    final var showcaseObj = new JsonObject();
                    showcaseObj.addProperty("OriginalMessageID", originalMessageID);
                    showcaseObj.addProperty("StarCount", showcaseInfo.getStars());
                    showcaseObj.addProperty("StarboardMessageID", showcaseInfo.starboardMessageID);
                    showcaseInfos.add(showcaseObj);
                });

                guildObject.add("ShowcaseInfo", showcaseInfos);

                json.add(guildObject);
            });
            Files.writeString(GUILD_INFO, GSON.toJson(json));
        } catch (final IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static JsonElement getOrDefaultJson(final JsonObject object, final String name,
            final Boolean defaultValue) {
        JsonElement obj = object.get(name);
        if (obj == null) {
            object.addProperty(name, defaultValue);
            obj = object.get(name);
        }
        return obj;
    }

    private static JsonElement getOrDefaultJson(final JsonObject object, final String name,
            final Character defaultValue) {
        JsonElement obj = object.get(name);
        if (obj == null) {
            object.addProperty(name, defaultValue);
            obj = object.get(name);
        }
        return obj;
    }

    private static JsonElement getOrDefaultJson(final JsonObject object, final String name,
            final JsonElement defaultValue) {
        JsonElement obj = object.get(name);
        if (obj == null) {
            object.add(name, defaultValue);
            obj = object.get(name);
        }
        return obj;
    }

    private static JsonElement getOrDefaultJson(final JsonObject object, final String name,
            final Number defaultValue) {
        JsonElement obj = object.get(name);
        if (obj == null) {
            object.addProperty(name, defaultValue);
            obj = object.get(name);
        }

        return obj;
    }

    private static JsonElement getOrDefaultJson(final JsonObject object, final String name,
            final String defaultValue) {
        JsonElement obj = object.get(name);
        if (obj == null) {
            object.addProperty(name, defaultValue);
            obj = object.get(name);
        }
        return obj;
    }
}