package io.github.darealturtywurty.turtybot.util.data;

public enum EmojiImageType {

    OK_HAND("ok-hand"), B_LETTER("b"), LAUGHING_CRYING("laugh-cry"), ONE_HUNDRED("100"), FIRE("fire"),
    WEARY("weary");

    public final String fileName;

    EmojiImageType(final String name) {
        this.fileName = name;
    }
}
