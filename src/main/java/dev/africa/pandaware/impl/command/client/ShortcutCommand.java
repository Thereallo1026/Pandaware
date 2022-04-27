package dev.africa.pandaware.impl.command.client;

import dev.africa.pandaware.Client;
import dev.africa.pandaware.api.command.Command;
import dev.africa.pandaware.api.command.interfaces.CommandInformation;
import dev.africa.pandaware.utils.client.Printer;

import java.util.Arrays;

@CommandInformation(name = "Shortcuts", description = "lists shortcuts for .bind and .toggle")
public class ShortcutCommand extends Command {
    @Override
    public void process(String[] arguments) {
        Printer.chat("§aList of shortcuts:");
        Client.getInstance().getModuleManager().getMap().values().forEach(module -> {
            if (module.getData().getShortcuts().length > 0) {
                Printer.chat(String.format("§f%s §7- §f%s", module.getData().getName(), Arrays.toString(module.getData().getShortcuts())));
            }
        });
    }
}
