package dev.africa.packetapi.packet.impl;

import dev.africa.packetapi.packet.api.Packet;
import lombok.Getter;

@Getter
public class PingPacket extends Packet {
    private final long currentTime;

    public PingPacket(long currentTime) {
        super("PingPacket");

        this.currentTime = currentTime;
    }
}
