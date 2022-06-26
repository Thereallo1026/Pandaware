package dev.africa.pandaware.impl.command.client;

import dev.africa.pandaware.api.command.Command;
import dev.africa.pandaware.api.command.interfaces.CommandInformation;

@CommandInformation(name = "IRCReconnect", description = "fucking reconnects to the irc what else would it be retard")
public class IRCReconnectCommand extends Command {
    private long lastMessage;

    @Override
    public void process(String[] arguments) {

    }
}