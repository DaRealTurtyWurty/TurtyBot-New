package io.github.darealturtywurty.turtybot.managers.help_system;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "title", "media" })
public class CloseData {

	@JsonProperty("owner")
	private long ownerID;

	protected CloseData() {
	}

	protected CloseData(final long ownerID) {
		this.ownerID = ownerID;
	}

	protected long getOwnerID() {
		return this.ownerID;
	}

	protected void setOwnerID(final long ownerID) {
		this.ownerID = ownerID;
	}
}
