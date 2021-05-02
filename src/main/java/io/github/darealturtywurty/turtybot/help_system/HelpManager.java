package io.github.darealturtywurty.turtybot.help_system;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import io.github.darealturtywurty.turtybot.util.BotUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.internal.utils.tuple.Pair;

public final class HelpManager {

	private HelpManager() {
	}

	protected static final Set<Map<Long, Long>> CHANNEL_SET = new HashSet<>();
	protected static final Map<Long, Pair<HelpData, Integer>> CHANNEL_STAGE_MAP = new HashMap<>();
	protected static final int MAX_CHANNEL_COUNT = 50;
	protected static final String SUPPORT = "Support ";

	protected static void requireCategory(Guild guild, int number) {
		guild.createCategory(SUPPORT + number).setPosition(guild.getCategories().size() - 2)
				.addCheck(() -> guild.getCategoriesByName(SUPPORT + number, true).isEmpty()).queue(category -> {
					category.getPermissionOverrides().forEach(override -> override.getDenied().add(Permission.VIEW_CHANNEL));
					category.upsertPermissionOverride(BotUtils.getModeratorRole(guild)).setAllow(Permission.VIEW_CHANNEL)
							.queueAfter(2, TimeUnit.SECONDS);
					category.upsertPermissionOverride(BotUtils.getAdvModderRole(guild)).setAllow(Permission.VIEW_CHANNEL)
							.queueAfter(2, TimeUnit.SECONDS);
				});
	}

	protected static boolean hasChannel(Guild guild, User user) {
		for (Map<Long, Long> userMap : CHANNEL_SET) {
			if (userMap.containsKey(user.getIdLong())) {
				if (userMap.get(user.getIdLong()) != 0L
						&& !guild.getTextChannelsByName(user.getName() + "-" + user.getDiscriminator(), true).isEmpty())
					return true;
				else
					userMap.remove(user.getIdLong());
			}
		}
		return false;
	}

	protected static void createChannel(Guild guild, User user, String description) {
		boolean complete = false;
		int index = 0;
		for (Map<Long, Long> userChannelMap : CHANNEL_SET) {
			if (userChannelMap.containsKey(user.getIdLong())
					&& !guild.getTextChannelsByName(user.getName() + "-" + user.getDiscriminator(), true).isEmpty()) {
				complete = true;
				break;
			}

			index++;
			if (guild.getCategoriesByName(SUPPORT + index, true).get(0).getTextChannels().size() >= MAX_CHANNEL_COUNT) {
				requireCategory(guild, index++);
			}

			guild.createTextChannel(user.getName() + "-" + user.getDiscriminator(),
					guild.getCategoriesByName(SUPPORT + index, true).get(0)).syncPermissionOverrides().setTopic(description)
					.queue(channel -> {
						userChannelMap.put(user.getIdLong(), channel.getIdLong());
						channel.upsertPermissionOverride(guild.getMemberById(user.getIdLong()))
								.setAllow(Permission.VIEW_CHANNEL).queue();
					});
			complete = true;
			break;
		}

		if (complete)
			return;

		if (guild.getCategoriesByName(SUPPORT + 1, true).isEmpty()) {
			requireCategory(guild, 1);
		} else if (guild.getCategoriesByName(SUPPORT + "1", true).get(0).getTextChannels().size() >= MAX_CHANNEL_COUNT) {
			requireCategory(guild, 2);
		}

		guild.createTextChannel(user.getName() + "-" + user.getDiscriminator(),
				guild.getCategoriesByName(SUPPORT + "1", true).get(0)).syncPermissionOverrides().setTopic(description)
				.queue(channel -> {
					channel.upsertPermissionOverride(guild.getMemberById(user.getIdLong())).setAllow(Permission.VIEW_CHANNEL)
							.queue();
					Map<Long, Long> group1 = new HashMap<>();
					group1.put(user.getIdLong(), channel.getIdLong());
					CHANNEL_SET.add(group1);
				});
	}

	protected static void setStage(TextChannel channel, long ownerID, int stage) {
		channel.getManager().setTopic(ownerID + "\n" + channel.getTopic()).queue();
		Pair<HelpData, Integer> data = CHANNEL_STAGE_MAP.get(channel.getIdLong());
		if (data == null)
			data = Pair.of(new HelpData(), 0);
		CHANNEL_STAGE_MAP.put(channel.getIdLong(), Pair.of(data.getLeft(), stage));
	}

	protected static void removeChannel(TextChannel channel) {
		CHANNEL_STAGE_MAP.remove(channel.getIdLong());
		for (Map<Long, Long> map : CHANNEL_SET) {
			map.forEach((key, val) -> {
				if (channel.getIdLong() == key)
					map.remove(key);
			});
		}
	}

