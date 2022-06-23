package dev.africa.pandaware.impl.command.client;

import dev.africa.pandaware.Client;
import dev.africa.pandaware.api.command.Command;
import dev.africa.pandaware.api.command.interfaces.CommandInformation;
import dev.africa.pandaware.utils.client.HWIDUtils;
import dev.africa.pandaware.utils.client.Printer;
import me.rhys.packet.api.Direction;
import me.rhys.packet.impl.PacketClientAuthenticate;
import net.minecraft.util.EnumChatFormatting;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@CommandInformation(name = "IRCReconnect", description = "fucking reconnects to the irc what else would it be retard")
public class IRCReconnectCommand extends Command {
    private long lastMessage;

    @Override
    public void process(String[] arguments) {
        String hwid = HWIDUtils.getHWID();
        String ircName = Client.getInstance().getSocketHandler().getIrcName();
        if (!Client.getInstance().getSocketHandler().isConnected()
                || Client.getInstance().getSocketHandler().getIrcName() == null) {
            Printer.chat(EnumChatFormatting.RED + "IRC is not connected, type .setIRCName <Username> to connect.");
        } else {
            long now = System.currentTimeMillis();
            if ((now - this.lastMessage) > TimeUnit.SECONDS.toSeconds(10)) {
                Client.getInstance().getSocketHandler().queuePacket(new PacketClientAuthenticate(Base64.getEncoder().encodeToString(
                        (ircName + ":" + hwid).getBytes(StandardCharsets.UTF_8)), Direction.CLIENT));
                Printer.chat(EnumChatFormatting.RED + "Attempting a reconnect");

                this.lastMessage = now;
            } else {
                Printer.chat(EnumChatFormatting.RED + "Please wait before trying to reconnect again");
            }
        }
    }
}