package io.github.darealturtywurty.turtybot.commands.utility;

import java.util.List;

import io.github.darealturtywurty.turtybot.commands.core.CommandCategory;
import io.github.darealturtywurty.turtybot.commands.core.CoreCommandContext;
import io.github.darealturtywurty.turtybot.commands.core.GuildCommand;
import io.github.darealturtywurty.turtybot.commands.core.RegisterBotCmd;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@RegisterBotCmd
public class RolesCommand implements GuildCommand {

    public static String padRight(final String s, final int n) {
        return String.format("%-" + n + "s", s);
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.UTILITY;
    }

    @Override
    public String getDescription() {
        return "Gets all the roles in the current guild";
    }

    @Override
    public String getName() {
        return "roles";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of();
    }

    @Override
    public void handle(final CoreCommandContext ctx) {
        ctx.getEvent().deferReply().setContent("```" + getRoles(ctx.getGuild()) + "```")
                .mentionRepliedUser(false).queue();
    }

    @Override
    public boolean productionReady() {
        return true;
    }

    private String getRoles(final Guild guild) {
        final var strBuilder = new StringBuilder();
        final List<Role> roles = guild.getRoles();
        for (var index = 0; index < roles.size(); index++) {
            strBuilder.append(
                    padRight(roles.get(index).getName().replace("@everyone", "Members with no role") + ":",
                            25) + guild.getMembersWithRoles(roles.get(index)).size() + "\n");
        }
        return strBuilder.toString();
    }
}
