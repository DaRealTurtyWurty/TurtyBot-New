package io.github.darealturtywurty.turtybot.commands.moderation;

import java.awt.Color;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class KickCommand implements GuildCommand {

    public static void kickMember(final Guild guild, final Member kicker, final Member toKick,
            @Nullable final Message toDelete, final String reason) {
        final var kickEmbed = new EmbedBuilder().setColor(Color.RED)
                .setTitle("You were kicked from: " + guild.getName())
                .setDescription("**Reason**: " + reason + "\n**Kicked By**: " + kicker.getAsMention());
        toKick.getUser().openPrivateChannel().queueAfter(5, TimeUnit.SECONDS, channel -> channel
                .sendMessageEmbeds(kickEmbed.build()).queue(msg -> toKick.kick(reason).queue()));

        final var kickLogEmbed = new EmbedBuilder().setColor(Color.RED)
                .setTitle(toKick.getEffectiveName() + " was kicked!")
                .setDescription("**Reason**: " + reason + "\n**Kicked By**: " + kicker.getAsMention());
        BotUtils.getModLogChannel(guild).sendMessageEmbeds(kickLogEmbed.build()).queue();

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
        return "Kicks a member from the guild.";
    }

    @Override
    public String getName() {
        return "kick";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(new OptionData(OptionType.USER, "user", "The user you want to kick", true),
                new OptionData(OptionType.STRING, "reason", "The reason you want to kick this user for",
                        false));
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        final var guild = ctx.getGuild();
        final Member member = ctx.getEvent().getOption("user").getAsMember();
        final OptionMapping reasonOption = ctx.getEvent().getOption("reason");
        final String reason = reasonOption == null ? "Unspecified" : reasonOption.getAsString();
        if (member == null || !ctx.getMember().canInteract(member)
                || !BotUtils.isModerator(guild, ctx.getMember())) {
            ctx.getEvent().deferReply(true).setContent("You do not have permission to kick this user.")
                    .mentionRepliedUser(false).queue();
            return;
        }

        if (member.getIdLong() == ctx.getMember().getIdLong()) {
            ctx.getEvent().deferReply(true).setContent("You cannot kick yourself.").mentionRepliedUser(false)
                    .queue();
            return;
        }

        kickMember(guild, ctx.getMember(), member, null, reason);
    }

    @Override
    public boolean isModeratorOnly() {
        return true;
    }
}
