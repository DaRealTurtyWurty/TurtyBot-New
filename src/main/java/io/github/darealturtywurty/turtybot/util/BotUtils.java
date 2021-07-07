package io.github.darealturtywurty.turtybot.util;

import static io.github.darealturtywurty.turtybot.util.Constants.GSON;
import static io.github.darealturtywurty.turtybot.util.Constants.LOGGER;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;

import io.github.darealturtywurty.turtybot.TurtyBot;
import io.github.darealturtywurty.turtybot.data.GuildInfo;
import io.github.darealturtywurty.turtybot.data.ShowcaseInfo;
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

	public static final class CoreBotUtils {
		private static final Path GUILD_INFO = Path.of("guildInfo.json");

		public static final Map<Guild, GuildInfo> GUILDS = new HashMap<>();

		private static Date readTime;

		private static JsonElement getOrDefaultJson(final JsonObject object, final String name, final Boolean defaultValue) {
			JsonElement obj = object.get(name);
			if (obj == null) {
				object.addProperty(name, defaultValue);
				obj = object.get(name);
			}
			return obj;
		}

		private static JsonElement getOrDefaultJson(final JsonObject object, final String name,
				final Character defaultValue) {
			JsonElement obj = object.get(name);
			if (obj == null) {
				object.addProperty(name, defaultValue);
				obj = object.get(name);
			}
			return obj;
		}

		private static JsonElement getOrDefaultJson(final JsonObject object, final String name,
				final JsonElement defaultValue) {
			JsonElement obj = object.get(name);
			if (obj == null) {
				object.add(name, defaultValue);
				obj = object.get(name);
			}
			return obj;
		}

		private static JsonElement getOrDefaultJson(final JsonObject object, final String name, final Number defaultValue) {
			JsonElement obj = object.get(name);
			if (obj == null) {
				object.addProperty(name, defaultValue);
				obj = object.get(name);
			}

			return obj;
		}

		private static JsonElement getOrDefaultJson(final JsonObject object, final String name, final String defaultValue) {
			JsonElement obj = object.get(name);
			if (obj == null) {
				object.addProperty(name, defaultValue);
				obj = object.get(name);
			}
			return obj;
		}

		public static void readGuildInfo(final JDA bot) {
			try {
				if (!Files.exists(GUILD_INFO))
					return;
				final JsonArray json = GSON.fromJson(Files.readString(GUILD_INFO), JsonArray.class);
				for (var guildIndex = 0; guildIndex < json.size(); guildIndex++) {
					final var guildObject = json.get(guildIndex).getAsJsonObject();
					final var guild = bot
							.getGuildById(getOrDefaultJson(guildObject, "GuildID", 819294753732296776L).getAsLong());
					final var info = new GuildInfo(guild);
					info.prefix = getOrDefaultJson(guildObject, "Prefix", "!").getAsString();
					info.modRoleID = getOrDefaultJson(guildObject, "ModeratorRoleID", 819294753732296776L).getAsLong();
					info.advModderRoleID = getOrDefaultJson(guildObject, "AdvancedModderRoleID", 835892413113434132L)
							.getAsLong();
					info.mutedRoleID = getOrDefaultJson(guildObject, "MutedRoleID", 852502130588909568L).getAsLong();
					info.modLogID = getOrDefaultJson(guildObject, "ModLogChannelID", 820451030684008499L).getAsLong();
					info.muteThreshold = getOrDefaultJson(guildObject, "MuteThreshold", 2).getAsInt();
					info.kickThreshold = getOrDefaultJson(guildObject, "KickThreshold", 3).getAsInt();
					info.banThreshold = getOrDefaultJson(guildObject, "BanThreshold", 5).getAsInt();

					final var usersWarnsArray = getOrDefaultJson(guildObject, "UserWarns", new JsonArray()).getAsJsonArray();
					usersWarnsArray.forEach(object -> {
						final var user = object.getAsJsonObject();
						final var userID = user.get("ID").getAsLong();
						final var userWarns = user.get("Warns").getAsJsonArray();
						final Map<UUID, Triple<Long, Date, String>> warnMap = new HashMap<>();
						userWarns.forEach(warning -> {
							final var warningObj = warning.getAsJsonObject();
							final var uuid = UUID.fromString(warningObj.get("UUID").getAsString());
							final var warner = warningObj.get("Warner").getAsLong();
							Date date = null;
							try {
								date = Constants.DATE_FORMAT.parse(warningObj.get("Date").getAsString());
							} catch (final ParseException e) {
								CoreBotUtils.readTime = Date.from(Instant.now());
								date = CoreBotUtils.readTime;
							}
							final var reason = warningObj.get("Reason").getAsString();
							warnMap.put(uuid, new Triple<>(warner, date, reason));
						});
						final var userWarnsObj = new UserWarns(userID, warnMap);
						info.userWarnMap.put(userID, userWarnsObj);
					});

					info.showcasesID = getOrDefaultJson(guildObject, "ShowcasesChannelID", 820454246923108352L).getAsLong();
					info.starboardID = getOrDefaultJson(guildObject, "StarboardChannelID", 820451994606239765L).getAsLong();
					info.minimumStars = getOrDefaultJson(guildObject, "MinimumStars", 8).getAsInt();
					info.includeBotStar = getOrDefaultJson(guildObject, "ShouldIncludeBotStar", true).getAsBoolean();
					info.enableStarboard = getOrDefaultJson(guildObject, "StarboardEnabled", true).getAsBoolean();

					final var stages = getOrDefaultJson(guildObject, "StarboardStages", new JsonObject()).getAsJsonObject();
					info.stages[0] = stages.get("Stage0").getAsInt();
					info.stages[1] = stages.get("Stage2").getAsInt();
					info.stages[2] = stages.get("Stage3").getAsInt();
					info.stages[3] = stages.get("Stage4").getAsInt();
					info.stages[4] = stages.get("Stage5").getAsInt();

					final var showcaseInfos = getOrDefaultJson(guildObject, "ShowcaseInfo", new JsonArray())
							.getAsJsonArray();
					showcaseInfos.forEach(object -> {
						final var jsonObj = object.getAsJsonObject();
						final var originalMessageID = jsonObj.get("OriginalMessageID").getAsLong();
						final var starCount = jsonObj.get("StarCount").getAsInt();
						final var starboardMessageID = jsonObj.get("StarboardMessageID").getAsLong();

						final var showcaseInfo = new ShowcaseInfo(originalMessageID);
						showcaseInfo.starboardMessageID = starboardMessageID;
						showcaseInfo.setStars(starCount);

						info.showcaseInfos.put(originalMessageID, showcaseInfo);
					});
					GUILDS.put(guild, info);
				}
			} catch (final IOException e) {
				throw new IllegalArgumentException(e);
			}
		}

		public static void writeGuildInfo() {
			try {
				if (!Files.exists(GUILD_INFO)) {
					Files.createFile(GUILD_INFO);
				}
				final var json = new JsonArray();
				GUILDS.forEach((guild, info) -> {
					final var newGuildInfo = GUILDS.get(guild);
					final var guildObject = new JsonObject();
					guildObject.addProperty("GuildID", guild.getIdLong());
					guildObject.addProperty("Prefix", newGuildInfo.prefix);
					guildObject.addProperty("ModeratorRoleID", newGuildInfo.modRoleID);
					guildObject.addProperty("ModLogChannelID", newGuildInfo.modLogID);
					guildObject.addProperty("AdvancedModderRoleID", newGuildInfo.advModderRoleID);
					guildObject.addProperty("MutedRoleID", newGuildInfo.mutedRoleID);
					guildObject.addProperty("MuteThreshold", newGuildInfo.muteThreshold);
					guildObject.addProperty("KickThreshold", newGuildInfo.kickThreshold);
					guildObject.addProperty("BanThreshold", newGuildInfo.banThreshold);

					final var userWarnsArray = new JsonArray();
					newGuildInfo.userWarnMap.forEach((userID, userWarns) -> {
						final var user = new JsonObject();
						user.addProperty("ID", userWarns.userID);

						final var userWarnings = new JsonArray();
						userWarns.warns.forEach((uuid, warnInfo) -> {
							final var warn = new JsonObject();
							warn.addProperty("UUID", uuid.toString());
							warn.addProperty("Warner", warnInfo.left);
							warn.addProperty("Date", Constants.DATE_FORMAT.format(warnInfo.middle));
							warn.addProperty("Reason", warnInfo.right);
							userWarnings.add(warn);

							if (warnInfo.middle.equals(CoreBotUtils.readTime)) {
								Constants.LOGGER.severe("Experienced an error with the warning date-time: " + uuid.toString()
										+ " | " + Constants.DATE_FORMAT.format(warnInfo.middle));
							}
						});
						user.add("Warns", userWarnings);
						userWarnsArray.add(user);
					});
					guildObject.add("UserWarns", userWarnsArray);

					guildObject.addProperty("ShowcasesChannelID", newGuildInfo.showcasesID);
					guildObject.addProperty("StarboardChannelID", newGuildInfo.starboardID);
					guildObject.addProperty("MinimumStars", newGuildInfo.minimumStars);
					guildObject.addProperty("ShouldIncludeBotStar", newGuildInfo.includeBotStar);
					guildObject.addProperty("StarboardEnabled", newGuildInfo.enableStarboard);

					final var stages = new JsonObject();
					for (var stage = 0; stage < newGuildInfo.stages.length; stage++) {
						stages.addProperty("Stage" + stage, newGuildInfo.stages[stage]);
					}

					guildObject.add("StarboardStages", stages);

					final var showcaseInfos = new JsonArray();
					newGuildInfo.showcaseInfos.forEach((originalMessageID, showcaseInfo) -> {
						final var showcaseObj = new JsonObject();
						showcaseObj.addProperty("OriginalMessageID", originalMessageID);
						showcaseObj.addProperty("StarCount", showcaseInfo.getStars());
						showcaseObj.addProperty("StarboardMessageID", showcaseInfo.starboardMessageID);
						showcaseInfos.add(showcaseObj);
					});

					guildObject.add("ShowcaseInfo", showcaseInfos);

					json.add(guildObject);
				});
				Files.writeString(GUILD_INFO, GSON.toJson(json));
			} catch (final IOException e) {
				throw new IllegalArgumentException(e);
			}
		}

		private CoreBotUtils() {
			throw new IllegalAccessError("Attempted to construct utility class!");
		}
	}

	public static final class StarboardUtils {

		public static int getMinimumStars(final Guild guild) {
			return CoreBotUtils.GUILDS.get(guild) == null ? 5 : CoreBotUtils.GUILDS.get(guild).minimumStars;
		}

		public static TextChannel getShowcasesChannel(final Guild guild) {
			var channelID = 0L;
			try {
				channelID = getShowcasesFromGuild(guild);
			} catch (final ConfigException.Missing e) {
				LOGGER.log(Level.INFO, "No Showcases Channel found in config for guild: {0} [{1}]!",
						new Object[] { guild.getName(), guild.getId() });
			}

			if (channelID <= 0) {
				final var channel = guild.getTextChannelsByName("showcases", true).get(0);
				if (channel == null) {
					LOGGER.log(Level.WARNING, "No Showcases Channel found in guild: {0} [{1}]!",
							new Object[] { guild.getName(), guild.getId() });
					return null;
				}
				channelID = channel.getIdLong();
			}

			setShowcasesChannel(guild, channelID);

			final var showcasesChannel = guild.getTextChannelById(channelID);
			if (showcasesChannel == null) {
				LOGGER.log(Level.WARNING, "No Showcases Channel found in guild: {0} [{1}]!",
						new Object[] { guild.getName(), guild.getId() });
				return null;
			}
			return showcasesChannel;
		}

		public static long getShowcasesFromGuild(final Guild guild) {
			return CoreBotUtils.GUILDS.get(guild) == null ? 0L : CoreBotUtils.GUILDS.get(guild).showcasesID;
		}

		public static int getStageStar(final Guild guild, int stage) {
			final GuildInfo info = CoreBotUtils.GUILDS.get(guild) == null ? new GuildInfo(guild)
					: CoreBotUtils.GUILDS.get(guild);

			int count;
			if (stage >= info.stages.length) {
				stage = info.stages.length - 1;
			} else if (stage < 0) {
				stage = 0;
			}
			count = info.stages[stage];

			CoreBotUtils.GUILDS.put(guild, info);
			CoreBotUtils.writeGuildInfo();
			return count;
		}

		public static TextChannel getStarboardChannel(final Guild guild) {
			var channelID = 0L;
			try {
				channelID = getStarboardFromGuild(guild);
			} catch (final ConfigException.Missing e) {
				LOGGER.log(Level.INFO, "No Starboard Channel found in config for guild: {0} [{1}]!",
						new Object[] { guild.getName(), guild.getId() });
			}

			if (channelID <= 0) {
				final var channel = guild.getTextChannelsByName("starboard", true).get(0);
				if (channel == null) {
					LOGGER.log(Level.WARNING, "No Starboard Channel found in guild: {0} [{1}]!",
							new Object[] { guild.getName(), guild.getId() });
					return null;
				}
				channelID = channel.getIdLong();
			}

			setStarboardChannel(guild, channelID);

			final var starboardChannel = guild.getTextChannelById(channelID);
			if (starboardChannel == null) {
				LOGGER.log(Level.WARNING, "No Starboard Channel found in guild: {0} [{1}]!",
						new Object[] { guild.getName(), guild.getId() });
				return null;
			}
			return starboardChannel;
		}

		public static long getStarboardFromGuild(final Guild guild) {
			return CoreBotUtils.GUILDS.get(guild) == null ? 0L : CoreBotUtils.GUILDS.get(guild).starboardID;
		}

		public static boolean includesBotStar(final Guild guild) {
			return CoreBotUtils.GUILDS.get(guild) != null && CoreBotUtils.GUILDS.get(guild).includeBotStar;
		}

		public static boolean isStarboardEnabled(final Guild guild) {
			return CoreBotUtils.GUILDS.get(guild) != null && CoreBotUtils.GUILDS.get(guild).enableStarboard;
		}

		public static void setIncludeBotStar(final Guild guild, final boolean include) {
			final GuildInfo info = CoreBotUtils.GUILDS.get(guild) == null ? new GuildInfo(guild)
					: CoreBotUtils.GUILDS.get(guild);
			info.includeBotStar = include;
			CoreBotUtils.GUILDS.put(guild, info);
			CoreBotUtils.writeGuildInfo();
		}

		public static void setMinimumStars(final Guild guild, int minimum) {
			final GuildInfo info = CoreBotUtils.GUILDS.get(guild) == null ? new GuildInfo(guild)
					: CoreBotUtils.GUILDS.get(guild);
			if (minimum <= 0) {
				minimum = 5;
			}
			info.minimumStars = minimum;
			CoreBotUtils.GUILDS.put(guild, info);
			CoreBotUtils.writeGuildInfo();
		}

		public static void setShowcasesChannel(final Guild guild, final long channelID) {
			final GuildInfo info = CoreBotUtils.GUILDS.get(guild) == null ? new GuildInfo(guild)
					: CoreBotUtils.GUILDS.get(guild);
			info.showcasesID = channelID;
			CoreBotUtils.GUILDS.put(guild, info);
			CoreBotUtils.writeGuildInfo();
		}

		public static void setStarboardChannel(final Guild guild, final long channelID) {
			final GuildInfo info = CoreBotUtils.GUILDS.get(guild) == null ? new GuildInfo(guild)
					: CoreBotUtils.GUILDS.get(guild);
			info.starboardID = channelID;
			CoreBotUtils.GUILDS.put(guild, info);
			CoreBotUtils.writeGuildInfo();
		}

		public static void setStarboardEnabled(final Guild guild, final boolean enabled) {
			final GuildInfo info = CoreBotUtils.GUILDS.get(guild) == null ? new GuildInfo(guild)
					: CoreBotUtils.GUILDS.get(guild);
			info.enableStarboard = enabled;
			CoreBotUtils.GUILDS.put(guild, info);
			CoreBotUtils.writeGuildInfo();
		}

		private StarboardUtils() {
			throw new UnsupportedOperationException("Cannot construct a Utility Class");
		}
	}

	public static final class WarnUtils {

		public static void clearWarns(final Guild guild, final Member clearer, final Member toClear) {
			createUserWarns(guild, toClear);
			CoreBotUtils.GUILDS.get(guild).userWarnMap.clear();

			final var loggingChannel = getModLogChannel(guild);
			if (loggingChannel != null) {
				final var embed = new EmbedBuilder().setColor(Color.BLUE)
						.setTitle("Warns cleared for: " + toClear.getEffectiveName())
						.setDescription("**Cleared By**: " + clearer.getAsMention()).setTimestamp(Instant.now());
				loggingChannel.sendMessageEmbeds(embed.build()).queue();
			}
		}

		public static UserWarns createUserWarns(final Guild guild, final long userID) {
			GuildInfo info = CoreBotUtils.GUILDS.get(guild);
			if (info == null) {
				info = new GuildInfo(guild);
				CoreBotUtils.GUILDS.put(guild, info);
			}
			info.userWarnMap.put(userID, new UserWarns(userID, new HashMap<>()));
			CoreBotUtils.writeGuildInfo();
			return info.userWarnMap.get(userID);
		}

		public static UserWarns createUserWarns(final Guild guild, final Member member) {
			GuildInfo info = CoreBotUtils.GUILDS.get(guild);
			if (info == null) {
				info = new GuildInfo(guild);
				CoreBotUtils.GUILDS.put(guild, info);
			}
			info.userWarnMap.put(member.getIdLong(), new UserWarns(member));
			CoreBotUtils.writeGuildInfo();
			final var userWarns = info.userWarnMap.get(member.getIdLong());
			if (userWarns.member == null) {
				userWarns.member = member;
			}
			return userWarns;
		}

		public static UserWarns createUserWarns(final Guild guild, final User user) {
			GuildInfo info = CoreBotUtils.GUILDS.get(guild);
			if (info == null) {
				info = new GuildInfo(guild);
				CoreBotUtils.GUILDS.put(guild, info);
			}
			info.userWarnMap.put(user.getIdLong(), new UserWarns(user));
			CoreBotUtils.writeGuildInfo();
			return info.userWarnMap.get(user.getIdLong());
		}

		public static UserWarns getUserWarns(final Guild guild, final long userID) {
			final GuildInfo info = CoreBotUtils.GUILDS.get(guild);
			if (info == null)
				return createUserWarns(guild, userID);
			return info.userWarnMap.get(userID) == null ? createUserWarns(guild, userID) : info.userWarnMap.get(userID);
		}

		public static UserWarns getUserWarns(final Guild guild, final Member member) {
			final GuildInfo info = CoreBotUtils.GUILDS.get(guild);
			if (info == null)
				return createUserWarns(guild, member);
			final UserWarns userWarns = info.userWarnMap.get(member.getIdLong()) == null ? createUserWarns(guild, member)
					: info.userWarnMap.get(member.getIdLong());
			if (userWarns.member == null) {
				userWarns.member = member;
			}
			return userWarns;
		}

		public static UserWarns getUserWarns(final Guild guild, final User user) {
			final GuildInfo info = CoreBotUtils.GUILDS.get(guild);
			if (info == null)
				return createUserWarns(guild, user);
			return info.userWarnMap.get(user.getIdLong()) == null ? createUserWarns(guild, user)
					: info.userWarnMap.get(user.getIdLong());
		}

		public static boolean removeWarnByUUID(final Guild guild, final Member remover, final String strUUID) {
			final var uuid = UUID.fromString(strUUID);
			if (uuid == null)
				return false;

			final AtomicReference<UserWarns> atomicUserWarns = new AtomicReference<>();
			CoreBotUtils.GUILDS.get(guild).userWarnMap.forEach((user, warns) -> {
				if (atomicUserWarns.get() == null && warns.warns.containsKey(uuid)) {
					atomicUserWarns.set(warns);
				}
			});

			if (atomicUserWarns.get() != null)
				return atomicUserWarns.get().removeWarn(guild, remover, strUUID);

			return atomicUserWarns.get() != null;
		}

		private WarnUtils() {
			throw new UnsupportedOperationException("Cannot construct a Utility Class");
		}
	}

	private static Role mutedRole;

	private static Role createMutedRole(final Guild guild) {
		guild.createRole().setName("Muted").setColor(Color.DARK_GRAY).setMentionable(false)
				.setPermissions(Permission.EMPTY_PERMISSIONS).setHoisted(true)
				.queue(r -> guild.getTextChannels().forEach(channel -> channel.createPermissionOverride(r)
						.deny(Permission.MESSAGE_WRITE).queue(role -> mutedRole = role.getRole())));
		final var role = mutedRole;
		mutedRole = null;
		return role;
	}

	public static Color generateRandomColor() {
		final var red = Constants.RANDOM.nextFloat();
		final var green = Constants.RANDOM.nextFloat();
		final var blue = Constants.RANDOM.nextFloat();
		return new Color(red, green, blue);
	}

	public static Color generateRandomPastelColor() {
		final var rand = Constants.RANDOM;
		final var hue = rand.nextFloat();
		// Saturation between 0.1 and 0.3
		final var saturation = (rand.nextInt(2000) + 1000) / 10000f;
		final var luminance = 0.9f;
		return Color.getHSBColor(hue, saturation, luminance);
	}

	public static Role getAdvModderRole(final Guild guild) {
		var roleID = 0L;
		try {
			roleID = getAdvModderRoleFromGuild(guild);
		} catch (final ConfigException.Missing e) {
			LOGGER.log(Level.INFO, "No Advanced Modder Role found in config for guild: {0} [{1}]!",
					new Object[] { guild.getName(), guild.getId() });
		}

		if (roleID <= 0) {
			final var modderRole = guild.getRolesByName("advanced modder", true).get(0);
			if (modderRole == null) {
				LOGGER.log(Level.WARNING, "No Advanced Modder Role found in guild: {0} [{1}]!",
						new Object[] { guild.getName(), guild.getId() });
				return null;
			}
			roleID = modderRole.getIdLong();
		}

		setAdvModderRoleForGuild(guild, roleID);

		final var modderRole = guild.getRoleById(roleID);
		if (modderRole == null) {
			LOGGER.log(Level.WARNING, "No Advanced Modder Role found in guild: {0} [{1}]!",
					new Object[] { guild.getName(), guild.getId() });
			return null;
		}
		return modderRole;
	}

	public static long getAdvModderRoleFromGuild(final Guild guild) {
		return CoreBotUtils.GUILDS.get(guild).advModderRoleID;
	}

	public static int getBanThreshold(final Guild guild) {
		return CoreBotUtils.GUILDS.get(guild) == null ? 5 : CoreBotUtils.GUILDS.get(guild).banThreshold;
	}

	public static String getBotToken() {
		try {
			return getImportantConfig().getString("BotToken");
		} catch (final ConfigException.Missing e) {
			LOGGER.log(Level.WARNING, "No Bot Token found in config!");
			throw new IllegalArgumentException(e);
		}
	}

	public static String getGithubToken() {
		try {
			return getImportantConfig().getString("GithubToken");
		} catch (final ConfigException.Missing e) {
			LOGGER.log(Level.WARNING, "No GitHub Token found in config!");
			throw new IllegalArgumentException(e);
		}
	}

	public static Config getImportantConfig() {
		return Constants.CONFIG.getConfig("important");
	}

	public static int getKickThreshold(final Guild guild) {
		return CoreBotUtils.GUILDS.get(guild) == null ? 3 : CoreBotUtils.GUILDS.get(guild).kickThreshold;
	}

	public static Role getModeratorRole(final Guild guild) {
		var roleID = 0L;
		try {
			roleID = getModRoleFromGuild(guild);
		} catch (final ConfigException.Missing e) {
			LOGGER.log(Level.INFO, "No Moderation Role found in config for guild: {0} [{1}]!",
					new Object[] { guild.getName(), guild.getId() });
		}

		if (roleID <= 0) {
			final var modRole = guild.getRolesByName("moderator", true).get(0);
			if (modRole == null) {
				LOGGER.log(Level.WARNING, "No Moderation Role found in guild: {0} [{1}]!",
						new Object[] { guild.getName(), guild.getId() });
				return null;
			}
			roleID = modRole.getIdLong();
		}

		setModRoleForGuild(guild, roleID);

		final var moderatorRole = guild.getRoleById(roleID);
		if (moderatorRole == null) {
			LOGGER.log(Level.WARNING, "No Moderation Role found in guild: {0} [{1}]!",
					new Object[] { guild.getName(), guild.getId() });
		}
		return moderatorRole;
	}

	public static TextChannel getModLogChannel(final Guild guild) {
		var channelID = 0L;
		try {
			channelID = getModLogFromGuild(guild);
		} catch (final ConfigException.Missing e) {
			LOGGER.log(Level.INFO, "No Moderation Log Channel found in config for guild: {0} [{1}]!",
					new Object[] { guild.getName(), guild.getId() });
		}

		if (channelID <= 0) {
			final var channel = guild.getTextChannelsByName("mod-log", true).get(0);
			if (channel == null) {
				LOGGER.log(Level.WARNING, "No Moderation Log Channel found in guild: {0} [{1}]!",
						new Object[] { guild.getName(), guild.getId() });
				return null;
			}
			channelID = channel.getIdLong();
		}

		setModLogForGuild(guild, channelID);

		final var modLogChannel = guild.getTextChannelById(channelID);
		if (modLogChannel == null) {
			LOGGER.log(Level.WARNING, "No Moderation Log Channel found in guild: {0} [{1}]!",
					new Object[] { guild.getName(), guild.getId() });
			return null;
		}
		return modLogChannel;
	}

	public static long getModLogFromGuild(final Guild guild) {
		return CoreBotUtils.GUILDS.get(guild).modLogID;
	}

	public static long getModRoleFromGuild(final Guild guild) {
		return CoreBotUtils.GUILDS.get(guild).modRoleID;
	}

	public static String getMongoConnection() {
		try {
			return getImportantConfig().getString("MongoConnection");
		} catch (final ConfigException.Missing e) {
			LOGGER.log(Level.WARNING, "No Mongo Connection found in config!");
			return "";
		}
	}

	public static Role getMutedRole(final Guild guild) {
		var roleID = 0L;
		try {
			roleID = getMutedRoleFromGuild(guild);
		} catch (final ConfigException.Missing e) {
			LOGGER.log(Level.INFO, "No Muted Role found in config for guild: {0} [{1}]!",
					new Object[] { guild.getName(), guild.getId() });
		}

		if (roleID <= 0L) {
			Role mutedRole = null;
			try {
				mutedRole = guild.getRolesByName("Muted", true).get(0);
			} catch (final IndexOutOfBoundsException ex) {
				LOGGER.log(Level.WARNING, "No Muted Role found in guild: {0} [{1}]!",
						new Object[] { guild.getName(), guild.getId() });
				return createMutedRole(guild);
			}
			roleID = mutedRole.getIdLong();
		}

		setMutedRoleForGuild(guild, roleID);

		final var mutedRole = guild.getRoleById(roleID);
		if (mutedRole == null) {
			LOGGER.log(Level.WARNING, "No Muted Role found in guild: {0} [{1}]!",
					new Object[] { guild.getName(), guild.getId() });
			return createMutedRole(guild);
		}
		return mutedRole;
	}

	public static long getMutedRoleFromGuild(final Guild guild) {
		return CoreBotUtils.GUILDS.get(guild).mutedRoleID;
	}

	public static int getMuteThreshold(final Guild guild) {
		return CoreBotUtils.GUILDS.get(guild) == null ? 2 : CoreBotUtils.GUILDS.get(guild).muteThreshold;
	}

	public static long getOwnerID() {
		try {
			return getImportantConfig().getLong("OwnerID");
		} catch (final ConfigException.Missing e) {
			LOGGER.log(Level.WARNING, "No Bot Owner found in config!");
			return Constants.DEFAULT_OWNER_ID;
		}
	}

	public static String getPrefixFromGuild(final Guild guild) {
		return CoreBotUtils.GUILDS.get(guild) == null ? "!" : CoreBotUtils.GUILDS.get(guild).prefix;
	}

	public static String getRedditID() {
		try {
			return getImportantConfig().getString("RedditID");
		} catch (final ConfigException.Missing e) {
			throw new NullPointerException("No Reddit ID found in config!");
		}
	}

	public static String getRedditSecret() {
		try {
			return getImportantConfig().getString("RedditSecret");
		} catch (final ConfigException.Missing e) {
			throw new NullPointerException("No Reddit Secret found in config!");
		}
	}

	public static boolean isBotOwner(final User user) {
		return getOwnerID() == user.getIdLong();
	}

	public static boolean isModerator(final Guild guild, final Member member) {
		if (member == null)
			return false;

		if (!member.isOwner()) {
			final var modRole = getModeratorRole(guild);
			return modRole != null && member.getRoles().contains(modRole);
		}
		return true;
	}

	public static boolean isModerator(final Guild guild, final User user) {
		final var member = guild.getMember(user);
		if (member == null) {
			LOGGER.log(Level.WARNING, "Cannot find user: {0} [{1}] in guild: {2} [{3}]!", new Object[] {
					user.getName() + "#" + user.getDiscriminator(), user.getId(), guild.getName(), guild.getId() });
			return false;
		}

		if (!member.isOwner()) {
			final var modRole = getModeratorRole(guild);
			return modRole != null && member.getRoles().contains(modRole);
		}
		return true;
	}

	private static void resetThresholds(final Guild guild) {
		final GuildInfo info = CoreBotUtils.GUILDS.get(guild) == null ? new GuildInfo(guild)
				: CoreBotUtils.GUILDS.get(guild);
		info.muteThreshold = 2;
		info.kickThreshold = 3;
		info.banThreshold = 5;
		CoreBotUtils.GUILDS.put(guild, info);
		CoreBotUtils.writeGuildInfo();
	}

	public static void restartApplication(final JDA jda) throws URISyntaxException, IOException {
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

	public static void setAdvModderRoleForGuild(final Guild guild, final long roleID) {
		final GuildInfo info = CoreBotUtils.GUILDS.get(guild) == null ? new GuildInfo(guild)
				: CoreBotUtils.GUILDS.get(guild);
		info.advModderRoleID = roleID;
		CoreBotUtils.GUILDS.put(guild, info);
		CoreBotUtils.writeGuildInfo();
	}

	public static void setBanThreshold(final Guild guild, int maxWarns) {
		final int kickThreshold = getKickThreshold(guild);
		final GuildInfo info = CoreBotUtils.GUILDS.get(guild) == null ? new GuildInfo(guild)
				: CoreBotUtils.GUILDS.get(guild);
		if (maxWarns <= 0) {
			maxWarns = 5;
		}
		if (maxWarns <= kickThreshold) {
			maxWarns = kickThreshold + 1;
		}
		info.banThreshold = maxWarns;
		CoreBotUtils.GUILDS.put(guild, info);
		CoreBotUtils.writeGuildInfo();
	}

	public static void setKickThreshold(final Guild guild, int maxWarns) {
		final int banThreshold = getBanThreshold(guild);
		final GuildInfo info = CoreBotUtils.GUILDS.get(guild) == null ? new GuildInfo(guild)
				: CoreBotUtils.GUILDS.get(guild);
		if (maxWarns <= 0) {
			maxWarns = 3;
		}
		if (maxWarns >= banThreshold && banThreshold > 1) {
			maxWarns = banThreshold - 1;
		} else if (banThreshold <= 1) {
			resetThresholds(guild);
			return;
		}
		info.kickThreshold = maxWarns;
		CoreBotUtils.GUILDS.put(guild, info);
		CoreBotUtils.writeGuildInfo();
	}

	public static void setModLogForGuild(final Guild guild, final long channelID) {
		final GuildInfo info = CoreBotUtils.GUILDS.get(guild) == null ? new GuildInfo(guild)
				: CoreBotUtils.GUILDS.get(guild);
		info.modLogID = channelID;
		CoreBotUtils.GUILDS.put(guild, info);
		CoreBotUtils.writeGuildInfo();
	}

	public static void setModRoleForGuild(final Guild guild, final long roleID) {
		final GuildInfo info = CoreBotUtils.GUILDS.get(guild) == null ? new GuildInfo(guild)
				: CoreBotUtils.GUILDS.get(guild);
		info.modRoleID = roleID;
		CoreBotUtils.GUILDS.put(guild, info);
		CoreBotUtils.writeGuildInfo();
	}

	public static void setMutedRoleForGuild(final Guild guild, final long roleID) {
		final GuildInfo info = CoreBotUtils.GUILDS.get(guild) == null ? new GuildInfo(guild)
				: CoreBotUtils.GUILDS.get(guild);
		info.mutedRoleID = roleID;
		CoreBotUtils.GUILDS.put(guild, info);
		CoreBotUtils.writeGuildInfo();
	}

	public static void setMuteThreshold(final Guild guild, int maxWarns) {
		final int kickThreshold = getKickThreshold(guild);
		final GuildInfo info = CoreBotUtils.GUILDS.get(guild) == null ? new GuildInfo(guild)
				: CoreBotUtils.GUILDS.get(guild);
		if (maxWarns <= 0) {
			maxWarns = 2;
		}
		if (maxWarns >= kickThreshold && kickThreshold > 1) {
			maxWarns = kickThreshold - 1;
		} else if (kickThreshold <= 1) {
			resetThresholds(guild);
			return;
		}
		info.muteThreshold = maxWarns;
		CoreBotUtils.GUILDS.put(guild, info);
		CoreBotUtils.writeGuildInfo();
	}

	public static void setPrefixForGuild(final Guild guild, final String prefix) {
		final GuildInfo info = CoreBotUtils.GUILDS.get(guild) == null ? new GuildInfo(guild)
				: CoreBotUtils.GUILDS.get(guild);
		info.prefix = prefix;
		CoreBotUtils.GUILDS.put(guild, info);
		CoreBotUtils.writeGuildInfo();
	}

	public static void shutdownApplication(final JDA jda) {
		CoreBotUtils.writeGuildInfo();
		jda.shutdown();
		jda.getHttpClient().connectionPool().evictAll();
		jda.getHttpClient().dispatcher().executorService().shutdown();
		System.exit(0);
	}

	public static String trueFalseToYesNo(final boolean value) {
		return String.valueOf(value).replace("true", "Yes").replace("false", "No");
	}

	private BotUtils() {
		throw new UnsupportedOperationException("Cannot construct a Utility Class");
	}
}
