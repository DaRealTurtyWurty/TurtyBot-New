package io.github.darealturtywurty.turtybot.util.data;

public class HelpData {
    private long ownerID;
    private String title;
    private String description;
    private String media;
    private String logs;

    public HelpData() {
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

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setLogs(final String logs) {
        this.logs = logs;
    }

    public void setMedia(final String media) {
        this.media = media;
    }

    public void setOwner(final long ownerID) {
        this.ownerID = ownerID;
    }

    public void setTitle(final String title) {
        this.title = title;
    }
}