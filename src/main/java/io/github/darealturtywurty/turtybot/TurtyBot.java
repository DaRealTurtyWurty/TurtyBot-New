package io.github.darealturtywurty.turtybot;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.github.darealturtywurty.turtybot.commands.core.CommandHook;
import io.github.darealturtywurty.turtybot.managers.auto_mod.AutoModerator;
import io.github.darealturtywurty.turtybot.managers.help_system.CloseButtonListener;
import io.github.darealturtywurty.turtybot.managers.help_system.HelpManager.HelpEventListener;
import io.github.darealturtywurty.turtybot.managers.music.core.MusicManager;
import io.github.darealturtywurty.turtybot.managers.music.core.VoiceChannelListener;
import io.github.darealturtywurty.turtybot.managers.polls.PollCommand;
import io.github.darealturtywurty.turtybot.managers.starboard.StarboardManager;
import io.github.darealturtywurty.turtybot.util.BotUtils;
import io.github.darealturtywurty.turtybot.util.Constants;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import okhttp3.OkHttpClient;

public class TurtyBot {

    private TurtyBot(final JDA bot) {
        bot.addEventListener(new HelpEventListener(), new StarboardManager(), Constants.LEVELLING_MANAGER,
                new VoiceChannelListener(), new AutoModerator(), new PollCommand(),
                new CloseButtonListener());
        Logger.getLogger(OkHttpClient.class.getName()).setLevel(Level.FINE);
        MusicManager.register();
        Constants.LOGGER.info("I have finished loading everything!");
    }

    public static void main(final String[] args) {
        TurtyBot.create(BotUtils.getBotToken());
    }

    static TurtyBot create(final String token) {
        try {
            return new TurtyBot(JDABuilder.createDefault(token).enableIntents(GatewayIntent.GUILD_MEMBERS)
                    .setMemberCachePolicy(MemberCachePolicy.ALL).addEventListeners(new CommandHook())
                    .build());
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
