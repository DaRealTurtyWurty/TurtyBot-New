package io.github.darealturtywurty.turtybot.managers.modding_helper;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;

public class ModdingHelperManager {
	protected static final Map<Long, Pair<Integer, Integer>> USER_MAP = new HashMap<>();
	private static final int DEFAULT_THRESHOLD = 10;

	public static int decrementUser(final long userID) {
		final Entry<Long, Pair<Integer, Integer>> data = getOrCreate(userID);
		USER_MAP.put(userID, Pair.of(data.getValue().getLeft() - 1, data.getValue().getRight()));
		return data.getValue().getLeft();
	}

	public static Entry<Long, Pair<Integer, Integer>> getOrCreate(final long userID) {
		if (!USER_MAP.containsKey(userID)) {
			USER_MAP.put(userID, Pair.of(0, DEFAULT_THRESHOLD));
		}
		return USER_MAP.entrySet().stream().filter(entry -> entry.getKey() == userID).findFirst().get();
	}

	public static int increaseThreshold(final long userID, final int amount) {
		final Entry<Long, Pair<Integer, Integer>> data = getOrCreate(userID);
		USER_MAP.get(userID).setValue(data.getValue().getRight() + amount);
		return data.getValue().getRight();
	}

	public static int incrementUser(final long userID) {
		final Entry<Long, Pair<Integer, Integer>> data = getOrCreate(userID);
		USER_MAP.put(userID, Pair.of(data.getValue().getLeft() + 1, data.getValue().getRight()));
		return data.getValue().getLeft();
	}

	public static boolean isWithinThreshold(final long userID) {
		final Entry<Long, Pair<Integer, Integer>> data = getOrCreate(userID);
		return data.getValue().getLeft() > data.getValue().getRight();
	}
}
