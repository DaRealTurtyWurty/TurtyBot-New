package io.github.darealturtywurty.turtybot.commands.moderation;

import java.awt.Color;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import io.github.darealturtywurty.turtybot.util.BotUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class BanCommand implements GuildCommand {
    public static void banMember(final Guild guild, final Member banner, final Member toBan,
            @Nullable final Message toDelete, final String reason) {
        final var bannedEmbed = new EmbedBuilder().setColor(Color.RED)
                .setTitle("You were banned from: " + guild.getName())
                .setDescription("**Reason**: " + reason + "\n**Banned By**: " + banner.getAsMention());
        toBan.getUser().openPrivateChannel().queueAfter(5, TimeUnit.SECONDS,
                channel -> channel.sendMessageEmbeds(bannedEmbed.build()).queue());

        final var banLogEmbed = new EmbedBuilder().setColor(Color.RED)
                .setTitle(toBan.getEffectiveName() + " was banned!")
                .setDescription("**Reason**: " + reason + "\n**Banned By**: " + banner.getAsMention());
        BotUtils.getModLogChannel(guild).sendMessageEmbeds(banLogEmbed.build())
                .queue(msg -> toBan.ban(0, reason).queue());

        if (toDelete != null) {
            toDelete.delete().queue();
        }
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.MODERATION;
    }

    @Override
    public String getDescription() {
        return "Bans a member from the guild.";
    }

    @Override
    public String getName() {
        return "ban";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(new OptionData(OptionType.USER, "to_ban", "The user to ban.", true),
                new OptionData(OptionType.STRING, "reason", "The reason for the ban", false),
                new OptionData(OptionType.INTEGER, "delete_days", "Number of days to delete user's messages",
                        false));
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        final var guild = ctx.getGuild();
        final User banner = ctx.getAuthor();
        if (!ctx.getMember().hasPermission(Permission.BAN_MEMBERS, Permission.KICK_MEMBERS)) {
            ctx.getEvent().deferReply().setContent("You do not have permission to use this command!")
                    .mentionRepliedUser(false).queue(hook -> {
                        hook.deleteOriginal().queueAfter(15, TimeUnit.SECONDS);
                        ctx.getEvent().getHook().deleteOriginal().queueAfter(15, TimeUnit.SECONDS);
                    });
            return;
        }

        final User user = ctx.getEvent().getOption("to_ban").getAsUser();
        if (user.getIdLong() == banner.getIdLong()) {
            ctx.getEvent().deferReply(true).setContent("You cannot ban yourself!").mentionRepliedUser(true)
                    .queue();
            return;
        }

        final String reason = ctx.getEvent().getOption("reason") == null ? "Unspecified"
                : ctx.getEvent().getOption("reason").getAsString();
        final AtomicInteger deleteDays = new AtomicInteger(0);
        if (ctx.getEvent().getOption("delete_days") != null) {
            deleteDays.set((int) ctx.getEvent().getOption("delete_days").getAsLong());
        }

        final var banLogEmbed = new EmbedBuilder();
        banLogEmbed.setColor(Color.RED);
        banLogEmbed.setDescription("**Reason**: " + reason + "\n**Banned By**: " + banner.getAsMention());
        final var bannedEmbed = new EmbedBuilder();
        bannedEmbed.setColor(Color.RED).setTitle("You were banned from: " + guild.getName())
                .setDescription("**Reason**: " + reason + "\n**Banned By**: " + banner.getAsMention());
        user.openPrivateChannel().queueAfter(5, TimeUnit.SECONDS,
                channel -> channel.sendMessageEmbeds(bannedEmbed.build()).queue());

        banLogEmbed.setTitle(user.getName() + " was banned!");
        banLogEmbed.setThumbnail(user.getEffectiveAvatarUrl());

        BotUtils.getModLogChannel(guild).sendMessageEmbeds(banLogEmbed.build())
                .queue(msg -> guild.ban(user, deleteDays.get(), reason));
        ctx.getEvent().deferReply(true).setContent("User was successfully banned!").queue();
    }

    @Override
    public boolean isModeratorOnly() {
        return true;
    }
}
