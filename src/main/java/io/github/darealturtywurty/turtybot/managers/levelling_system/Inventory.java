package io.github.darealturtywurty.turtybot.managers.levelling_system;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import io.github.darealturtywurty.turtybot.util.Constants;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

public class Inventory {

	protected final List<InventoryItem> items = new ArrayList<>();
	protected final Member member;

	public Inventory(final Member memberIn) {
		this.member = memberIn;
	}

	public boolean addItem(final InventoryItem item, @Nullable final TextChannel channel) {
		final var itemStream = this.items.stream();
		final boolean doesntExist = itemStream
				.noneMatch(i -> i.itemType == item.itemType || i.name.equalsIgnoreCase(item.name));
		itemStream.close();

		if (!doesntExist) {
			Constants.LOGGER.warning("You cannot add Item: \"" + item.name + "\" to " + this.member.getUser().getName() + "#"
					+ this.member.getUser().getDiscriminator() + "(" + this.member.getIdLong() + ")"
					+ "'s inventory because they already have that item!");
			return false;
		}

		this.items.add(item);
		if (channel != null) {
			channel.sendMessage("Congrats " + this.member.getEffectiveName() + ", Item: \"" + item.name + "\" of type ("
					+ item.itemType.name + ") has been added to your inventory! 🎉").queue();
		}
		Constants.LEVELLING_MANAGER.updateInventory(this.member);
		return true;
	}

	public boolean removeItem(final InventoryItem item, @Nullable final TextChannel channel) {
		final var itemStream = this.items.stream();
		final boolean doesntExist = itemStream
				.noneMatch(i -> i.itemType == item.itemType || i.name.equalsIgnoreCase(item.name));
		itemStream.close();

		if (doesntExist) {
			Constants.LOGGER.warning("You cannot remove Item: \"" + item.name + "\" from " + this.member.getUser().getName()
					+ "#" + this.member.getUser().getDiscriminator() + "(" + this.member.getIdLong() + ")"
					+ "'s inventory because they do not have that item!");
			return false;
		}

		this.items.add(item);
		if (channel != null) {
			channel.sendMessage("Oh dear " + this.member.getEffectiveName() + ", Item: \"" + item.name + "\" of type ("
					+ item.itemType.name + ") has been removed from your inventory! 😢").queue();
		}
		Constants.LEVELLING_MANAGER.updateInventory(this.member);
		return true;
	}
}
