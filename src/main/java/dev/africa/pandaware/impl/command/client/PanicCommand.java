package dev.africa.pandaware.impl.command.client;

import dev.africa.pandaware.Client;
import dev.africa.pandaware.api.command.Command;
import dev.africa.pandaware.api.command.interfaces.CommandInformation;
import dev.africa.pandaware.api.module.Module;
import dev.africa.pandaware.utils.client.Printer;
import org.lwjgl.input.Keyboard;

@CommandInformation(name = "Panic", description = "Disables all modules")
public class PanicCommand extends Command {

    @Override
    public void process(String[] arguments) {
        Client.getInstance().getModuleManager().getAllModules().forEach(module -> module.toggle(false));
    }
}
