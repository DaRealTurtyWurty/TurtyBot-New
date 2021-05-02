package io.github.darealturtywurty.turtybot.commands.utility;

import static java.lang.String.format;
import static java.lang.String.join;

import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.github.darealturtywurty.turtybot.commands.core.CommandContext;
import io.github.darealturtywurty.turtybot.commands.core.IGuildCommand;
import io.github.darealturtywurty.turtybot.util.BotUtils;
import io.vavr.control.Either;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.internal.utils.tuple.Pair;

public class SolutionsCommand implements IGuildCommand {

	private final Map<String, Solution> solutions = new HashMap<>();

	public SolutionsCommand() {
		addSolution(Solution.createSolution("formatting_codes", getFormattingCodes()).addAliases("formatting_code",
				"format_code", "format_codes"));
		addSolution(Solution.createSolution(".project", getMissingProject()).addAliases("eclipse_project", "project_missing",
				"missing_project"));
		addSolution(Solution.createSolution("datagen", getDatagen()).shouldDM());
		addSolution(Solution.createSolution("intellij_gradle", getIntellijGradle()));
		addSolution(Solution.createSolution("transparent_block", getTransparency()).addAliases("transparency"));
		addSolution(Solution.createSolution("tooltip", getTooltip()).addAliases("tooltips"));
	}

	private void addSolution(Solution solution) {
		solution.aliases.forEach(alias -> this.solutions.put(alias, solution));
	}

	@Override
	public void handle(CommandContext ctx) {
		if (ctx.getArgs().length >= 1) {
			String solutionStr = ctx.getArgs()[0];
			if (this.solutions.containsKey(solutionStr)) {
				Solution solution = this.solutions.get(solutionStr);
				if (solution.shouldDM) {
					ctx.getAuthor().openPrivateChannel().queue(channel -> {
						if (solution.message.isLeft())
							channel.sendMessage(format("Message sent by: %s", ctx.getAuthor().getAsMention())
									+ solution.message.getLeft()).queue();
						else
							channel.sendMessage(
									new EmbedBuilder(solution.message.get()).setAuthor(
											"Message sent by: " + ctx.getAuthor().getName() + "#"
													+ ctx.getAuthor().getDiscriminator(),
											null, ctx.getAuthor().getAvatarUrl()).build())
									.queue();
					});
					ctx.getMessage().delete().queue();
					return;
				}
				ctx.getMessage().reply(new EmbedBuilder(solution.message.get()).build()).mentionRepliedUser(false)
						.queue(reply -> {
							reply.delete().queueAfter(5, TimeUnit.MINUTES);
							ctx.getMessage().delete().queueAfter(5, TimeUnit.MINUTES);
						});
				return;
			} else {
				ctx.getChannel()
						.sendMessage(
								format("You must provide a valid solution! For the list of solutions, use `%ssolutions`.",
										BotUtils.getPrefixFromGuild(ctx.getGuild())))
						.queue();
			}
			return;
		}

		EmbedBuilder embed = new EmbedBuilder();
		embed.setTitle("List of available solutions:");
		embed.setDescription("`" + join("`, `", this.solutions.keySet()) + "`");
		embed.setColor(Color.CYAN);
		ctx.getMessage().reply(embed.build()).mentionRepliedUser(false).queue(reply -> {
			reply.delete().queueAfter(45, TimeUnit.SECONDS);
			ctx.getMessage().delete().queueAfter(45, TimeUnit.SECONDS);
		});
	}

	@Override
	public String getName() {
		return "solutions";
	}

	@Override
	public List<String> getAliases() {
		return Arrays.asList("solution");
	}

	@Override
	public String getDescription() {
		return "Gets the list of solutions, or if an argument is specified, "
				+ "gets a solution by the argument name (if it exists).";
	}

	public Either<String, MessageEmbed> getFormattingCodes() {
		return Either.right(new EmbedBuilder().setTitle("List of minecraft formatting codes!")
				.setImage("https://media.discordapp.net/attachments/645695673668337692/645723908737073152/unknown_1.png")
				.setColor(BotUtils.generateRandomColor()).build());
	}

