package io.github.darealturtywurty.turtybot.commands.fun;

import java.util.List;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class AmogusCommand implements GuildCommand {

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.FUN;
    }

    @Override
    public long getCooldownMillis() {
        return 600000;
    }

    @Override
    public String getDescription() {
        return "Sus.";
    }

    @Override
    public String getName() {
        return "amongus";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of();
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        ctx.getEvent().deferReply().setContent(
                "Red 🔴 📛 sus 💦 💦. Red 🔴 🔴 suuuus. I 👁👄 👁 said 🤠🗣 💬👱🏿💦 red 👹 🔴, sus 💦 💦, hahahahaha 🤣 🤣. "
                        + "Why 🤔 🤔 arent you 👉😯 👈 laughing 😂 😂? I 👁🍊 👥 just made 👑 👑 a reference 👀👄🙀 👀👄🙀 "
                        + "to the popular 👍😁😂 😂 video 📹 📹 game 🎮 🎮 \"Among 🇷🇴🎛 💰 Us 👨 👨\"! How can you 👈 👈 not laugh 😂 😂 at it? "
                        + "Emergency meeting 💯 🤝! Guys 👦 👨, this here guy 👨 👱🏻👨🏻 doesn't laugh 🤣 ☑😂😅 at my funny 😃😂 🍺😛😃 "
                        + "Among 💰 💰 Us 👨 👨 memes 🐸 😂! Lets 🙆 🙆 beat ✊👊🏻 😰👊 him 👴 👨 to death 💀💥❓ 💀! "
                        + "Dead 💀😂 ☠ body 💃 💃 reported ☎ 🧐! Skip 🐧 🏃🏼! Skip 🐧 🐧! Vote 🔝 🔝 blue 💙 💙! "
                        + "Blue 💙 💙 was not an impostor 😎 😠. Among 😂 🙆🏽🅰 us 👨 👨 in a nutshell 😠 😠 hahahaha 😂👌👋 😂. "
                        + "What?! Youre still 🤞🙌 🤞🙌 not laughing 😂 😂 your 👉 👉 ass 🍑 🅰 off 📴 📴☠? I 👁 👁 made 👑 👑 "
                        + "SEVERAL 💯 💯 funny 😀😂😛 😃❓ references 👀👄🙀 📖 to Among 💰 💑👨‍❤️‍👨👩‍❤️‍👩 Us 👨 🇺🇸 and YOU 👈🏼 😂👉🔥 "
                        + "STILL 🤞🙌 🙄 ARENT LAUGHING 😂 😂😎💦??!!! Bruh ⚠ 😳🤣😂. Ya 🙏🎼 🙀 hear 👂 👂 that? "
                        + "Wooooooosh 💦👽👾 💦👽👾. Whats 😦 😦 woooosh 🚁 🚁? Oh 🙀 🙀, nothing ❌ 🚫. Just the sound 👂 🔊 of a "
                        + "joke 😂 😂 flying ✈ ✈ over 😳🙊💦 🔁 your 👉 👉 head 💆 💆. Whats 😦 🤔 that? You 👈 👉 think 💭 💭 "
                        + "im 👌 💘 annoying 😠 😠? Kinda 🙅 🙅 sus 💦 💦, bro 👆 🌈☺👬. Hahahaha 😂 😂! Anyway 🔛 🔛, yea 😀 💯, "
                        + "gotta 👉 👉 go 🏃 🏃 do tasks ✔ 📋. Hahahaha 😂 😂!")
                .queue();
    }

    @Override
    public boolean productionReady() {
        return true;
    }
}
