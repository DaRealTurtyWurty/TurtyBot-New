package io.github.darealturtywurty.turtybot.commands.utility;

import java.util.List;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import io.github.darealturtywurty.turtybot.util.core.BotUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction;

@RegisterBotCmd
public class BotInfoCommand implements GuildCommand {

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.UTILITY;
    }

    @Override
    public String getDescription() {
        return "Gets information about the bot.";
    }

    @Override
    public String getName() {
        return "botinfo";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of();
    }

    public String getRoles(final List<Role> roleList) {
        final var roles = new StringBuilder();
        if (!roleList.isEmpty()) {
            var tempRole = roleList.get(0);
            roles.append(tempRole.getAsMention());
            for (var index = 1; index < roleList.size(); index++) {
                tempRole = roleList.get(index);
                roles.append(", " + tempRole.getAsMention());
            }
        } else {
            roles.append("None");
        }
        return roles.toString();
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        sendInfo(ctx.getJDA(), ctx.getGuild(), ctx.getEvent().deferReply());
    }

    @Override
    public boolean productionReady() {
        return true;
    }

    public void sendInfo(final JDA jda, final Guild guild, final ReplyAction action) {
        final var builder = new EmbedBuilder();
        builder.setTitle("Bot Info");
        builder.setColor(BotUtils.generateRandomPastelColor());
        builder.setDescription("Information about TurtyBot.");
        builder.setThumbnail(jda.getSelfUser().getEffectiveAvatarUrl());
        builder.addField("Bot Name:", jda.getSelfUser().getAsMention(), true);
        builder.addField("Max Upload Size:",
                Long.toString(jda.getSelfUser().getAllowedFileSize() / 1000000) + "MB", true);
        builder.addField("Time Created:", jda.getSelfUser().getTimeCreated().toLocalDate().toString(), true);
        builder.addField("Has a Private Channel?", Boolean.toString(jda.getSelfUser().hasPrivateChannel()),
                true);
        builder.addField("Has 2FA/MFA?", Boolean.toString(jda.getSelfUser().isMfaEnabled()), true);
        builder.addField("Verified Bot?", Boolean.toString(jda.getSelfUser().isVerified()), true);
        builder.addField("WebSocket ping:", Long.toString(jda.getGatewayPing()) + "ms", true);
        builder.addField("Max reconnect time:", Integer.toString(jda.getMaxReconnectDelay()) + "ms", true);
        builder.addField("No. of JSON Responses:", Long.toString(jda.getResponseTotal()), true);
        builder.addField("Shard Info:", jda.getShardInfo().getShardString(), true);
        builder.addField("Bot's Roles: ",
                getRoles(guild.getMembersByName("TurtyBot", false).get(0).getRoles()), false);
        builder.addField("Auto-reconnect?", Boolean.toString(jda.isAutoReconnect()), true);
        builder.addField("Bulk Delete Splitting?", Boolean.toString(jda.isBulkDeleteSplittingEnabled()),
                true);
        builder.addField("Account Type:", jda.getAccountType().toString(), true);

        final var strBuilder = new StringBuilder();
        for (final Guild mutualGuild : jda.getMutualGuilds()) {
            strBuilder.append(mutualGuild.getName() + ", ");
        }
        builder.addField("Mutual Guilds: ",
                strBuilder.toString().substring(0, strBuilder.toString().length() - 2), false);
        action.addEmbeds(builder.build()).mentionRepliedUser(false).queue();
    }
}
