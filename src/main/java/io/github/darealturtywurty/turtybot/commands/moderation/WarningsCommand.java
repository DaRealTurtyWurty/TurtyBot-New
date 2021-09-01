package io.github.darealturtywurty.turtybot.commands.moderation;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import io.github.darealturtywurty.turtybot.util.Constants;
import io.github.darealturtywurty.turtybot.util.WarnUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class WarningsCommand implements GuildCommand {

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.MODERATION;
    }

    @Override
    public String getDescription() {
        return "Lists all the warnings for a user.";
    }

    @Override
    public String getName() {
        return "warnings";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(new OptionData(OptionType.USER, "user", "User to get warnings for", false));
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        final var guild = ctx.getGuild();
        final OptionMapping userOption = ctx.getEvent().getOption("user");
        User toGetUser = null;
        Member toGetMember = null;

        if (userOption == null) {
            toGetUser = ctx.getAuthor();
            toGetMember = ctx.getMember();
        } else {
            toGetUser = userOption.getAsUser();
            toGetMember = userOption.getAsMember();
        }

        final var userWarns = WarnUtils.getUserWarns(guild, toGetUser);
        final var warnsEmbed = new EmbedBuilder()
                .setColor(toGetMember != null ? toGetMember.getColorRaw() : 0xFFFFFF)
                .setTitle("Warnings for: " + toGetMember != null ? toGetMember.getEffectiveName()
                        : toGetUser.getName())
                .setDescription(toGetMember != null ? toGetMember.getEffectiveName()
                        : toGetUser.getName() + " has " + userWarns.getNumberWarns() + " warnings!")
                .setTimestamp(Instant.now());
        final var counter = new AtomicInteger(1);
        userWarns.warns.forEach((uuid, warnInfo) -> warnsEmbed.addField(counter.getAndIncrement() + ".",
                "**UUID:** " + uuid.toString() + "\n**Warned By (ID):** " + warnInfo.left + "\n**Date:** "
                        + Constants.DATE_FORMAT.format(warnInfo.middle) + "\n**Reason:** " + warnInfo.right,
                false));

        ctx.getEvent().deferReply().addEmbeds(warnsEmbed.build()).mentionRepliedUser(false).queue();
    }
}
