package io.github.darealturtywurty.turtybot.managers.autoposter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.GuildChannel;

public abstract class Autoposter {
    public final long guildId, channelId;
    public final JDA jda;

    protected Autoposter(final GuildChannel channel) {
        this.guildId = channel.getGuild().getIdLong();
        this.channelId = channel.getIdLong();
        this.jda = channel.getJDA();
    }

    public abstract long getTimeout();

    public abstract void run();

    public static class Manager {
        public static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(5);
        private static final List<ScheduledFuture<?>> TASKS = new ArrayList<>();
        public static final Set<Autoposter> AUTOPOSTERS = new HashSet<>();

        public static void init() {
            TASKS.forEach(task -> {
                if (!task.isCancelled()) {
                    task.cancel(false);
                }
            });
            TASKS.clear();

            for (final Autoposter autoposter : AUTOPOSTERS) {
                TASKS.add(EXECUTOR.scheduleWithFixedDelay(autoposter::run, autoposter.getTimeout(),
                        autoposter.getTimeout(), TimeUnit.SECONDS));
            }
        }
    }
}
