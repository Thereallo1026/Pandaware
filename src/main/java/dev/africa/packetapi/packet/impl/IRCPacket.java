package dev.africa.packetapi.packet.impl;

import dev.africa.packetapi.packet.api.Packet;
import lombok.Getter;

@Getter
public class IRCPacket extends Packet {
    private final String user;
    private final String message;

    public IRCPacket(String user, String message) {
        super("IRCPacket");

        this.user = user;
        this.message = message;
    }
}
