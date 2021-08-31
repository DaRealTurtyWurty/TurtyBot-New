package io.github.darealturtywurty.turtybot.managers.levelling_system;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import io.github.darealturtywurty.turtybot.managers.levelling_system.InventoryItem.Rarity;
import io.github.darealturtywurty.turtybot.util.Constants;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class InventoryCommand implements GuildCommand {

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.UTILITY;
    }

    @Override
    public String getDescription() {
        return "Displays your rank card inventory.";
    }

    @Override
    public String getName() {
        return "inventory";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of();
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        final var card = Constants.LEVELLING_MANAGER.getOrCreateCard(ctx.getMember());
        final var inventory = card.inventory;
        final var embed = new EmbedBuilder();
        embed.setTitle("🎒 " + ctx.getMember().getEffectiveName() + "'s Inventory");
        embed.setDescription("__" + ctx.getMember().getEffectiveName() + " has **" + inventory.items.size()
                + "** items in their inventory. This consists of:__ \n**"
                + inventory.items.stream().filter(item -> item.rarity == Rarity.COMMON).count()
                + "** Common Items.\n**"
                + inventory.items.stream().filter(item -> item.rarity == Rarity.UNCOMMON).count()
                + "** Uncommon Items.\n**"
                + inventory.items.stream().filter(item -> item.rarity == Rarity.RARE).count()
                + "** Rare Items.\n**"
                + inventory.items.stream().filter(item -> item.rarity == Rarity.EPIC).count()
                + "** Epic Items.\n**"
                + inventory.items.stream().filter(item -> item.rarity == Rarity.LEGENDARY).count()
                + "** Legendary Items.\n**"
                + inventory.items.stream().filter(item -> item.rarity == Rarity.MYSTICAL).count()
                + "** _Mystical Items._");
        embed.addField("Common Items:",
                String.join(",", inventory.items.stream().filter(item -> item.rarity == Rarity.COMMON)
                        .map(item -> item.name).collect(Collectors.toList())),
                false);
        embed.addField("Uncommon Items:",
                String.join(",", inventory.items.stream().filter(item -> item.rarity == Rarity.UNCOMMON)
                        .map(item -> item.name).collect(Collectors.toList())),
                false);
        embed.addField("Rare Items:",
                String.join(",", inventory.items.stream().filter(item -> item.rarity == Rarity.RARE)
                        .map(item -> item.name).collect(Collectors.toList())),
                false);
        embed.addField("Epic Items:",
                String.join(",", inventory.items.stream().filter(item -> item.rarity == Rarity.EPIC)
                        .map(item -> item.name).collect(Collectors.toList())),
                false);
        embed.addField("Legendary Items:",
                String.join(",", inventory.items.stream().filter(item -> item.rarity == Rarity.LEGENDARY)
                        .map(item -> item.name).collect(Collectors.toList())),
                false);
        embed.addField("Mystical Items:",
                String.join(",", inventory.items.stream().filter(item -> item.rarity == Rarity.MYSTICAL)
                        .map(item -> item.name).collect(Collectors.toList())),
                false);
        embed.setTimestamp(Instant.now());
        embed.setAuthor(ctx.getMember().getEffectiveName(), null,
                ctx.getMember().getUser().getEffectiveAvatarUrl());
        embed.setColor(ctx.getMember().getColorRaw());
        embed.setThumbnail(ctx.getMember().getUser().getEffectiveAvatarUrl());
        ctx.getEvent().deferReply().addEmbeds(embed.build()).queue();
    }
}
