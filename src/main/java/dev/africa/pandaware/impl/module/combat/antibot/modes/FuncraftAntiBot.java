package dev.africa.pandaware.impl.module.combat.antibot.modes;

import dev.africa.pandaware.Client;
import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.mode.ModuleMode;
import dev.africa.pandaware.impl.event.player.PacketEvent;
import dev.africa.pandaware.impl.event.player.UpdateEvent;
import dev.africa.pandaware.impl.module.combat.antibot.AntiBotModule;
import dev.africa.pandaware.utils.player.PlayerUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.S02PacketChat;

public class FuncraftAntiBot extends ModuleMode<AntiBotModule> {

    @EventHandler
    EventCallback<UpdateEvent> onUpdate = event -> {
        mc.theWorld.playerEntities.forEach(entityPlayer -> {
            if (entityPlayer.getHealth() != 68 && entityPlayer.getHealth() != 1636) {
                Client.getInstance().getIgnoreManager().add(entityPlayer, false);
            } else {
                Client.getInstance().getIgnoreManager().remove(entityPlayer, false);
            }
        });
    };

    public FuncraftAntiBot(String name, AntiBotModule parent) {
        super(name, parent);
    }

    @Override
    public void onDisable() {
        Client.getInstance().getIgnoreManager().getIgnoreList().clear();
    }
}
