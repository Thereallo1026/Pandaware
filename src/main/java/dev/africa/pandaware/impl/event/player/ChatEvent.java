package dev.africa.pandaware.impl.event.player;

import dev.africa.pandaware.api.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ChatEvent extends Event {
    private final String message;
}
