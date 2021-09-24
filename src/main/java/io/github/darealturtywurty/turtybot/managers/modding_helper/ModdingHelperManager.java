package io.github.darealturtywurty.turtybot.managers.modding_helper;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;

import io.github.darealturtywurty.turtybot.util.core.CoreBotUtils;
import io.github.darealturtywurty.turtybot.util.data.GuildInfo;
import net.dv8tion.jda.api.entities.Guild;

public class ModdingHelperManager {
    private static final int DEFAULT_THRESHOLD = 10;

    public static int decrementUser(final Guild guild, final long userID) {
        final Entry<Long, Pair<Integer, Integer>> data = getOrCreate(guild, userID);
        getMap(guild).put(userID, Pair.of(data.getValue().getLeft() - 1, data.getValue().getRight()));
        return data.getValue().getLeft();
    }

    public static Entry<Long, Pair<Integer, Integer>> getOrCreate(final Guild guild, final long userID) {
        if (!getMap(guild).containsKey(userID)) {
            getMap(guild).put(userID, Pair.of(0, DEFAULT_THRESHOLD));
        }
        return getMap(guild).entrySet().stream().filter(entry -> entry.getKey() == userID).findFirst().get();
    }

    public static int increaseThreshold(final Guild guild, final long userID, final int amount) {
        final Entry<Long, Pair<Integer, Integer>> data = getOrCreate(guild, userID);
        getMap(guild).get(userID).setValue(data.getValue().getRight() + amount);
        return data.getValue().getRight();
    }

    public static int incrementUser(final Guild guild, final long userID) {
        final Entry<Long, Pair<Integer, Integer>> data = getOrCreate(guild, userID);
        getMap(guild).put(userID, Pair.of(data.getValue().getLeft() + 1, data.getValue().getRight()));
        return data.getValue().getLeft();
    }

    public static boolean isWithinThreshold(final Guild guild, final long userID) {
        final Entry<Long, Pair<Integer, Integer>> data = getOrCreate(guild, userID);
        return data.getValue().getLeft() > data.getValue().getRight();
    }

    private static Map<Long, Pair<Integer, Integer>> getMap(final Guild guild) {
        final GuildInfo info = CoreBotUtils.GUILDS.get(guild.getIdLong());
        return info.moddingHelpers;
    }
}
