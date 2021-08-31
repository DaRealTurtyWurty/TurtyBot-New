package io.github.darealturtywurty.turtybot.managers.help_system;

import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CloseButtonListener extends ListenerAdapter {

    @Override
    public void onButtonClick(final ButtonClickEvent event) {
        super.onButtonClick(event);
        if (event.getComponentId().startsWith("confirm._.")) {
            event.getJDA().addEventListener(new HelpReactionEventListener((TextChannel) event.getChannel(),
                    event.getMessageIdLong()));
        } else if (event.getComponentId().startsWith("cancel._.")) {
            event.getChannel().deleteMessageById(event.getMessageIdLong()).queue();
        }
    }
}
