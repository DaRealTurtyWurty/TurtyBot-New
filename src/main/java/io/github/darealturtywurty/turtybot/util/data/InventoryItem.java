package io.github.darealturtywurty.turtybot.util.data;

import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import com.google.gson.JsonObject;

import io.github.darealturtywurty.turtybot.util.Constants;

public class InventoryItem {

    public final String name, url;

    public final Rarity rarity;

    public final Type itemType;

    public final boolean premiumOnly;

    private InventoryItem(final Builder builder) {
        this.name = builder.itemName;
        this.url = builder.itemUrl;
        this.rarity = builder.itemRarity;
        this.itemType = builder.itemType;
        this.premiumOnly = builder.premiumOnly;
    }

    public static Builder parse(final JsonObject object, final String loc) {
        if (!object.has("Name"))
            throw new NullPointerException(Path.of("/" + loc + ".json").toString()
                    + " was missing the required key and/or value for the \"Name\". This field is essential in order for the item to exist.");
        if (!object.has("URL"))
            throw new NullPointerException(Path.of("/" + loc + ".json").toString()
                    + " was missing the required key and/or value for the \"URL\". This field is essential in order for the item to exist.");
        if (!object.has("Type"))
            throw new NullPointerException(Path.of("/" + loc + ".json").toString()
                    + " was missing the required key and/or value for the \"Type\". This field is essential in order for the item to exist.");

        final var itemBuilder = Builder.create(object.get("Name").getAsString(),
                object.get("URL").getAsString(), Type.byName(object.get("Type").getAsString()));

        if (object.has("PremiumOnly")) {
            itemBuilder.setPremium(object.get("PremiumOnly").getAsBoolean());
        } else {
            Constants.LOGGER.warning(Path.of("/" + loc + ".json").toString()
                    + " is missing the \"PremiumOnly\" field. The value has been changed to the default value of false. "
                    + "If this was unintended, make sure to set the \"PremiumOnly\" field!");
        }

        if (object.has("Rarity")) {
            itemBuilder.setRarity(Rarity.byName(object.get("Rarity").getAsString()));
        } else {
            Constants.LOGGER.warning(Path.of("/" + loc + ".json").toString()
                    + " is missing the \"Rarity\" field. The rarity has been changed to the default value of "
                    + Rarity.COMMON + ". If this was unintended, make sure to set the \"Rarity\" field!");
        }
        return itemBuilder;
    }

    public static JsonObject serialize(final InventoryItem item) {
        final var object = new JsonObject();
        object.addProperty("Name", item.name);
        object.addProperty("URL", item.url);
        object.addProperty("Type", item.itemType.name);
        object.addProperty("PremiumOnly", item.premiumOnly);
        object.addProperty("Rarity", item.rarity.name);
        return object;
    }

    public static class Builder {
        private final String itemName, itemUrl;

        private Rarity itemRarity = Rarity.COMMON;
        private final Type itemType;
        private boolean premiumOnly = false;

        private Builder(final String name, final String itemUrl, final Type type) {
            this.itemName = name;
            this.itemUrl = itemUrl;
            this.itemType = type;
        }

        public static Builder create(final String name, final String itemUrl, final Type type) {
            return new Builder(name, itemUrl, type);
        }

        public InventoryItem build() {
            return new InventoryItem(this);
        }

        public Builder setPremium(final boolean premium) {
            this.premiumOnly = premium;
            return this;
        }

        public Builder setRarity(final Rarity rarity) {
            this.itemRarity = rarity;
            return this;
        }
    }

    public enum Rarity {
        COMMON("common"), UNCOMMON("uncommon"), RARE("rare"), EPIC("epic"), LEGENDARY("legendary"),
        MYSTICAL("mystical");

        public final String name;

        Rarity(final String name) {
            this.name = name;
        }

        public static Rarity byName(final String name) {
            try {
                return Stream.of(Rarity.values()).filter(type -> type.name.equalsIgnoreCase(name)).findFirst()
                        .get();
            } catch (final NoSuchElementException e) {
                throw new IllegalArgumentException(name + " is not a valid type!");
            }
        }
    }

    public enum Type {
        BACKGROUND_IMAGE("bgImg"), OUTLINE_IMAGE("outImg"), XP_BAR_OUTLINE_IMAGE("xpOutImg"),
        XP_BAR_EMPTY_IMAGE("xpEmptyImg"), XP_BAR_FILL_IMAGE("xpFillImg"),
        AVATAR_OUTLINE_IMAGE("avatarOutImg"), WRAPPER("wrapper");

        public final String name;

        Type(final String name) {
            this.name = name;
        }

        public static Type byName(final String name) {
            try {
                return Stream.of(Type.values()).filter(type -> type.name.equalsIgnoreCase(name)).findFirst()
                        .get();
            } catch (final NoSuchElementException e) {
                throw new IllegalArgumentException(name + " is not a valid type!");
            }
        }
    }
}
