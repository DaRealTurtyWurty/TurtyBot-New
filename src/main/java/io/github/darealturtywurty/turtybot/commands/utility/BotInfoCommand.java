package io.github.darealturtywurty.turtybot.commands.utility;

import java.awt.Color;
import java.util.List;

import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;
import io.github.darealturtywurty.turtybot.util.Constants;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;

public class BotInfoCommand implements IGuildCommand {

	@Override
	public void handle(CommandContext ctx) {
		sendInfo(ctx.getJDA(), ctx.getGuild(), ctx.getMessage());
	}

	@Override
	public String getName() {
		return "botinfo";
	}

	@Override
	public String getDescription() {
		return "Gets information about the bot.";
	}

	public void sendInfo(JDA jda, Guild guild, Message message) {
		var builder = new EmbedBuilder();
		builder.setTitle("Bot Info");
		builder.setColor(chooseColor());
		builder.setDescription("Information about TurtyBot.");
		builder.setThumbnail(jda.getSelfUser().getEffectiveAvatarUrl());
		builder.addField("Bot Name:", jda.getSelfUser().getAsMention(), true);
		builder.addField("Max Upload Size:", Long.toString(jda.getSelfUser().getAllowedFileSize() / 1000000) + "MB", true);
		builder.addField("Time Created:", jda.getSelfUser().getTimeCreated().toLocalDate().toString(), true);
		builder.addField("Has a Private Channel?", Boolean.toString(jda.getSelfUser().hasPrivateChannel()), true);
		builder.addField("Has 2FA/MFA?", Boolean.toString(jda.getSelfUser().isMfaEnabled()), true);
		builder.addField("Verified Bot?", Boolean.toString(jda.getSelfUser().isVerified()), true);
		builder.addField("WebSocket ping:", Long.toString(jda.getGatewayPing()) + "ms", true);
		builder.addField("Max reconnect time:", Integer.toString(jda.getMaxReconnectDelay()) + "ms", true);
		builder.addField("No. of JSON Responses:", Long.toString(jda.getResponseTotal()), true);
		builder.addField("Shard Info:", jda.getShardInfo().getShardString(), true);
		builder.addField("Bot's Roles: ", getRoles(guild.getMembersByName("TurtyBot", false).get(0).getRoles()), false);
		builder.addField("Auto-reconnect?", Boolean.toString(jda.isAutoReconnect()), true);
		builder.addField("Bulk Delete Splitting?", Boolean.toString(jda.isBulkDeleteSplittingEnabled()), true);
		builder.addField("Account Type:", jda.getAccountType().toString(), true);

		var strBuilder = new StringBuilder();
		for (Guild mutualGuild : jda.getMutualGuilds()) {
			strBuilder.append(mutualGuild.getName() + ", ");
		}
		builder.addField("Mutual Guilds: ", strBuilder.toString().substring(0, strBuilder.toString().length() - 2), false);
		message.reply(builder.build()).mentionRepliedUser(false).queue();
	}

	public Color chooseColor() {
		// Pastel Colors
		final var hue = Constants.RANDOM.nextFloat();
		final var saturation = 0.9f; // 1.0f for brilliant, 0.0f for dull
		final var luminance = 1.0f; // 1.0f for brighter, 0.0f for black
		return Color.getHSBColor(hue, saturation, luminance);
	}

	public String getRoles(List<Role> roleList) {
		var roles = new StringBuilder();
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
}
