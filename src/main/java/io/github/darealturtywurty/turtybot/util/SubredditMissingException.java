package io.github.darealturtywurty.turtybot.util;

public class SubredditMissingException extends RuntimeException {

	private static final long serialVersionUID = 8083743466462059952L;
	private final String description;

	public SubredditMissingException(final String description) {
		this.description = description;
	}

	@Override
	public String getMessage() {
		return this.description;
	}
}
