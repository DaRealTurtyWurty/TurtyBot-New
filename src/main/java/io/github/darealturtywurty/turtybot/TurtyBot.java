package io.github.darealturtywurty.turtybot;

import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.github.darealturtywurty.turtybot.commands.core.CommandHook;
import io.github.darealturtywurty.turtybot.commands.nsfw.NSFWCommandListener;
import io.github.darealturtywurty.turtybot.managers.auto_mod.AutoModerator;
import io.github.darealturtywurty.turtybot.managers.music.core.MusicManager;
import io.github.darealturtywurty.turtybot.managers.music.core.VoiceChannelListener;
import io.github.darealturtywurty.turtybot.managers.polls.PollCommand;
import io.github.darealturtywurty.turtybot.managers.starboard.StarboardManager;
import io.github.darealturtywurty.turtybot.managers.suggestions.SuggestionManager;
import io.github.darealturtywurty.turtybot.util.Constants;
import io.github.darealturtywurty.turtybot.util.core.BotUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import okhttp3.OkHttpClient;

public class TurtyBot {

    private TurtyBot(final JDA bot) {
        bot.addEventListener(new StarboardManager(), Constants.LEVELLING_MANAGER, new VoiceChannelListener(),
                new AutoModerator(), new PollCommand(), new SuggestionManager(bot),
                new NSFWCommandListener());
        Logger.getLogger(OkHttpClient.class.getName()).setLevel(Level.FINE);
        MusicManager.register();
        Locale.setDefault(Locale.UK);
        Constants.LOGGER.info("I have finished loading everything!");
    }

    public static void main(final String[] args) {
        TurtyBot.create(BotUtils.getBotToken());
    }

    static TurtyBot create(final String token) {
        try {
            return new TurtyBot(JDABuilder.createDefault(token)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES)
                    .setMemberCachePolicy(MemberCachePolicy.ALL).enableCache(CacheFlag.ONLINE_STATUS)
                    .setChunkingFilter(ChunkingFilter.ALL).addEventListeners(new CommandHook()).build());
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
