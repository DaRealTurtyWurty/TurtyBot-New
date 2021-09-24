package io.github.darealturtywurty.turtybot.managers.levelling_system;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;

import io.github.darealturtywurty.turtybot.TurtyBot;
import io.github.darealturtywurty.turtybot.util.Constants;
import io.github.darealturtywurty.turtybot.util.data.InventoryItem;
import io.github.darealturtywurty.turtybot.util.data.InventoryItem.Rarity;
import io.github.darealturtywurty.turtybot.util.data.InventoryItem.Type;
import io.github.darealturtywurty.turtybot.util.math.WeightedRandomBag;

public final class ItemRegistry {

    protected static final Map<String, InventoryItem> REGISTRY = new HashMap<>();

    private ItemRegistry() {
        throw new IllegalAccessError("Attempted to construct registry class!");
    }

    public static List<InventoryItem> get(final boolean premium) {
        return REGISTRY.values().stream().filter(i -> i.premiumOnly == premium).collect(Collectors.toList());
    }

    public static List<InventoryItem> get(final Rarity rarity) {
        return REGISTRY.values().stream().filter(i -> i.rarity == rarity).collect(Collectors.toList());
    }

    public static List<InventoryItem> get(final String name) {
        return REGISTRY.values().stream().filter(i -> i.name.equalsIgnoreCase(name))
                .collect(Collectors.toList());
    }

    public static List<InventoryItem> get(final Type type) {
        return REGISTRY.values().stream().filter(i -> i.itemType == type).collect(Collectors.toList());
    }

    public static InventoryItem random() {
        return REGISTRY.values().toArray(new InventoryItem[0])[Constants.RANDOM.nextInt(REGISTRY.size() - 1)];
    }

    public static String readString(final InputStream stream) {
        try {
            final var result = new ByteArrayOutputStream();
            final var buffer = new byte[1024];

            for (int length; (length = stream.read(buffer)) != -1;) {
                result.write(buffer, 0, length);
            }
            return result.toString(StandardCharsets.UTF_8);
        } catch (final IOException e) {
            throw new IllegalArgumentException("There was an error reading the file: " + stream.toString());
        }
    }

    public static void register(final String loc) {
        final JsonObject object = Constants.GSON.fromJson(
                readString(TurtyBot.class.getResourceAsStream("/items/" + loc + ".json")), JsonObject.class);
        final var itemBuilder = InventoryItem.parse(object, loc);
        REGISTRY.put(loc, itemBuilder.build());
    }

    public static void registerAll(final String... locs) {
        for (final var loc : locs) {
            register(loc);
        }
    }

    public static InventoryItem scaledRandom() {
        final WeightedRandomBag<Rarity> rarities = new WeightedRandomBag<>();
        rarities.addEntry(Rarity.COMMON, 6);
        rarities.addEntry(Rarity.UNCOMMON, 5);
        rarities.addEntry(Rarity.RARE, 4);
        rarities.addEntry(Rarity.EPIC, 3);
        rarities.addEntry(Rarity.LEGENDARY, 2);
        rarities.addEntry(Rarity.MYSTICAL, 1);
        final List<InventoryItem> items = REGISTRY.values().stream()
                .filter(item -> item.rarity == rarities.getRandom()).toList();
        try {
            return items.get(Constants.RANDOM.nextInt(Math.max(0, Math.min(items.size() - 1, items.size()))));
        } catch (final IndexOutOfBoundsException e) {
            Constants.LOGGER.warning("There was an issue finding any inventory items!");
            return null;
        }
    }
}
