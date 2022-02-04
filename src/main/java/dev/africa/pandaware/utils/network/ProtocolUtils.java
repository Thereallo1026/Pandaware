package dev.africa.pandaware.utils.network;

import dev.africa.pandaware.switcher.ViaMCP;
import dev.africa.pandaware.switcher.protocols.ProtocolCollection;

public class ProtocolUtils {

    public static boolean isOneDotEight() {
        return ViaMCP.getInstance().getVersion() == ProtocolCollection.R1_8.getVersion().getVersion();
    }
}