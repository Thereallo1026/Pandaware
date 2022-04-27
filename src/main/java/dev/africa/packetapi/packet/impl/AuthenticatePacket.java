package dev.africa.packetapi.packet.impl;

import dev.africa.packetapi.packet.api.Packet;
import lombok.Getter;

@Getter
public class AuthenticatePacket extends Packet {
    private final String hash;
    private final String uuid;
    private final String name;

    public AuthenticatePacket(String hash, String uuid, String name) {
        super("AuthenticatePacket");

        this.hash = hash;
        this.uuid = uuid;
        this.name = name;
    }
}
