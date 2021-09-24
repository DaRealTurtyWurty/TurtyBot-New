package io.github.darealturtywurty.turtybot.commands.core;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.sharding.ShardManager;

public interface CommandContext {

    /**
     * Returns the {@link net.dv8tion.jda.api.entities.User author} of the message
     * as user
     *
     * @return the {@link net.dv8tion.jda.api.entities.User author} of the message
     *         as user
     */
    default User getAuthor() {
        return getEvent().getUser();
    }

    /**
     * Returns the {@link net.dv8tion.jda.api.entities.TextChannel channel} that the
     * message for this event was send in
     *
     * @return the {@link net.dv8tion.jda.api.entities.TextChannel channel} that the
     *         message for this event was send in
     */
    default TextChannel getChannel() {
        return (TextChannel) getEvent().getChannel();
    }

    /**
     * Returns the {@link net.dv8tion.jda.api.events.interaction.SlashCommandEvent
     * message event} that was received for this instance
     *
     * @return the {@link net.dv8tion.jda.api.events.interaction.SlashCommandEvent
     *         message event} that was received for this instance
     */
    SlashCommandEvent getEvent();

    /**
     * Returns the {@link net.dv8tion.jda.api.entities.Guild} for the current
     * command/event
     *
     * @return the {@link net.dv8tion.jda.api.entities.Guild} for this command/event
     */
    default Guild getGuild() {
        return getEvent().getGuild();
    }

    /**
     * Returns the current {@link net.dv8tion.jda.api.JDA jda} instance
     *
     * @return the current {@link net.dv8tion.jda.api.JDA jda} instance
     */
    default JDA getJDA() {
        return getEvent().getJDA();
    }

    /**
     * Returns the {@link net.dv8tion.jda.api.entities.Member author} of the message
     * as member
     *
     * @return the {@link net.dv8tion.jda.api.entities.Member author} of the message
     *         as member
     */
    default Member getMember() {
        return getEvent().getMember();
    }

    /**
     * Returns the {@link net.dv8tion.jda.api.entities.Member member} in the guild
     * for the currently logged in account
     *
     * @return the {@link net.dv8tion.jda.api.entities.Member member} in the guild
     *         for the currently logged in account
     */
    default Member getSelfMember() {
        return getGuild().getSelfMember();
    }

    /**
     * Returns the {@link net.dv8tion.jda.api.entities.User user} for the currently
     * logged in account
     *
     * @return the {@link net.dv8tion.jda.api.entities.User user} for the currently
     *         logged in account
     */
    default User getSelfUser() {
        return getJDA().getSelfUser();
    }

    /**
     * Returns the current {@link net.dv8tion.jda.api.sharding.ShardManager}
     * instance
     *
     * @return the current {@link net.dv8tion.jda.api.sharding.ShardManager}
     *         instance
     */
    default ShardManager getShardManager() {
        return getJDA().getShardManager();
    }
}
