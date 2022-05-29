package dev.africa.pandaware.manager.discord;

import dev.africa.pandaware.Client;
import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;

public class DiscordRP {
    private long created = 0;

    public void updateStatus(String lineOne, String lineTwo) {
        DiscordRichPresence.Builder builder = new DiscordRichPresence.Builder(lineTwo);
        builder.setBigImage("large", Client.getInstance().randomTitleText);
        builder.setDetails(lineOne);
        builder.setStartTimestamps(created);
        DiscordRPC.discordUpdatePresence(builder.build());
        System.out.println("[DISCORD RPC] Updated Status.");
    }

    public void start() {
        this.created = System.currentTimeMillis();
        System.out.println("[DISCORD RPC] Starting RPC...");

        DiscordEventHandlers discordEventHandlers = new DiscordEventHandlers.Builder().setReadyEventHandler(discordUser -> {

            System.out.println("[DISCORD RPC] " + discordUser.username + discordUser.discriminator + discordUser.avatar);
            System.out.println("[DISCORD RPC] User Found: " + discordUser.username + "#" + discordUser.discriminator + " (" + discordUser.userId + ")");

            updateStatus("Launching " + Client.getInstance().getManifest().getClientName() + Client.getInstance().getManifest().getClientVersion() + ".", "...Yep, there's RPC now.");
            System.out.println("[DISCORD RPC] Launch Status Update.");

        }).build();
        DiscordRPC.discordInitialize("980137797866360912", discordEventHandlers, true);

        new Thread("Discord RPC Callback") {
            @Override
            public void run() {
                DiscordRPC.discordRunCallbacks();
            }
        }.start();


    }

    public void shutdown() {
        DiscordRPC.discordShutdown();
    }


}