	public Either<String, MessageEmbed> getMissingProject() {
		return Either.right(new EmbedBuilder().setTitle("(Eclipse) 'Open Project' has encountered a problem.")
				.setDescription(
						"If you recieve the following error, this usually means you are either missing the .project' file, or it is corrupted. To fix this you can use the `<bot_prefix>setup eclipse` command. This will give you the command that you need to run in Command Prompt at the root of your mod folder.")
				.setImage("https://i.postimg.cc/G2xgLsHX/124.jpg").setColor(BotUtils.generateRandomColor())
				.build());
	}

	public Either<String, MessageEmbed> getDatagen() {
		return Either.right(new EmbedBuilder()
				.setTitle("For Forge 1.13+, here is how you can automatically generate your jsons.")
				.setDescription(
						"This is for block states, item models, lang files, loot tables, recipes and tags. However, following the same technique, you can do it for other things too. For 1.16.2+ you can also use this for biomes, surface builders, chunk generators and more.")
				.addField(
						"The build.gradle - In your build.gradle you will find 'data' sections for each run, you need to first change that to the following.",
						"```gradle\ndata {\n            workingDirectory project.file('run')\n\n            "
								+ "// Recommended logging data for a userdev environment\n            "
								+ "property 'forge.logging.markers', 'SCAN'\n\n            "
								+ "// Recommended logging level for the console\n            "
								+ "property 'forge.logging.console.level', 'debug'\n\n            "
								+ "args '--mod', 'YOUR_MODID', '--all', '--output', file('src/generated/resources/'), "
								+ "'--existing', sourceSets.main.resources.srcDirs[0]\n            \n            mods {"
								+ "\n                YOUR_MODID{\n                    source sourceSets.main\n                "
								+ "}\n            }```\nYou will have to replace `YOUR_MODID` with your modid. "
								+ "Once you have done that, you will want to go ahead and run the "
								+ "`gradlew genEclipseRuns --refresh-dependencies` or `gradlew genIntellijRuns --refresh-dependencies`.",
						false)
				.addField("Then you will need the class that handles/registers the datagen. Here is an example:",
						"<https://pastebin.com/rru3Ccqf>", false)
				.addField("Then you will need a class for each generator. Here is an example for the language generation:",
						"```java\npublic class LanguagesDataGen extends LanguageProvider\n{\n    "
								+ "public LanguagesDataGen(DataGenerator gen, String locale)\n    {\n        "
								+ "super(gen, Reference.MOD_ID, locale);\n    }\n\n    @Override\n    "
								+ "protected void addTranslations()\n    {\n        "
								+ "add(\"itemGroup.items\", \"Airplanes Item\");\n        "
								+ "add(\"itemGroup.vehicles\", \"Airplanes Vehicles\");\n    }\n    \n    @Override\n    "
								+ "public String getName()\n    {\n        return \"Airplanes Mod Languages\";\n    }\n}```",
						false)
				.addField("Here is an example for Item Tags:",
						"```java\npublic class ItemTagsDataGen extends ItemTagsProvider\n{\n    "
								+ "public ItemTagsDataGen(DataGenerator generatorIn)\n    {\n        "
								+ "super(generatorIn);\n    }\n\n    @Override\n    protected void registerTags()\n    {    "
								+ "\n        //ingots\n        addForgeTag(\"ingots/aluminum\", ItemInit.ALUMINUM_INGOT.get());"
								+ "\n        addForgeTag(\"ingots/copper\", ItemInit.COPPER_INGOT.get());\n    }\n    \n    "
								+ "private void addForgeTag(String name, Item... items)\n    {\n        "
								+ "AirplanesMod.LOGGER.debug(\"Creating item tag for forge:\" + name);\n        "
								+ "ResourceLocation loc = new ResourceLocation(\"forge\", name);\n        "
								+ "getBuilder(new Tag<Item>(loc)).replace(false).add(items).build(loc);\n    }\n\n    "
								+ "@Override\n    public String getName()\n    {\n        return \"Item Tags\";\n    }\n}```",
						false)
				.addField(
						"Finally, you just need to enter the gradle tab in your IDE and run the `runData` task. "
								+ "Alternatively, you can just run `gradlew runData` in your terminal.",
						"The JSONs will then be generated in src/generated. "
								+ "Just make sure to refresh the folder if you are checking it in your IDE.",
						false)
				.setFooter("Credits to Affehund#9883",
						"https://cdn.discordapp.com/avatars/406870590922686464/58c3a548e0020f2df3fb3325c6de3d69.png?size=128")
				.setColor(0x22747).build());
	}

