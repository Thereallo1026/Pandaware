package dev.africa.packetapi.packet.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

@Getter
@RequiredArgsConstructor
public class Packet implements Serializable {
    private static final long serialVersionUID = 13L;

    private final String packetName;
}
