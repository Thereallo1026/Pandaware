package dev.africa.pandaware.impl.command.client;

import dev.africa.pandaware.Client;
import dev.africa.pandaware.api.command.Command;
import dev.africa.pandaware.api.command.interfaces.CommandInformation;
import dev.africa.pandaware.utils.client.Printer;
import me.rhys.packet.api.Direction;
import me.rhys.packet.impl.PacketIRCMessage;
import net.minecraft.util.EnumChatFormatting;

import java.util.concurrent.TimeUnit;

@CommandInformation(name = "IRC", description = "fucking uses irc what else would it be retard")
public class IRCCommand extends Command {

    private long lastMessage;

    @Override
    public void process(String[] arguments) {

        if (!Client.getInstance().getSocketHandler().isConnected()
                || Client.getInstance().getSocketHandler().getIrcName() == null) {
            Printer.chat(EnumChatFormatting.RED + "IRC is not connected, type .setIRCName <Username> to connect.");
            return;
        }

        if (arguments.length > 0) {
            StringBuilder stringBuilder = new StringBuilder();

            long now = System.currentTimeMillis();

            for (int i = 1; i < arguments.length; i++) {
                stringBuilder.append(arguments[i]).append(" ");
            }

            String message = stringBuilder.toString().trim();

            message = message.replaceAll("[^\\p{ASCII}]", "");

            if (message.length() <= 1 || message.length() > 32) {
                Printer.chat(EnumChatFormatting.RED + "Message is too long or short.");
            } else {
                if ((now - this.lastMessage) > TimeUnit.SECONDS.toMillis(2)) {
                    Client.getInstance().getSocketHandler().queuePacket(
                            new PacketIRCMessage(Direction.CLIENT, message));

                    this.lastMessage = now;
                } else {
                    Printer.chat("Please wait before sending another message.");
                }
            }

        } else {
            Printer.chat(EnumChatFormatting.RED + "Supply a message.");
        }
    }
}