	public Either<String, MessageEmbed> getIntellijGradle() {
		return Either.right(new EmbedBuilder().setTitle("IntelliJ Gradle Settings").setDescription(
				"If you use IntelliJ, you might have to set the Gradle Settings from 'default' to 'IntelliJ IDEA' for the Gradle to work properly.")
				.setImage("https://i.postimg.cc/1tQ0NPNw/image.png").build());
	}

	public Either<String, MessageEmbed> getTransparency() {
		return Either.right(new EmbedBuilder().setTitle("Blocks with non-opaque pixels").setDescription(
				"In order to add a tooltip to your item or block item, you must override the `addInformation` method.")
				.addField("An example of a simple tooltip is as follows:",
						"```java\n@Override\npublic void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {"
								+ "\n    tooltip.add(new StringTextComponent(\"Hold \" + \"\u00A7e\" + \"Shift\" + "
								+ "\"\u00A77\" + \" for More Information\")); \n    //\"\u00A7e\" is a colour code\n    "
								+ "//\"\u00A77\" is a colour code\n}```",
						false)
				.addField("An example of a more advanced tooltip is as follows:",
						"```java\n@Override\npublic void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {"
								+ "\n    if(InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), "
								+ "GLFW.GLFW_KEY_LEFT_SHIFT)) {\n        "
								+ "tooltip.add(new StringTextComponent(\"This is a more advanced description\"));\n    } else {"
								+ "\n        tooltip.add(new StringTextComponent(\"Hold \" + \"\u00A7e\" + \"Shift\" + \"\u00A77\" + "
								+ "\" for More Information\"));\n        //\"\u00A7e\" is a color code\n        "
								+ "//\"\u00A77\" is a color code\n    }\n}```",
						false)
				.setColor(BotUtils.generateRandomColor()).build());
	}

	public Either<String, MessageEmbed> getTooltip() {
		return Either.right(new EmbedBuilder().setTitle("Adding tooltips to your items").setDescription(
				"If you use IntelliJ, you might have to set the Gradle Settings from 'default' to 'IntelliJ IDEA' for the Gradle to work properly.")
				.setImage("https://i.postimg.cc/1tQ0NPNw/image.png").build());
	}

	// place_block
	// block_place
	// ghost_block
	public Either<String, MessageEmbed> getBlockPlace() {
		return Either.right(new EmbedBuilder().setTitle("Ghost block when opening container with block in hand")
				.setDescription("**When opening your container with a block in your hand, you experience an issue where it "
						+ "places the block for a split second.**\n\n**How to fix:**\nYou can fix this issue by going into"
						+ " your `onBlockActivated` method, and making sure you return the same action result every time. "
						+ "You should always be returning `ActionResultType.SUCCESS` in this specific instance."
						+ "\n\n**Why does this happen?**\nThe reason this is happening is because of the server side "
						+ "check that you are doing to open the GUI. You are then retuning `SUCCESS` on the server "
						+ "and `FAIL` on the client. This means that the client will try to use the held item, since it "
						+ "is being told you were unable to complete the action. Whereas the server knows that you "
						+ "succeded. This means the client will render the block, but only for a split second since "
						+ "the server is denying the placement.")
				.build());
	}

	// long_transparency
	// extended_transparency
	// xray
	// transparency_long
	// transparency_extended
	// transparency_extension
	public Either<String, MessageEmbed> getTransparencyLong() {
		return Either.right(new EmbedBuilder().setTitle("A fully detailed explaination on transparency").setDescription(
				"Note: This is all made for mcp, not mojmap.")
				.setImage("https://i.postimg.cc/1tQ0NPNw/image.png").setColor(0xF2F2F2).build());
	}

	@Override
	public Pair<Boolean, List<String>> validChannels() {
		return Pair.of(true, Arrays.asList("bot-stuff"));
	}

	private static final class Solution {
		private boolean shouldDM = false;
		private Set<String> aliases = new HashSet<>();
		private Either<String, MessageEmbed> message;

		private Solution(String name, Either<String, MessageEmbed> message) {
			this.addAliases(name);
			this.message = message;
		}

		public static Solution createSolution(String name, Either<String, MessageEmbed> message) {
			return new Solution(name, message);
		}

		public Solution shouldDM() {
			this.shouldDM = true;
			return this;
		}

		public Solution addAliases(String... aliases) {
			this.aliases.addAll(Arrays.asList(aliases));
			return this;
		}
	}
}
