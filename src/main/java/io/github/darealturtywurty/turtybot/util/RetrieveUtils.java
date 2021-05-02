package io.github.darealturtywurty.turtybot.util;

import java.util.function.Consumer;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;

public interface RetrieveUtils {
	static void getUser(long id, JDA jda, Consumer<User> callback) {
		User user = jda.getUserById(id);

		if (user == null)
			jda.retrieveUserById(id).queue(callback);
		else
			callback.accept(user);
	}
}