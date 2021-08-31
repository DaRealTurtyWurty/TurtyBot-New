package io.github.darealturtywurty.turtybot.commands.fun;

import java.util.List;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class UpsideDownTextCommand implements GuildCommand {

    private static final String NORMAL_CHARS = "abcdefghijklmnopqrstuvwxyz_,;.?!/\\'";
    private static final String UPSIDEDOWN_CHARS = "ɐqɔpǝɟbɥıظʞןɯuodbɹsʇnʌʍxʎz‾'؛˙¿¡/\\\\,";

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.FUN;
    }

    @Override
    public String getDescription() {
        return "Makes the input text upside-down.";
    }

    @Override
    public String getName() {
        return "upsidedowntext";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(new OptionData(OptionType.STRING, "text", "The text to make upside-down", true));
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        final String text = ctx.getEvent().getOption("text").getAsString();
        final var newText = new StringBuilder();
        for (int charIndex = 0; charIndex < text.length(); charIndex++) {
            final char letter = text.charAt(charIndex);
            final int normalIndex = NORMAL_CHARS.indexOf(letter);
            newText.append(normalIndex != -1 ? UPSIDEDOWN_CHARS.charAt(normalIndex) : letter);
        }
        ctx.getEvent().deferReply().setContent(newText.toString()).mentionRepliedUser(false).queue();
    }
}