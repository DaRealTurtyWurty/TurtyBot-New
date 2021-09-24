package io.github.darealturtywurty.turtybot.util.data;

import java.util.function.LongSupplier;

import io.github.darealturtywurty.turtybot.managers.suggestions.SuggestionResponse;

public class SuggestionData {

    public SuggestionResponse response;
    public int suggestionNumber;
    public final long authorId;
    public final LongSupplier messageId;

    public SuggestionData(final SuggestionResponse response, final int suggestionNumber, final long authorId,
            final LongSupplier messageId) {
        this.response = response;
        this.suggestionNumber = suggestionNumber;
        this.authorId = authorId;
        this.messageId = messageId;
    }
}
