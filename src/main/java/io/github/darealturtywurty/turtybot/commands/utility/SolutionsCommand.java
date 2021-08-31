package io.github.darealturtywurty.turtybot.commands.utility;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.vavr.control.Either;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class SolutionsCommand {

    private final Map<String, Solution> solutions = new HashMap<>();

    private void addSolution(final Solution solution) {
        solution.aliases.forEach(alias -> this.solutions.put(alias, solution));
    }

    private static final class Solution {
        private boolean shouldDM = false;

        private final Set<String> aliases = new HashSet<>();
        private final Either<String, MessageEmbed> message;

        private Solution(final String name, final Either<String, MessageEmbed> message) {
            addAliases(name);
            this.message = message;
        }

        public static Solution createSolution(final String name, final Either<String, MessageEmbed> message) {
            return new Solution(name, message);
        }

        public Solution addAliases(final String... aliases) {
            this.aliases.addAll(Arrays.asList(aliases));
            return this;
        }

        public Solution shouldDM() {
            this.shouldDM = true;
            return this;
        }
    }
}
