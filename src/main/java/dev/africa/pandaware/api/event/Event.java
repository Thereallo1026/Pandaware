package dev.africa.pandaware.api.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class Event {
    private boolean cancelled;

    public void cancel() {
        this.cancelled = true;
    }

    @Getter
    @AllArgsConstructor
    public enum EventState {
        PRE("Pre"),
        POST("Post");

        private final String label;
    }
}