	protected static void postProblem(Guild guild, long channelID, HelpData details) {
		List<TextChannel> channels = guild.getTextChannelsByName("current-problems", true);
		TextChannel channel = channels.isEmpty() ? BotUtils.getModLogChannel(guild) : channels.get(0);
		Member owner = guild.getMemberById(details.getOwner());
		String messageID = guild.getTextChannelById(channelID).getLatestMessageId();
		var embed = new EmbedBuilder();
		embed.setTitle(owner.getEffectiveName() + " | " + details.getTitle());
		embed.setDescription(details.getDescription() + "\n" + "[Jump]"
				+ ("(https://discord.com/channels/" + guild.getId() + "/" + channelID + "/" + messageID + ")"));
		embed.setColor(BotUtils.generateRandomColor());
		embed.setFooter(owner.getId(), owner.getUser().getEffectiveAvatarUrl());
		channel.sendMessage(embed.build()).queue(message -> {
			message.addReaction("🦺").queue();
			guild.getJDA().addEventListener(
					new HelpReactionEventListener(guild.getTextChannelById(channelID), message.getIdLong()));
		});
	}

	protected static void sendFormattedInformation(TextChannel channel, HelpData data, String ownerID) {
		channel.sendMessage("Thank you for these details. We will now look for people who are willing to"
				+ " help you with your problem. Please be patient, not everyone is active all the time, and there"
				+ " are many people who have problems right now. Thank you for your understanding.")
				.queueAfter(1, TimeUnit.SECONDS);
		var embed = new EmbedBuilder();
		embed.setTitle(data.getTitle());
		embed.setDescription(data.getDescription());
		embed.setColor(BotUtils.generateRandomColor());
		embed.setFooter(ownerID);
		channel.sendMessage(embed.build()).queueAfter(1, TimeUnit.SECONDS);

		channel.sendMessage("Image/Video of the issue: \n" + data.getMedia() + "\n\n" + "Logs relating to the issue: \n"
				+ data.getLogs()).queueAfter(1, TimeUnit.SECONDS);
	}

	protected static void deleteProblemMessage(Guild guild, String userID) {
		List<TextChannel> channels = guild.getTextChannelsByName("current-problems", true);
		if (channels.isEmpty()) {
			BotUtils.getModLogChannel(guild).sendMessage("There was an error trying to get the '#current-problems' channel. "
					+ "Please ensure you have set it in the config.").queue();
			return;
		}

		TextChannel problemsChannel = channels.get(0);
		problemsChannel.getHistory().retrievePast(100).queue(messages -> {
			List<Message> possibleProblems = messages.stream().filter(m -> !m.getEmbeds().isEmpty())
					.filter(m -> m.getEmbeds().get(0).getFooter() != null)
					.filter(m -> m.getEmbeds().get(0).getFooter().getText().contains(userID)).collect(Collectors.toList());

			if (possibleProblems.isEmpty())
				return;
			possibleProblems.get(0).delete().queue();
		});
	}

	public static class HelpEventListener extends ListenerAdapter {
		@Override
		public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
			TextChannel channel = event.getChannel();
			long channelID = channel.getIdLong();
			Message message = event.getMessage();

			if (!channel.getParent().getName().contains(SUPPORT) || event.getAuthor().isBot() || event.isWebhookMessage()
					|| !CHANNEL_STAGE_MAP.containsKey(channelID))
				return;

			String ownerID = channel.getTopic().split("\n")[0];
			if (!event.getAuthor().getId().equalsIgnoreCase(ownerID))
				return;

			Pair<HelpData, Integer> data = CHANNEL_STAGE_MAP.get(channelID);
			HelpData helpData = data.getLeft();

			switch (data.getRight()) {
			case 0:
				helpData.setOwner(Long.parseLong(ownerID));
				helpData.setTitle(channel.getTopic().replace(ownerID + "\n", ""));
				helpData.setDescription(message.getContentRaw());
				channel.sendMessage("Please provide an image or video of the issue. "
						+ "This could be an image in-game, or an image of the error in the code. "
						+ "If you are unable to provide this information, please respond with `N/A`!").queue();
				CHANNEL_STAGE_MAP.put(channelID, Pair.of(helpData, 1));
				break;
			case 1:
				helpData.setMedia(
						message.getEmbeds().isEmpty() ? message.getContentRaw() : message.getAttachments().get(0).getUrl());
				channel.sendMessage(
						"Please provide the `latest.log`, which can be found in `YourModFolder/run/logs/latest.log`. "
								+ "If this file does not exist or is empty, please try to re-run the game and check. "
								+ "If it is still empty, please state that.")
						.queue();
				CHANNEL_STAGE_MAP.put(channelID, Pair.of(helpData, 2));
				break;
			case 2:
				helpData.setLogs(
						message.getEmbeds().isEmpty() ? message.getContentRaw() : message.getAttachments().get(0).getUrl());
				channel.getHistory().retrievePast(20).queue(messages -> channel.deleteMessages(messages).queue());
				CHANNEL_STAGE_MAP.put(channelID, Pair.of(helpData, 3));
				postProblem(event.getGuild(), channelID, helpData);
				sendFormattedInformation(channel, helpData, ownerID);
				break;
			default:
				break;
			}
		}
	}
}
