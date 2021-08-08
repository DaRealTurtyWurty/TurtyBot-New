package io.github.darealturtywurty.turtybot.managers.help_system;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "title", "media", "description", "logs" })
public class HelpData {

	@JsonProperty("owner")
	private long ownerID;

	@JsonProperty("title")
	private String title;

	@JsonProperty("description")
	private String description;

	@JsonProperty("media")
	private String media;

	@JsonProperty("logs")
	private String logs;

	protected HelpData() {
	}

	protected HelpData(final long ownerID, final String title, final String description, final String media,
			final String logs) {
		this.ownerID = ownerID;
		this.title = title;
		this.description = description;
		this.media = media;
		this.logs = logs;
	}

	public String getDescription() {
		return this.description;
	}

	public String getLogs() {
		return this.logs;
	}

	public String getMedia() {
		return this.media;
	}

	public long getOwner() {
		return this.ownerID;
	}

	public String getTitle() {
		return this.title;
	}

	protected void setDescription(final String description) {
		this.description = description;
	}

	protected void setLogs(final String logs) {
		this.logs = logs;
	}

	protected void setMedia(final String media) {
		this.media = media;
	}

	protected void setOwner(final long ownerID) {
		this.ownerID = ownerID;
	}

	protected void setTitle(final String title) {
		this.title = title;
	}
}