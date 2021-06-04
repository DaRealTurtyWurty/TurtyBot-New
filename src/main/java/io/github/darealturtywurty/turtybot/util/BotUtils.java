package io.github.darealturtywurty.turtybot.util;

import static io.github.darealturtywurty.turtybot.TurtyBot.getOrCreateInstance;
import static io.github.darealturtywurty.turtybot.util.Constants.LOGGER;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;

import io.github.darealturtywurty.turtybot.TurtyBot;
import io.github.darealturtywurty.turtybot.data.GuildInfo;
import io.github.darealturtywurty.turtybot.data.UserWarns;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public final class BotUtils {

	private static Role mutedRole;

	private BotUtils() {
		throw new UnsupportedOperationException("Cannot construct a Utility Class");
	}

	public static String getPrefixFromGuild(Guild guild) {
		return getOrCreateInstance().guilds.get(guild) == null ? "!" : getOrCreateInstance().guilds.get(guild).prefix;
	}

	public static void setPrefixForGuild(Guild guild, String prefix) {
		GuildInfo info = getOrCreateInstance().guilds.get(guild) == null ? new GuildInfo(guild)
				: getOrCreateInstance().guilds.get(guild);
		info.prefix = prefix;
		getOrCreateInstance().guilds.put(guild, info);
		getOrCreateInstance().writeGuildInfo();
	}

	public static int getMuteThreshold(Guild guild) {
		return getOrCreateInstance().guilds.get(guild) == null ? 2 : getOrCreateInstance().guilds.get(guild).muteThreshold;
	}

	public static void setMuteThreshold(Guild guild, int maxWarns) {
		int kickThreshold = getKickThreshold(guild);
		TurtyBot bot = getOrCreateInstance();
		GuildInfo info = bot.guilds.get(guild) == null ? new GuildInfo(guild) : bot.guilds.get(guild);
		if (maxWarns <= 0)
			maxWarns = 2;
		if (maxWarns >= kickThreshold && kickThreshold > 1)
			maxWarns = kickThreshold - 1;
		else if (kickThreshold <= 1) {
			resetThresholds(guild);
			return;
		}
		info.muteThreshold = maxWarns;
		bot.guilds.put(guild, info);
		bot.writeGuildInfo();
	}

	public static int getKickThreshold(Guild guild) {
		return getOrCreateInstance().guilds.get(guild) == null ? 3 : getOrCreateInstance().guilds.get(guild).kickThreshold;
	}

	public static void setKickThreshold(Guild guild, int maxWarns) {
		int banThreshold = getBanThreshold(guild);
		TurtyBot bot = getOrCreateInstance();
		GuildInfo info = bot.guilds.get(guild) == null ? new GuildInfo(guild) : bot.guilds.get(guild);
		if (maxWarns <= 0)
			maxWarns = 3;
		if (maxWarns >= banThreshold && banThreshold > 1)
			maxWarns = banThreshold - 1;
		else if (banThreshold <= 1) {
			resetThresholds(guild);
			return;
		}
		info.kickThreshold = maxWarns;
		bot.guilds.put(guild, info);
		bot.writeGuildInfo();
	}

	public static int getBanThreshold(Guild guild) {
		return getOrCreateInstance().guilds.get(guild) == null ? 5 : getOrCreateInstance().guilds.get(guild).banThreshold;
	}

	public static void setBanThreshold(Guild guild, int maxWarns) {
		int kickThreshold = getKickThreshold(guild);
		TurtyBot bot = getOrCreateInstance();
		GuildInfo info = bot.guilds.get(guild) == null ? new GuildInfo(guild) : bot.guilds.get(guild);
		if (maxWarns <= 0)
			maxWarns = 5;
		if (maxWarns <= kickThreshold)
			maxWarns = kickThreshold + 1;
		info.banThreshold = maxWarns;
		bot.guilds.put(guild, info);
		bot.writeGuildInfo();
	}

	private static void resetThresholds(Guild guild) {
		TurtyBot bot = getOrCreateInstance();
		GuildInfo info = bot.guilds.get(guild) == null ? new GuildInfo(guild) : bot.guilds.get(guild);
		info.muteThreshold = 2;
		info.kickThreshold = 3;
		info.banThreshold = 5;
		bot.guilds.put(guild, info);
		bot.writeGuildInfo();
	}

	public static long getModRoleFromGuild(Guild guild) {
		return getOrCreateInstance().guilds.get(guild).modRoleID;
	}

	public static void setModRoleForGuild(Guild guild, long roleID) {
		GuildInfo info = getOrCreateInstance().guilds.get(guild) == null ? new GuildInfo(guild)
				: getOrCreateInstance().guilds.get(guild);
		info.modRoleID = roleID;
		getOrCreateInstance().guilds.put(guild, info);
		getOrCreateInstance().writeGuildInfo();
	}

	public static long getAdvModderRoleFromGuild(Guild guild) {
		return getOrCreateInstance().guilds.get(guild).advModderRoleID;
	}

	public static void setAdvModderRoleForGuild(Guild guild, long roleID) {
		GuildInfo info = getOrCreateInstance().guilds.get(guild) == null ? new GuildInfo(guild)
				: getOrCreateInstance().guilds.get(guild);
		info.advModderRoleID = roleID;
		getOrCreateInstance().guilds.put(guild, info);
		getOrCreateInstance().writeGuildInfo();
	}

	public static long getModLogFromGuild(Guild guild) {
		return getOrCreateInstance().guilds.get(guild).modLogID;
	}

	public static void setModLogForGuild(Guild guild, long channelID) {
		GuildInfo info = getOrCreateInstance().guilds.get(guild) == null ? new GuildInfo(guild)
				: getOrCreateInstance().guilds.get(guild);
		info.modLogID = channelID;
		getOrCreateInstance().guilds.put(guild, info);
		getOrCreateInstance().writeGuildInfo();
	}

	public static boolean isModerator(Guild guild, User user) {
		var member = guild.getMember(user);
		if (member == null) {
			LOGGER.log(Level.WARNING, "Cannot find user: {0} [{1}] in guild: {2} [{3}]!", new Object[] {
					user.getName() + "#" + user.getDiscriminator(), user.getId(), guild.getName(), guild.getId() });
			return false;
		}

		if (!member.isOwner()) {
			var modRole = getModeratorRole(guild);
			return modRole != null && member.getRoles().contains(modRole);
		}
		return true;
	}

	public static boolean isModerator(Guild guild, Member member) {
		if (member == null) {
			return false;
		}

		if (!member.isOwner()) {
			var modRole = getModeratorRole(guild);
			return modRole != null && member.getRoles().contains(modRole);
		}
		return true;
	}

	public static Role getModeratorRole(Guild guild) {
		var roleID = 0L;
		try {
			roleID = getModRoleFromGuild(guild);
		} catch (ConfigException.Missing e) {
			LOGGER.log(Level.INFO, "No Moderation Role found in config for guild: {0} [{1}]!",
					new Object[] { guild.getName(), guild.getId() });
		}

		if (roleID <= 0) {
			var modRole = guild.getRolesByName("moderator", true).get(0);
			if (modRole == null) {
				LOGGER.log(Level.WARNING, "No Moderation Role found in guild: {0} [{1}]!",
						new Object[] { guild.getName(), guild.getId() });
				return null;
			}
			roleID = modRole.getIdLong();
		}

		setModRoleForGuild(guild, roleID);

		var moderatorRole = guild.getRoleById(roleID);
		if (moderatorRole == null) {
			LOGGER.log(Level.WARNING, "No Moderation Role found in guild: {0} [{1}]!",
					new Object[] { guild.getName(), guild.getId() });
		}
		return moderatorRole;
	}

	public static Role getAdvModderRole(Guild guild) {
		var roleID = 0L;
		try {
			roleID = getAdvModderRoleFromGuild(guild);
		} catch (ConfigException.Missing e) {
			LOGGER.log(Level.INFO, "No Advanced Modder Role found in config for guild: {0} [{1}]!",
					new Object[] { guild.getName(), guild.getId() });
		}

		if (roleID <= 0) {
			var modderRole = guild.getRolesByName("advanced modder", true).get(0);
			if (modderRole == null) {
				LOGGER.log(Level.WARNING, "No Advanced Modder Role found in guild: {0} [{1}]!",
						new Object[] { guild.getName(), guild.getId() });
				return null;
			}
			roleID = modderRole.getIdLong();
		}

		setAdvModderRoleForGuild(guild, roleID);

		var modderRole = guild.getRoleById(roleID);
		if (modderRole == null) {
			LOGGER.log(Level.WARNING, "No Advanced Modder Role found in guild: {0} [{1}]!",
					new Object[] { guild.getName(), guild.getId() });
			return null;
		}
		return modderRole;
	}

	public static TextChannel getModLogChannel(Guild guild) {
		var channelID = 0L;
		try {
			channelID = getModLogFromGuild(guild);
		} catch (ConfigException.Missing e) {
			LOGGER.log(Level.INFO, "No Moderation Log Channel found in config for guild: {0} [{1}]!",
					new Object[] { guild.getName(), guild.getId() });
		}

		if (channelID <= 0) {
			var channel = guild.getTextChannelsByName("mod-log", true).get(0);
			if (channel == null) {
				LOGGER.log(Level.WARNING, "No Moderation Log Channel found in guild: {0} [{1}]!",
						new Object[] { guild.getName(), guild.getId() });
				return null;
			}
			channelID = channel.getIdLong();
		}

		setModLogForGuild(guild, channelID);

		var modLogChannel = guild.getTextChannelById(channelID);
		if (modLogChannel == null) {
			LOGGER.log(Level.WARNING, "No Moderation Log Channel found in guild: {0} [{1}]!",
					new Object[] { guild.getName(), guild.getId() });
			return null;
		}
		return modLogChannel;
	}

	public static void setMutedRoleForGuild(Guild guild, long roleID) {
		GuildInfo info = getOrCreateInstance().guilds.get(guild) == null ? new GuildInfo(guild)
				: getOrCreateInstance().guilds.get(guild);
		info.mutedRoleID = roleID;
		getOrCreateInstance().guilds.put(guild, info);
		getOrCreateInstance().writeGuildInfo();
	}

	public static long getMutedRoleFromGuild(Guild guild) {
		return getOrCreateInstance().guilds.get(guild).mutedRoleID;
	}

	public static Role getMutedRole(Guild guild) {
		var roleID = 0L;
		try {
			roleID = getMutedRoleFromGuild(guild);
		} catch (ConfigException.Missing e) {
			LOGGER.log(Level.INFO, "No Muted Role found in config for guild: {0} [{1}]!",
					new Object[] { guild.getName(), guild.getId() });
		}

		if (roleID <= 0L) {
			Role mutedRole = null;
			try {
				mutedRole = guild.getRolesByName("Muted", true).get(0);
			} catch (IndexOutOfBoundsException ex) {
				LOGGER.log(Level.WARNING, "No Muted Role found in guild: {0} [{1}]!",
						new Object[] { guild.getName(), guild.getId() });
				return createMutedRole(guild);
			}
			roleID = mutedRole.getIdLong();
		}

		setMutedRoleForGuild(guild, roleID);

		var mutedRole = guild.getRoleById(roleID);
		if (mutedRole == null) {
			LOGGER.log(Level.WARNING, "No Muted Role found in guild: {0} [{1}]!",
					new Object[] { guild.getName(), guild.getId() });
			return createMutedRole(guild);
		}
		return mutedRole;
	}

	private static Role createMutedRole(Guild guild) {
		guild.createRole().setName("Muted").setColor(Color.DARK_GRAY).setMentionable(false)
				.setPermissions(Permission.EMPTY_PERMISSIONS).setHoisted(true)
				.queue(r -> guild.getTextChannels().forEach(channel -> channel.createPermissionOverride(r)
						.deny(Permission.MESSAGE_WRITE).queue(role -> mutedRole = role.getRole())));
		var role = mutedRole;
		mutedRole = null;
		return role;
	}

	public static boolean isBotOwner(User user) {
		return getOwnerID() == user.getIdLong();
	}

	public static void restartApplication(JDA jda) throws URISyntaxException, IOException {
		shutdownApplication(jda);
		final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
		final var currentJar = new File(TurtyBot.class.getProtectionDomain().getCodeSource().getLocation().toURI());

		/* is it a jar file? */
		if (!currentJar.getName().endsWith(".jar"))
			return;

		/* Build command: java -jar application.jar */
		final ArrayList<String> command = new ArrayList<>();
		command.add(javaBin);
		command.add("-jar");
		command.add(currentJar.getPath());

		final var builder = new ProcessBuilder(command);
		builder.start();
		System.exit(0);
	}

	public static void shutdownApplication(JDA jda) {
		TurtyBot.getOrCreateInstance().writeGuildInfo();
		jda.shutdown();
		jda.getHttpClient().connectionPool().evictAll();
		jda.getHttpClient().dispatcher().executorService().shutdown();
		System.exit(0);
	}

	public static Color generateRandomColor() {
		var rand = new Random(System.currentTimeMillis());
		final var hue = rand.nextFloat();
		// Saturation between 0.1 and 0.3
		final var saturation = (rand.nextInt(2000) + 1000) / 10000f;
		final var luminance = 0.9f;
		return Color.getHSBColor(hue, saturation, luminance);
	}

	public static Config getImportantConfig() {
		return Constants.CONFIG.getConfig("important");
	}

	public static String getBotToken() {
		try {
			return getImportantConfig().getString("BotToken");
		} catch (ConfigException.Missing e) {
			LOGGER.log(Level.WARNING, "No Bot Token found in config!");
			throw new IllegalArgumentException(e);
		}
	}

	public static String getGithubToken() {
		try {
			return getImportantConfig().getString("GithubToken");
		} catch (ConfigException.Missing e) {
			LOGGER.log(Level.WARNING, "No GitHub Token found in config!");
			throw new IllegalArgumentException(e);
		}
	}

	public static long getOwnerID() {
		try {
			return getImportantConfig().getLong("OwnerID");
		} catch (ConfigException.Missing e) {
			LOGGER.log(Level.WARNING, "No Bot Owner found in config!");
			return Constants.DEFAULT_OWNER_ID;
		}
	}

	public static String trueFalseToYesNo(boolean value) {
		return String.valueOf(value).replace("true", "Yes").replace("false", "No");
	}

	public static Member getMember(Guild guild, User user) {
		guild.findMembers(member -> member.getEffectiveName().contains(user.getName())
				|| user.getName().contains(member.getEffectiveName()));
		return guild.getMember(user);
	}

	public static Member getMember(Guild guild, UserData userData) {
		return guild.getMember(userData.getUser());
	}

	public static final class StarboardUtils {

		private StarboardUtils() {
			throw new UnsupportedOperationException("Cannot construct a Utility Class");
		}

		public static void setIncludeBotStar(Guild guild, boolean include) {
			TurtyBot bot = getOrCreateInstance();
			GuildInfo info = bot.guilds.get(guild) == null ? new GuildInfo(guild) : bot.guilds.get(guild);
			info.includeBotStar = include;
			bot.guilds.put(guild, info);
			bot.writeGuildInfo();
		}

		public static boolean includesBotStar(Guild guild) {
			TurtyBot bot = getOrCreateInstance();
			return bot.guilds.get(guild) != null && bot.guilds.get(guild).includeBotStar;
		}

		public static void setStarboardEnabled(Guild guild, boolean enabled) {
			TurtyBot bot = getOrCreateInstance();
			GuildInfo info = bot.guilds.get(guild) == null ? new GuildInfo(guild) : bot.guilds.get(guild);
			info.enableStarboard = enabled;
			bot.guilds.put(guild, info);
			bot.writeGuildInfo();
		}

		public static boolean isStarboardEnabled(Guild guild) {
			TurtyBot bot = getOrCreateInstance();
			return bot.guilds.get(guild) != null && bot.guilds.get(guild).enableStarboard;
		}

		public static void setMinumumStars(Guild guild, int minimum) {
			TurtyBot bot = getOrCreateInstance();
			GuildInfo info = bot.guilds.get(guild) == null ? new GuildInfo(guild) : bot.guilds.get(guild);
			if (minimum <= 0)
				minimum = 5;
			info.minimumStars = minimum;
			bot.guilds.put(guild, info);
			bot.writeGuildInfo();
		}

		public static int getMinimumStars(Guild guild) {
			TurtyBot bot = getOrCreateInstance();
			return bot.guilds.get(guild) == null ? 5 : bot.guilds.get(guild).minimumStars;
		}

		public static long getStarboardFromGuild(Guild guild) {
			TurtyBot bot = getOrCreateInstance();
			return bot.guilds.get(guild) == null ? 0L : bot.guilds.get(guild).starboardID;
		}

		public static TextChannel getStarboardChannel(Guild guild) {
			var channelID = 0L;
			try {
				channelID = getStarboardFromGuild(guild);
			} catch (ConfigException.Missing e) {
				LOGGER.log(Level.INFO, "No Starboard Channel found in config for guild: {0} [{1}]!",
						new Object[] { guild.getName(), guild.getId() });
			}

			if (channelID <= 0) {
				var channel = guild.getTextChannelsByName("starboard", true).get(0);
				if (channel == null) {
					LOGGER.log(Level.WARNING, "No Starboard Channel found in guild: {0} [{1}]!",
							new Object[] { guild.getName(), guild.getId() });
					return null;
				}
				channelID = channel.getIdLong();
			}

			setStarboardChannel(guild, channelID);

			var starboardChannel = guild.getTextChannelById(channelID);
			if (starboardChannel == null) {
				LOGGER.log(Level.WARNING, "No Starboard Channel found in guild: {0} [{1}]!",
						new Object[] { guild.getName(), guild.getId() });
				return null;
			}
			return starboardChannel;
		}

		public static void setStarboardChannel(Guild guild, long channelID) {
			TurtyBot bot = getOrCreateInstance();
			GuildInfo info = bot.guilds.get(guild) == null ? new GuildInfo(guild) : bot.guilds.get(guild);
			info.starboardID = channelID;
			bot.guilds.put(guild, info);
			bot.writeGuildInfo();
		}

		public static long getShowcasesFromGuild(Guild guild) {
			TurtyBot bot = getOrCreateInstance();
			return bot.guilds.get(guild) == null ? 0L : bot.guilds.get(guild).showcasesID;
		}

		public static TextChannel getShowcasesChannel(Guild guild) {
			var channelID = 0L;
			try {
				channelID = getShowcasesFromGuild(guild);
			} catch (ConfigException.Missing e) {
				LOGGER.log(Level.INFO, "No Showcases Channel found in config for guild: {0} [{1}]!",
						new Object[] { guild.getName(), guild.getId() });
			}

			if (channelID <= 0) {
				var channel = guild.getTextChannelsByName("showcases", true).get(0);
				if (channel == null) {
					LOGGER.log(Level.WARNING, "No Showcases Channel found in guild: {0} [{1}]!",
							new Object[] { guild.getName(), guild.getId() });
					return null;
				}
				channelID = channel.getIdLong();
			}

			setShowcasesChannel(guild, channelID);

			var showcasesChannel = guild.getTextChannelById(channelID);
			if (showcasesChannel == null) {
				LOGGER.log(Level.WARNING, "No Showcases Channel found in guild: {0} [{1}]!",
						new Object[] { guild.getName(), guild.getId() });
				return null;
			}
			return showcasesChannel;
		}

		public static void setShowcasesChannel(Guild guild, long channelID) {
			TurtyBot bot = getOrCreateInstance();
			GuildInfo info = bot.guilds.get(guild) == null ? new GuildInfo(guild) : bot.guilds.get(guild);
			info.showcasesID = channelID;
			bot.guilds.put(guild, info);
			bot.writeGuildInfo();
		}

		public static int getStageStar(Guild guild, int stage) {
			TurtyBot bot = getOrCreateInstance();
			GuildInfo info = bot.guilds.get(guild) == null ? new GuildInfo(guild) : bot.guilds.get(guild);
			
			var count = 0;
			if (stage >= info.stages.length)
				stage = info.stages.length - 1;
			else if (stage < 0)
				stage = 0;
			count = info.stages[stage];
			
			bot.guilds.put(guild, info);
			bot.writeGuildInfo();
			return count;
		}
	}

	public static final class WarnUtils {

		private WarnUtils() {
			throw new UnsupportedOperationException("Cannot construct a Utility Class");
		}

		public static UserWarns getUserWarns(Guild guild, long userID) {
			TurtyBot bot = getOrCreateInstance();
			GuildInfo info = bot.guilds.get(guild);
			if (info == null)
				return createUserWarns(guild, userID);
			return info.userWarnMap.get(userID) == null ? createUserWarns(guild, userID) : info.userWarnMap.get(userID);
		}

		public static UserWarns createUserWarns(Guild guild, long userID) {
			TurtyBot bot = getOrCreateInstance();
			GuildInfo info = bot.guilds.get(guild);
			if (info == null) {
				info = new GuildInfo(guild);
				bot.guilds.put(guild, info);
			}
			info.userWarnMap.put(userID, new UserWarns(userID, new HashMap<>()));
			bot.writeGuildInfo();
			return info.userWarnMap.get(userID);
		}

		public static UserWarns getUserWarns(Guild guild, User user) {
			TurtyBot bot = getOrCreateInstance();
			GuildInfo info = bot.guilds.get(guild);
			if (info == null)
				return createUserWarns(guild, user);
			return info.userWarnMap.get(user.getIdLong()) == null ? createUserWarns(guild, user)
					: info.userWarnMap.get(user.getIdLong());
		}

		public static UserWarns createUserWarns(Guild guild, User user) {
			TurtyBot bot = getOrCreateInstance();
			GuildInfo info = bot.guilds.get(guild);
			if (info == null) {
				info = new GuildInfo(guild);
				bot.guilds.put(guild, info);
			}
			info.userWarnMap.put(user.getIdLong(), new UserWarns(user));
			bot.writeGuildInfo();
			return info.userWarnMap.get(user.getIdLong());
		}

		public static UserWarns getUserWarns(Guild guild, Member member) {
			TurtyBot bot = getOrCreateInstance();
			GuildInfo info = bot.guilds.get(guild);
			if (info == null)
				return createUserWarns(guild, member);
			UserWarns userWarns = info.userWarnMap.get(member.getIdLong()) == null ? createUserWarns(guild, member)
					: info.userWarnMap.get(member.getIdLong());
			if (userWarns.member == null)
				userWarns.member = member;
			return userWarns;
		}

		public static UserWarns createUserWarns(Guild guild, Member member) {
			TurtyBot bot = getOrCreateInstance();
			GuildInfo info = bot.guilds.get(guild);
			if (info == null) {
				info = new GuildInfo(guild);
				bot.guilds.put(guild, info);
			}
			info.userWarnMap.put(member.getIdLong(), new UserWarns(member));
			bot.writeGuildInfo();
			var userWarns = info.userWarnMap.get(member.getIdLong());
			if (userWarns.member == null)
				userWarns.member = member;
			return userWarns;
		}

		public static boolean removeWarnByUUID(Guild guild, Member remover, String strUUID) {
			TurtyBot bot = getOrCreateInstance();
			var uuid = UUID.fromString(strUUID);
			if (uuid == null)
				return false;

			AtomicReference<UserWarns> atomicUserWarns = new AtomicReference<>();
			bot.guilds.get(guild).userWarnMap.forEach((user, warns) -> {
				if (atomicUserWarns.get() == null && warns.warns.containsKey(uuid)) {
					atomicUserWarns.set(warns);
				}
			});

			if (atomicUserWarns.get() != null) {
				return atomicUserWarns.get().removeWarn(guild, remover, strUUID);
			}

			return atomicUserWarns.get() != null;
		}

		public static void clearWarns(Guild guild, Member clearer, Member toClear) {
			TurtyBot bot = getOrCreateInstance();
			createUserWarns(guild, toClear);
			bot.guilds.get(guild).userWarnMap.clear();

			var loggingChannel = getModLogChannel(guild);
			if (loggingChannel != null) {
				var embed = new EmbedBuilder().setColor(Color.BLUE)
						.setTitle("Warns cleared for: " + toClear.getEffectiveName())
						.setDescription("**Cleared By**: " + clearer.getAsMention()).setTimestamp(Instant.now());
				loggingChannel.sendMessage(embed.build()).queue();
			}
		}
	}

	public static class UserData {
		private final User user;

		private UserData(User user) {
			this.user = user;
		}

		public static UserData create(JDA bot, long userID) {
			return new UserData(bot.getShardManager().getUserById(userID));
		}

		public static UserData create(JDA bot, String name, String discriminator) {
			return new UserData(bot.getUserByTag(name, discriminator));
		}

		public User getUser() {
			return this.user;
		}
	}
}
