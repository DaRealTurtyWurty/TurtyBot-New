package io.github.darealturtywurty.turtybot.managers.help_system;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import io.github.darealturtywurty.turtybot.managers.modding_helper.ModdingHelperManager;
import io.github.darealturtywurty.turtybot.util.BotUtils;
import io.github.darealturtywurty.turtybot.util.Constants;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.internal.utils.tuple.Pair;

public final class HelpManager {

	public static class HelpEventListener extends ListenerAdapter {

		private final boolean closing;

		public HelpEventListener() {
			this(false);
		}

		public HelpEventListener(final boolean closing) {
			this.closing = closing;
		}

		@Override
		public void onGuildMessageReceived(final GuildMessageReceivedEvent event) {
			final var channel = event.getChannel();
			final var channelID = channel.getIdLong();
			final var message = event.getMessage();

			if (channel.getName().equalsIgnoreCase("request-help")
					&& !message.getContentRaw().contains(BotUtils.getPrefixFromGuild(event.getGuild()) + "requesthelp")) {
				message.delete().queue();
			}

			if (!channel.getParent().getName().contains(SUPPORT) || event.getAuthor().isBot() || event.isWebhookMessage()
					|| !CHANNEL_STAGE_MAP.containsKey(channelID))
				return;

			final String ownerID = channel.getTopic().split("\n")[0];
			if (!event.getAuthor().getId().equalsIgnoreCase(ownerID))
				return;

			final Pair<HelpData, Integer> data = CHANNEL_STAGE_MAP.get(channelID).getLeft();
			final var helpData = data.getLeft();

			switch (data.getRight()) {
			case 0:
				helpData.setOwner(Long.parseLong(ownerID));
				helpData.setTitle(channel.getTopic().replace(ownerID + "\n", ""));
				helpData.setDescription(message.getContentRaw());
				channel.sendMessage("Please provide an image or video of the issue. "
						+ "This could be an image in-game, or an image of the error in the code. "
						+ "If you are unable to provide this information, please respond with `N/A`!").queue();
				CHANNEL_STAGE_MAP.put(channelID, Pair.of(Pair.of(helpData, 1), CHANNEL_STAGE_MAP.get(channelID).getRight()));
				break;
			case 1:
				helpData.setMedia(
						message.getEmbeds().isEmpty() ? message.getContentRaw() : message.getAttachments().get(0).getUrl());
				channel.sendMessage(
						"Please provide the `latest.log`, which can be found in `YourModFolder/run/logs/latest.log`. "
								+ "If this file does not exist or is empty, please try to re-run the game and check. "
								+ "If it is still empty, please state that.")
						.queue();
				CHANNEL_STAGE_MAP.put(channelID, Pair.of(Pair.of(helpData, 2), CHANNEL_STAGE_MAP.get(channelID).getRight()));
				break;
			case 2:
				helpData.setLogs(
						message.getEmbeds().isEmpty() ? message.getContentRaw() : message.getAttachments().get(0).getUrl());
				channel.getHistory().retrievePast(20).queue(messages -> channel.deleteMessages(messages).queue());
				CHANNEL_STAGE_MAP.put(channelID, Pair.of(Pair.of(helpData, 3), CHANNEL_STAGE_MAP.get(channelID).getRight()));
				postProblem(event.getGuild(), channelID, helpData);
				sendFormattedInformation(channel, helpData, ownerID);
				break;
			default:
				break;
			}

			if (this.closing) {
				handleClosing(channel, message, Long.parseLong(ownerID));
			}
		}

		@Override
		public void onTextChannelDelete(final TextChannelDeleteEvent event) {
			if (CHANNEL_STAGE_MAP.containsKey(event.getChannel().getIdLong())) {
				closeChannel(event.getChannel());
			}
		}
	}

	protected static final Set<Map<Long, Long>> CHANNEL_SET = new HashSet<>();

	protected static final Map<Long, Pair<Pair<HelpData, Integer>, Pair<CloseData, Integer>>> CHANNEL_STAGE_MAP = new HashMap<>();
	protected static final int MAX_CHANNEL_COUNT = 50;
	protected static final String SUPPORT = "Support ";

	private static void closeChannel(final TextChannel channel) {
		CHANNEL_STAGE_MAP.remove(channel.getIdLong());
		deleteProblemMessage(channel.getGuild(), channel.getTopic().split("\n")[0]);
		channel.delete().queue();
	}

