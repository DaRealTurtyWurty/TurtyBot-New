package io.github.darealturtywurty.turtybot.util;

import static io.github.darealturtywurty.turtybot.TurtyBot.getOrCreateInstance;
import static io.github.darealturtywurty.turtybot.util.Constants.LOGGER;
import static java.lang.String.format;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;

import io.github.darealturtywurty.turtybot.TurtyBot;
import io.github.darealturtywurty.turtybot.data.GuildInfo;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public final class BotUtils {

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
		Member member = guild.getMember(user);
		if (member == null) {
			LOGGER.log(Level.WARNING, format("Cannot find user: %s [%s] in guild: %s [%s]!",
					user.getName() + "#" + user.getDiscriminator(), user.getId(), guild.getName(), guild.getId()));
			return false;
		}

		if (!member.isOwner()) {
			Role modRole = getModeratorRole(guild);
			return modRole == null ? false : member.getRoles().contains(modRole);
		}
		return true;
	}

	public static boolean isModerator(Guild guild, Member member) {
		if (member == null) {
			return false;
		}

		if (!member.isOwner()) {
			Role modRole = getModeratorRole(guild);
			return modRole == null ? false : member.getRoles().contains(modRole);
		}
		return true;
	}

	public static Role getModeratorRole(Guild guild) {
		long roleID = 0L;
		try {
			roleID = getModRoleFromGuild(guild);
		} catch (ConfigException.Missing e) {
			LOGGER.log(Level.INFO,
					format("No moderation role found in config for guild: %s [%s]!", guild.getName(), guild.getId()));
		}

		if (roleID <= 0) {
			Role modRole = guild.getRolesByName("moderator", true).get(0);
			if (modRole == null) {
				LOGGER.log(Level.WARNING,
						format("No moderation role found in guild: %s [%s]!", guild.getName(), guild.getId()));
				return null;
			}
			roleID = modRole.getIdLong();
		}

		setModRoleForGuild(guild, roleID);

		Role moderatorRole = guild.getRoleById(roleID);
		if (moderatorRole == null) {
			LOGGER.log(Level.WARNING, format("No moderation role found in guild: %s [%s]!", guild.getName(), guild.getId()));
			return null;
		}
		return moderatorRole;
	}

	public static Role getAdvModderRole(Guild guild) {
		long roleID = 0L;
		try {
			roleID = getAdvModderRoleFromGuild(guild);
		} catch (ConfigException.Missing e) {
			LOGGER.log(Level.INFO,
					format("No advanced modder role found in config for guild: %s [%s]!", guild.getName(), guild.getId()));
		}

		if (roleID <= 0) {
			Role modderRole = guild.getRolesByName("advanced modder", true).get(0);
			if (modderRole == null) {
				LOGGER.log(Level.WARNING,
						format("No advanced modder role found in guild: %s [%s]!", guild.getName(), guild.getId()));
				return null;
			}
			roleID = modderRole.getIdLong();
		}

		setAdvModderRoleForGuild(guild, roleID);

		Role modderRole = guild.getRoleById(roleID);
		if (modderRole == null) {
			LOGGER.log(Level.WARNING,
					format("No advanced modder role found in guild: %s [%s]!", guild.getName(), guild.getId()));
			return null;
		}
		return modderRole;
	}

	public static TextChannel getModLogChannel(Guild guild) {
		long channelID = 0L;
		try {
			channelID = getModLogFromGuild(guild);
		} catch (ConfigException.Missing e) {
			LOGGER.log(Level.INFO,
					format("No moderation log channel found in config for guild: %s [%s]!", guild.getName(), guild.getId()));
		}

		if (channelID <= 0) {
			TextChannel channel = guild.getTextChannelsByName("modlog", true).get(0);
			if (channel == null) {
				LOGGER.log(Level.WARNING,
						format("No moderation log channel found in guild: %s [%s]!", guild.getName(), guild.getId()));
				return null;
			}
			channelID = channel.getIdLong();
		}

		setModLogForGuild(guild, channelID);

		TextChannel modLogChannel = guild.getTextChannelById(channelID);
		if (modLogChannel == null) {
			LOGGER.log(Level.WARNING,
					format("No moderation log channel found in guild: %s [%s]!", guild.getName(), guild.getId()));
			return null;
		}
		return modLogChannel;
	}

	public static boolean isBotOwner(User user) {
		return getOwnerID() == user.getIdLong();
	}

	public static void restartApplication(JDA jda) throws Exception {
		shutdownApplication(jda);
		final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
		final File currentJar = new File(TurtyBot.class.getProtectionDomain().getCodeSource().getLocation().toURI());

		/* is it a jar file? */
		if (!currentJar.getName().endsWith(".jar"))
			return;

		/* Build command: java -jar application.jar */
		final ArrayList<String> command = new ArrayList<>();
		command.add(javaBin);
		command.add("-jar");
		command.add(currentJar.getPath());

		final ProcessBuilder builder = new ProcessBuilder(command);
		builder.start();
		System.exit(0);
	}

	public static void shutdownApplication(JDA jda) {
		jda.shutdown();
		jda.getHttpClient().connectionPool().evictAll();
		jda.getHttpClient().dispatcher().executorService().shutdown();
	}

	public static Color generateRandomColor() {
		Random rand = new Random(System.currentTimeMillis());
		final float hue = rand.nextFloat();
		// Saturation between 0.1 and 0.3
		final float saturation = (rand.nextInt(2000) + 1000) / 10000f;
		final float luminance = 0.9f;
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
