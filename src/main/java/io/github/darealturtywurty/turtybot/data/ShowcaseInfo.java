package io.github.darealturtywurty.turtybot.data;

public class ShowcaseInfo {

	public final long originalMessageID;
	public long starboardMessageID;
	private int stars;

	public ShowcaseInfo(long messageID) {
		this.originalMessageID = messageID;
	}

	public void setStars(int stars) {
		if (stars < 0)
			this.stars = 0;
		else
			this.stars = stars;
	}

	public int getStars() {
		return this.stars;
	}
}
