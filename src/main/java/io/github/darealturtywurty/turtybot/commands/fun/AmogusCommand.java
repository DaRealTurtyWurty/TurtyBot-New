package io.github.darealturtywurty.turtybot.commands.fun;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;

public class AmogusCommand implements IGuildCommand {

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.FUN;
	}

	@Override
	public String getDescription() {
		return "Sus.";
	}

	@Override
	public String getName() {
		return "amogus";
	}

	@Override
	public void handle(final CommandContext ctx) {
		ctx.getChannel().sendMessage(
				"Red 🔴 📛 sus 💦 💦. Red 🔴 🔴 suuuus. I 👁👄 👁 said 🤠🗣 💬👱🏿💦 red 👹 🔴, sus 💦 💦, hahahahaha 🤣 🤣. "
						+ "Why 🤔 🤔 arent you 👉😯 👈 laughing 😂 😂? I 👁🍊 👥 just made 👑 👑 a reference 👀👄🙀 👀👄🙀 "
						+ "to the popular 👍😁😂 😂 video 📹 📹 game 🎮 🎮 \"Among 🇷🇴🎛 💰 Us 👨 👨\"! How can you 👈 👈 not laugh 😂 😂 at it? "
						+ "Emergeny meeting 💯 🤝! Guys 👦 👨, this here guy 👨 👱🏻👨🏻 doesnt laugh 🤣 ☑😂😅 at my funny 😃😂 🍺😛😃 "
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
}