	protected static void createChannel(final Guild guild, final User user, final String description) {
		var complete = false;
		var index = 0;
		for (final Map<Long, Long> userChannelMap : CHANNEL_SET) {
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
					final Map<Long, Long> group1 = new HashMap<>();
					group1.put(user.getIdLong(), channel.getIdLong());
					CHANNEL_SET.add(group1);
				});
	}

	protected static void deleteProblemMessage(final Guild guild, final String userID) {
		final List<TextChannel> channels = guild.getTextChannelsByName("current-problems", true);
		if (channels.isEmpty()) {
			BotUtils.getModLogChannel(guild).sendMessage("There was an error trying to get the '#current-problems' channel. "
					+ "Please ensure you have set it in the config.").queue();
			return;
		}

		final TextChannel problemsChannel = channels.get(0);
		problemsChannel.getHistory().retrievePast(100).queue(messages -> {
			final List<Message> possibleProblems = messages.stream().filter(m -> !m.getEmbeds().isEmpty())
					.filter(m -> m.getEmbeds().get(0).getFooter() != null)
					.filter(m -> m.getEmbeds().get(0).getFooter().getText().contains(userID)).collect(Collectors.toList());

			if (possibleProblems.isEmpty())
				return;
			possibleProblems.get(0).delete().queue();
		});
	}

	public static void handleClosing(final TextChannel channel, final Message message, final long ownerID) {
		Pair<CloseData, Integer> closeData = CHANNEL_STAGE_MAP.get(channel.getIdLong()).getRight();
		final Pair<HelpData, Integer> helpData = CHANNEL_STAGE_MAP.get(channel.getIdLong()).getLeft();
		if (closeData == null) {
			closeData = Pair.of(new CloseData(), 0);
			CHANNEL_STAGE_MAP.put(channel.getIdLong(), Pair.of(helpData, closeData));
		}

		switch (closeData.getRight()) {
		case 0:
			channel.sendMessage("Did you receive the help that you needed from this help channel? (Y/N)").queue();
			CHANNEL_STAGE_MAP.put(channel.getIdLong(), Pair.of(helpData, Pair.of(closeData.getLeft(), 1)));
			break;
		case 1:
			switch (message.getContentDisplay().strip().toLowerCase()) {
			case "y":
				channel.sendMessage("I am glad that you managed to receieve the support you needed!\n"
						+ "Can you provide the IDs or `@user`s of all of the users that helped you; "
						+ "If no users helped you, please reply with `N/A`.").queue();
				CHANNEL_STAGE_MAP.put(channel.getIdLong(), Pair.of(helpData, Pair.of(closeData.getLeft(), 2)));
				break;
			case "n":
				channel.sendMessage(
						"I apologise that you did not receive the support you needed! Lots of people need help and unfortunately, "
								+ "we cannot help everyone due to real world circumstances. There are also some problems that "
								+ "we may not know how to fix, as not all modders know everything. If you feel that the latter of these "
								+ "is the case, you can run `!mmd` or `!forgecord` in #bot-stuff to bring you to a server that "
								+ "has a wider range of users with a wider range of knowledge.")
						.queue();
				closeChannel(channel);
				channel.delete().queue();
				break;
			default:
				channel.sendMessage("Did you receive the help that you needed from this help channel? (Y/N)").queue();
				break;
			}
			break;
		case 2:
			final String text = message.getContentRaw();
			if (text.trim().equalsIgnoreCase("n/a")) {
				CHANNEL_STAGE_MAP.put(channel.getIdLong(), Pair.of(helpData, Pair.of(closeData.getLeft(), 3)));
				break;
			}

			final List<Member> members = new ArrayList<>();
			message.getMentionedMembers().forEach(member -> {
				if (sentMessage(channel, member.getIdLong())) {
					members.add(member);
				}
			});

			for (final String argument : message.getContentDisplay().split(" ")) {
				try {
					channel.getGuild().retrieveMemberById(Long.parseLong(argument)).queue(member -> {
						if (member != null) {
							members.add(member);
						}
					});
				} catch (final NumberFormatException ex) {
					// Skip (this is only here because I was moaned at for leaving this empty)
				}
			}

			final List<Member> verifiedMembers = members.stream()
					.filter(member -> channel.getPermissionOverrides().stream()
							.anyMatch(override -> member.hasAccess(channel) && channel.canTalk(member)))
					.collect(Collectors.toList());
			if (verifiedMembers.isEmpty()) {
				BotUtils.getModLogChannel(channel.getGuild())
						.sendMessage("User (" + ownerID + ") provided no users or invalid users!").queue();
				closeChannel(channel);
				break;
			}

			verifiedMembers.forEach(member -> {
				Constants.LEVELLING_MANAGER.setUserXP(member,
						Constants.LEVELLING_MANAGER.getUserXP(member) + Constants.RANDOM.nextInt(25) + 5);
				ModdingHelperManager.incrementUser(member.getIdLong());
			});
			closeChannel(channel);
			break;
		default:
			break;
		}
	}

	protected static boolean hasChannel(final Guild guild, final User user) {
		for (final Map<Long, Long> userMap : CHANNEL_SET) {
			if (userMap.containsKey(user.getIdLong())) {
				if (userMap.get(user.getIdLong()) != 0L
						&& !guild.getTextChannelsByName(user.getName() + "-" + user.getDiscriminator(), true).isEmpty())
					return true;
				userMap.remove(user.getIdLong());
			}
		}
		return false;
	}

	protected static void postProblem(final Guild guild, final long channelID, final HelpData details) {
		final List<TextChannel> channels = guild.getTextChannelsByName("current-problems", true);
		final TextChannel channel = channels.isEmpty() ? BotUtils.getModLogChannel(guild) : channels.get(0);
		final var owner = guild.getMemberById(details.getOwner());
		final String messageID = guild.getTextChannelById(channelID).getLatestMessageId();
		final var embed = new EmbedBuilder();
		embed.setTitle(owner.getEffectiveName() + " | " + details.getTitle());
		embed.setDescription(details.getDescription() + "\n" + "[Jump]" + "(https://discord.com/channels/" + guild.getId()
				+ "/" + channelID + "/" + messageID + ")");
		embed.setColor(BotUtils.generateRandomPastelColor());
		embed.setFooter(owner.getId(), owner.getUser().getEffectiveAvatarUrl());
		channel.sendMessageEmbeds(embed.build()).queue(message -> {
			message.addReaction("🦺").queue();
			guild.getJDA().addEventListener(
					new HelpReactionEventListener(guild.getTextChannelById(channelID), message.getIdLong()));
		});
	}

	protected static void removeChannel(final TextChannel channel) {
		CHANNEL_STAGE_MAP.remove(channel.getIdLong());
		for (final Map<Long, Long> map : CHANNEL_SET) {
			map.forEach((key, val) -> {
				if (channel.getIdLong() == key) {
					map.remove(key);
				}
			});
		}
	}

	protected static void requireCategory(final Guild guild, final int number) {
		guild.createCategory(SUPPORT + number).setPosition(guild.getCategories().size() - 2)
				.addCheck(() -> guild.getCategoriesByName(SUPPORT + number, true).isEmpty()).queue(category -> {
					category.getPermissionOverrides().forEach(override -> override.getDenied().add(Permission.VIEW_CHANNEL));
					category.upsertPermissionOverride(BotUtils.getModeratorRole(guild)).setAllow(Permission.VIEW_CHANNEL)
							.queueAfter(2, TimeUnit.SECONDS);
					category.upsertPermissionOverride(BotUtils.getAdvModderRole(guild)).setAllow(Permission.VIEW_CHANNEL)
							.queueAfter(2, TimeUnit.SECONDS);
				});
	}

	protected static void sendFormattedInformation(final TextChannel channel, final HelpData data, final String ownerID) {
		channel.sendMessage("Thank you for these details. We will now look for people who are willing to"
				+ " help you with your problem. Please be patient, not everyone is active all the time, and there"
				+ " are many people who have problems right now. Thank you for your understanding.")
				.queueAfter(1, TimeUnit.SECONDS);
		final var embed = new EmbedBuilder();
		embed.setTitle(data.getTitle());
		embed.setDescription(data.getDescription());
		embed.setColor(BotUtils.generateRandomPastelColor());
		embed.setFooter(ownerID);
		channel.sendMessageEmbeds(embed.build()).queueAfter(1, TimeUnit.SECONDS);

		channel.sendMessage("Image/Video of the issue: \n" + data.getMedia() + "\n\n" + "Logs relating to the issue: \n"
				+ data.getLogs()).queueAfter(1, TimeUnit.SECONDS);
	}

	private static boolean sentMessage(final TextChannel channel, final long userID) {
		final var sent = new AtomicBoolean(false);
		channel.getHistory().retrievePast(100).queue(messages -> sent.set(messages.stream()
				.filter(msg -> msg.getContentRaw().length() > 5).anyMatch(msg -> msg.getAuthor().getIdLong() == userID)));
		return sent.get();
	}

	protected static void setStage(final TextChannel channel, final long ownerID, final int stage) {
		channel.getManager().setTopic(ownerID + "\n" + channel.getTopic()).queue();
		final Pair<Pair<HelpData, Integer>, Pair<CloseData, Integer>> data = CHANNEL_STAGE_MAP.get(channel.getIdLong());
		if (data == null) {
			CHANNEL_STAGE_MAP.put(channel.getIdLong(), Pair.of(Pair.of(new HelpData(), stage), null));
		} else {
			CHANNEL_STAGE_MAP.put(channel.getIdLong(),
					Pair.of(Pair.of(CHANNEL_STAGE_MAP.get(channel.getIdLong()).getLeft().getLeft(),
							CHANNEL_STAGE_MAP.get(channel.getIdLong()).getLeft().getRight() + 1),
							CHANNEL_STAGE_MAP.get(channel.getIdLong()).getRight()));
		}
	}

	private HelpManager() {
		throw new IllegalAccessError("Attempted to construct Utility Class!");
	}
}
