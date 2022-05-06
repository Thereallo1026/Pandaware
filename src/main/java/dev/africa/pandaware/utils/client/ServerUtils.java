package dev.africa.pandaware.utils.client;

import dev.africa.pandaware.api.interfaces.MinecraftInstance;
import dev.africa.pandaware.utils.player.PlayerUtils;
import lombok.experimental.UtilityClass;
import net.minecraft.client.gui.GuiMultiplayer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

@UtilityClass
public class ServerUtils implements MinecraftInstance {
    public boolean compromised;

    public void checkHosts() throws Exception {
        if (System.getProperty("os.name").contains("Windows")) {
            File hostsFile = new File(System.getenv("WinDir") + "\\system32\\drivers\\etc\\hosts");

            try (BufferedReader br = new BufferedReader(new FileReader(hostsFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.toLowerCase().contains("hypixel")) {
                        compromised = true;

                        break;
                    }
                }
            }
        }
    }

    public boolean isOnServer(String ip) {
        return PlayerUtils.getServerIP().equals(ip) && !(mc.currentScreen instanceof GuiMultiplayer) &&
                !(mc.getCurrentServerData() == null);
    }
}
