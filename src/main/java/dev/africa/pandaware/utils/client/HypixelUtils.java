package dev.africa.pandaware.utils.client;

import lombok.experimental.UtilityClass;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

@UtilityClass
public class HypixelUtils {
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

    public void isOnHypixel() {

    }
}
