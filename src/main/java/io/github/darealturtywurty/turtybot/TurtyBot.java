package io.github.darealturtywurty.turtybot;

import static io.github.darealturtywurty.turtybot.util.Constants.GSON;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.github.darealturtywurty.turtybot.commands.bot.CommandHook;
import io.github.darealturtywurty.turtybot.data.GuildInfo;
import io.github.darealturtywurty.turtybot.data.UserWarns;
import io.github.darealturtywurty.turtybot.help_system.HelpManager;
import io.github.darealturtywurty.turtybot.util.BotUtils;
import io.github.darealturtywurty.turtybot.util.Constants;
import io.github.darealturtywurty.turtybot.util.Triple;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

public class TurtyBot extends ListenerAdapter {
	private static TurtyBot instance;
	private final Path guildInfo = Path.of("guildInfo.json");
	public final Map<Guild, GuildInfo> guilds = new HashMap<>();
	private final JDA bot;
	private Date readTime;

	public static void main(String[] args) {
		TurtyBot.getOrCreateInstance();
	}

	public static TurtyBot getOrCreateInstance() {
		String botToken = BotUtils.getBotToken();
		return instance == null ? TurtyBot.from(botToken) : instance;
	}

	static TurtyBot from(String token) {
		try {
			return new TurtyBot(JDABuilder.createDefault(token).enableIntents(GatewayIntent.GUILD_MEMBERS)
					.setMemberCachePolicy(MemberCachePolicy.ALL).build());
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	private TurtyBot(JDA bot) {
		instance = this;
		bot.addEventListener(this);
		bot.addEventListener(new CommandHook());
		bot.addEventListener(new HelpManager.HelpEventListener());
		this.bot = bot;
	}

	@Override
	public void onReady(ReadyEvent event) {
		super.onReady(event);
		this.readGuildInfo();
		this.writeGuildInfo();
	}

	private void readGuildInfo() {
		try {
			if (!Files.exists(this.guildInfo))
				return;
			JsonArray json = GSON.fromJson(Files.readString(this.guildInfo), JsonArray.class);
			for (var guildIndex = 0; guildIndex < json.size(); guildIndex++) {
				var guildObject = json.get(guildIndex).getAsJsonObject();
				var guild = this.bot.getGuildById(guildObject.get("GuildID").getAsLong());
				var info = new GuildInfo(guild);
				info.prefix = guildObject.get("Prefix").getAsString();
				info.modRoleID = guildObject.get("ModeratorRoleID").getAsLong();
				info.modLogID = guildObject.get("ModLogChannelID").getAsLong();
				info.advModderRoleID = guildObject.get("AdvancedModderRoleID").getAsLong();
				info.mutedRoleID = guildObject.get("MutedRoleID").getAsLong();
				info.muteThreshold = guildObject.get("MuteThreshold").getAsInt();
				info.kickThreshold = guildObject.get("KickThreshold").getAsInt();
				info.banThreshold = guildObject.get("BanThreshold").getAsInt();

				var usersWarnsArray = guildObject.get("UserWarns").getAsJsonArray();
				usersWarnsArray.forEach(object -> {
					var user = object.getAsJsonObject();
					var userID = user.get("ID").getAsLong();
					var userWarns = user.get("Warns").getAsJsonArray();
					Map<UUID, Triple<Long, Date, String>> warnMap = new HashMap<>();
					userWarns.forEach(warning -> {
						var warningObj = warning.getAsJsonObject();
						var uuid = UUID.fromString(warningObj.get("UUID").getAsString());
						var warner = warningObj.get("Warner").getAsLong();
						Date date = null;
						try {
							date = Constants.DATE_FORMAT.parse(warningObj.get("Date").getAsString());
						} catch (ParseException e) {
							readTime = Date.from(Instant.now());
							date = readTime;
						}
						var reason = warningObj.get("Reason").getAsString();
						warnMap.put(uuid, new Triple<>(warner, date, reason));
					});
					var userWarnsObj = new UserWarns(userID, warnMap);
					info.userWarnMap.put(userID, userWarnsObj);
				});

				this.guilds.put(guild, info);
			}
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public void writeGuildInfo() {
		try {
			if (!Files.exists(this.guildInfo))
				Files.createFile(this.guildInfo);
			var json = new JsonArray();
			this.guilds.forEach((guild, info) -> {
				var newGuildInfo = guilds.get(guild);
				var guildObject = new JsonObject();
				guildObject.addProperty("GuildID", guild.getIdLong());
				guildObject.addProperty("Prefix", newGuildInfo.prefix);
				guildObject.addProperty("ModeratorRoleID", newGuildInfo.modRoleID);
				guildObject.addProperty("ModLogChannelID", newGuildInfo.modLogID);
				guildObject.addProperty("AdvancedModderRoleID", newGuildInfo.advModderRoleID);
				guildObject.addProperty("MutedRoleID", newGuildInfo.mutedRoleID);
				guildObject.addProperty("MuteThreshold", newGuildInfo.muteThreshold);
				guildObject.addProperty("KickThreshold", newGuildInfo.kickThreshold);
				guildObject.addProperty("BanThreshold", newGuildInfo.banThreshold);

				var userWarnsArray = new JsonArray();
				newGuildInfo.userWarnMap.forEach((userID, userWarns) -> {
					var user = new JsonObject();
					user.addProperty("ID", userWarns.userID);

					var userWarnings = new JsonArray();
					userWarns.warns.forEach((uuid, warnInfo) -> {
						var warn = new JsonObject();
						warn.addProperty("UUID", uuid.toString());
						warn.addProperty("Warner", warnInfo.left);
						warn.addProperty("Date", Constants.DATE_FORMAT.format(warnInfo.middle));
						warn.addProperty("Reason", warnInfo.right);
						userWarnings.add(warn);

						if (warnInfo.middle.equals(this.readTime)) {
							Constants.LOGGER.severe("Experienced an error with the warning date-time: " + uuid.toString()
									+ " | " + Constants.DATE_FORMAT.format(warnInfo.middle));
						}
					});
					user.add("Warns", userWarnings);
					userWarnsArray.add(user);
				});
				guildObject.add("UserWarns", userWarnsArray);

				json.add(guildObject);
			});
			Files.writeString(this.guildInfo, GSON.toJson(json));
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}
}
