package io.github.darealturtywurty.turtybot.util.data;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;

public class UserEmbedData {

    public final long userId;
    public final Map<String, EmbedBuilder> embeds = new HashMap<>();
    public Pair<String, EmbedBuilder> currentlyEditing;

    public UserEmbedData(final User user) {
        this.userId = user.getIdLong();
    }

    public boolean createEmbed(@Nonnull final String name, @Nonnull final EmbedBuilder embed) {
        final String adjustedName = name.trim().toLowerCase();
        if (this.embeds.containsKey(adjustedName) || adjustedName.isBlank())
            return false;
        this.embeds.put(adjustedName, embed);
        return editEmbed(adjustedName) != null;
    }

    @Nullable
    public EmbedBuilder editEmbed(@Nonnull final String name) {
        final String adjustedName = name.trim().toLowerCase();
        final EmbedBuilder embed;
        if (!this.embeds.containsKey(adjustedName) || adjustedName.isBlank())
            return null;
        embed = this.embeds.get(adjustedName);
        this.currentlyEditing = Pair.of(name, embed);
        return embed;
    }

    public boolean removeEmbed(@Nonnull final String name) {
        final String adjustedName = name.trim().toLowerCase();
        if (!this.embeds.containsKey(adjustedName) || adjustedName.isBlank())
            return false;
        this.embeds.remove(adjustedName, this.embeds.get(adjustedName));
        if (this.currentlyEditing.getLeft().equals(adjustedName)) {
            stopEditing();
        }

        return true;
    }

    public boolean stopEditing() {
        this.currentlyEditing = null;
        return true;
    }

    public void updateEmbed(final String name, final EmbedBuilder newEmbed) {
        final String adjustedName = name.trim().toLowerCase();
        this.embeds.put(adjustedName, newEmbed);
        if (this.currentlyEditing.getLeft().equals(adjustedName)) {
            this.currentlyEditing = Pair.of(adjustedName, newEmbed);
        }
    }
}
