package io.github.darealturtywurty.turtybot.help_system;

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

	protected HelpData(long ownerID, String title, String description, String media, String logs) {
		this.ownerID = ownerID;
		this.title = title;
		this.description = description;
		this.media = media;
		this.logs = logs;
	}

	public long getOwner() {
		return this.ownerID;
	}

	public String getTitle() {
		return this.title;
	}

	public String getDescription() {
		return this.description;
	}

	public String getMedia() {
		return this.media;
	}

	public String getLogs() {
		return this.logs;
	}

	protected void setOwner(long ownerID) {
		this.ownerID = ownerID;
	}

	protected void setTitle(String title) {
		this.title = title;
	}

	protected void setDescription(String description) {
		this.description = description;
	}

	protected void setMedia(String media) {
		this.media = media;
	}

	protected void setLogs(String logs) {
		this.logs = logs;
	}
}