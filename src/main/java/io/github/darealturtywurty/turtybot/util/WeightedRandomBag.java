package io.github.darealturtywurty.turtybot.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

public class WeightedRandomBag<T extends Object> {

	private final List<Entry> entries = new ArrayList<>();

	private double accumulatedWeight;
	private final Random rand = new Random();
	public void addEntry(final T object, final double weight) {
		this.accumulatedWeight += weight;
		final var entry = new Entry();
		entry.object = object;
		entry.accumulatedWeight = this.accumulatedWeight;
		this.entries.add(entry);
	}

	@Nullable
	public T getRandom() {
		final double randVal = this.rand.nextDouble() * this.accumulatedWeight;

		for (final Entry entry : this.entries) {
			if (entry.accumulatedWeight >= randVal)
				return entry.object;
		}
		return null;
	}

	private class Entry {
		double accumulatedWeight;
		T object;
	}
}