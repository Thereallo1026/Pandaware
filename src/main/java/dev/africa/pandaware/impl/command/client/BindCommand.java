package dev.africa.pandaware.impl.command.client;

import dev.africa.pandaware.Client;
import dev.africa.pandaware.api.command.Command;
import dev.africa.pandaware.api.command.interfaces.CommandInformation;
import dev.africa.pandaware.api.module.Module;
import dev.africa.pandaware.utils.client.Printer;
import org.lwjgl.input.Keyboard;

import java.util.concurrent.atomic.AtomicReference;

@CommandInformation(name = "Bind", description = "Binds a module to a specified key")
public class BindCommand extends Command {

    @Override
    public void process(String[] arguments) {
        if (arguments.length >= 3) {
            AtomicReference<Module> module = new AtomicReference<>(null);

            Client.getInstance().getModuleManager().getMap().forEach((m, s) -> {
                if (s.equalsIgnoreCase(arguments[1])) {
                    module.set(m);
                } else {
                    if (m.getData().getShortcuts().length > 0) {
                        for (String shortcut : m.getData().getShortcuts()) {
                            if (shortcut.equalsIgnoreCase(arguments[1])) {
                                module.set(m);
                            }
                        }
                    }
                }
            });

            if (module.get() != null) {
                String key = arguments[2].toUpperCase();

                module.get().getData().setKey(Keyboard.getKeyIndex(key));
                Printer.chat("§aBound §7" + module.get().getData().getName() + "§a to §c" + key);
            } else {
                Printer.chat("§cModule not found");
            }
        } else {
            this.sendInvalidArgumentsMessage("module", "key");
        }
    }
}
