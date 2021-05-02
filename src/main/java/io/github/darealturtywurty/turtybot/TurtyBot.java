package io.github.darealturtywurty.turtybot;

import static io.github.darealturtywurty.turtybot.util.Constants.GSON;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.github.darealturtywurty.turtybot.commands.bot.CommandHook;
import io.github.darealturtywurty.turtybot.data.GuildInfo;
import io.github.darealturtywurty.turtybot.help_system.HelpManager;
import io.github.darealturtywurty.turtybot.util.BotUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class TurtyBot extends ListenerAdapter {
	private static TurtyBot INSTANCE;
	private final Path guildInfo = Path.of("guildInfo.json");
	public final Map<Guild, GuildInfo> guilds = new HashMap<>();
	private final JDA bot;

	public static void main(String[] args) {
		TurtyBot.getOrCreateInstance();
	}

	public static TurtyBot getOrCreateInstance() {
		String botToken = BotUtils.getBotToken();
		return INSTANCE == null ? TurtyBot.from(botToken) : INSTANCE;
	}

	static TurtyBot from(String token) {
		try {
			return new TurtyBot(JDABuilder.createDefault(token).build());
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	private TurtyBot(JDA bot) {
		INSTANCE = this;
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
			for (int guildIndex = 0; guildIndex < json.size(); guildIndex++) {
				JsonObject guildObject = json.get(guildIndex).getAsJsonObject();
				Guild guild = this.bot.getGuildById(guildObject.get("GuildID").getAsLong());
				GuildInfo info = new GuildInfo(guild);
				info.prefix = guildObject.get("Prefix").getAsString();
				info.modRoleID = guildObject.get("ModeratorRoleID").getAsLong();
				info.modLogID = guildObject.get("ModLogChannelID").getAsLong();
				info.advModderRoleID = guildObject.get("AdvancedModderRoleID").getAsLong();
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
			JsonArray json = new JsonArray();
			this.guilds.forEach((guild, info) -> {
				GuildInfo newGuildInfo = guilds.get(guild);
				JsonObject guildObject = new JsonObject();
				guildObject.addProperty("GuildID", guild.getIdLong());
				guildObject.addProperty("Prefix", newGuildInfo.prefix);
				guildObject.addProperty("ModeratorRoleID", newGuildInfo.modRoleID);
				guildObject.addProperty("ModLogChannelID", newGuildInfo.modLogID);
				guildObject.addProperty("AdvancedModderRoleID", newGuildInfo.advModderRoleID);
				json.add(guildObject);
			});
			Files.writeString(this.guildInfo, GSON.toJson(json));
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}
}
