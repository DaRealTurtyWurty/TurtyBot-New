package io.github.darealturtywurty.turtybot.commands.moderation;

import java.awt.Color;
import java.util.List;

import javax.annotation.Nullable;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import io.github.darealturtywurty.turtybot.util.BotUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class UnbanCommand implements GuildCommand {

    public static void unbanUser(final Guild guild, final Member unbanner, final long toUnban,
            @Nullable final Message toDelete, final String reason) {
        guild.unban(String.valueOf(toUnban)).queue();
        final var unbanLogEmbed = new EmbedBuilder().setColor(Color.RED)
                .setTitle("User with ID: \"" + toUnban + "\" was unbanned!")
                .setDescription("**Reason**: " + reason + "\n**Unbanned By**: " + unbanner.getAsMention());
        BotUtils.getModLogChannel(guild).sendMessageEmbeds(unbanLogEmbed.build()).queue();

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
        return "Unbans a member from the guild.";
    }

    @Override
    public String getName() {
        return "unban";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(new OptionData(OptionType.USER, "user", "User to unban", true),
                new OptionData(OptionType.STRING, "reason", "Reason for unban", false));
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        final var guild = ctx.getGuild();
        final User toUnban = ctx.getEvent().getOption("user").getAsUser();
        if (!BotUtils.isModerator(guild, ctx.getMember())) {
            ctx.getEvent().deferReply(true).setContent("You do not have permission to unban this user!")
                    .mentionRepliedUser(false).queue();
            return;
        }

        if (toUnban.getIdLong() == ctx.getMember().getIdLong()) {
            ctx.getEvent().deferReply(true).setContent("You cannot unban yourself!").mentionRepliedUser(false)
                    .queue();
            return;
        }

        final OptionMapping reasonOption = ctx.getEvent().getOption("reason");
        final String reason = reasonOption == null ? "Unspecified" : reasonOption.getAsString();
        unbanUser(guild, ctx.getMember(), toUnban.getIdLong(), null, reason);
    }

    @Override
    public boolean isModeratorOnly() {
        return true;
    }
}
