package dev.africa.pandaware.impl.socket;

import dev.africa.pandaware.Client;
import dev.africa.pandaware.impl.module.render.HUDModule;
import dev.africa.pandaware.utils.client.Printer;
import me.rhys.packet.api.Direction;
import me.rhys.packet.api.Packet;
import me.rhys.packet.api.Packets;
import me.rhys.packet.impl.PacketIRCMessage;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.gen.structure.StructureOceanMonumentPieces;

public class PacketHandle {

    public void handlePacket(Packet packet, long now) {

        switch (packet.getPacketName()) {
            case Packets.SERVER_PING: {
                Client.getInstance().getSocketHandler().setLastKeepAlive(now);
                break;
            }

            case Packets.IRC_MESSAGE: {
                HUDModule hudModule = Client.getInstance().getModuleManager().getByClass(HUDModule.class);
                if (!hudModule.getIrc().getValue() && hudModule.getData().isEnabled()) return;

                PacketIRCMessage wrapped = (PacketIRCMessage) packet;

                if (wrapped.getDirection() != Direction.SERVER) return;

                String data = wrapped.getContent();

                if (data.contains(":")) {
                    String[] split = data.split(":");

                    if (split.length > 0) {
                        String username = split[0];
                        String message = split[1].replaceAll("[^\\p{ASCII}]", "");

                        Printer.chat(EnumChatFormatting.GRAY + "IRC > " + EnumChatFormatting.RED
                                + username + " " + EnumChatFormatting.WHITE + message);
                    }
                }
                break;
            }
        }
    }
}
