package dev.africa.pandaware.impl.module.combat.antibot.modes;

import dev.africa.pandaware.Client;
import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.mode.ModuleMode;
import dev.africa.pandaware.impl.event.player.PacketEvent;
import dev.africa.pandaware.impl.event.player.UpdateEvent;
import dev.africa.pandaware.impl.module.combat.KillAuraModule;
import dev.africa.pandaware.impl.module.combat.antibot.AntiBotModule;
import dev.africa.pandaware.impl.module.combat.velocity.VelocityModule;
import dev.africa.pandaware.impl.setting.BooleanSetting;
import dev.africa.pandaware.utils.client.Printer;
import dev.africa.pandaware.utils.math.TimeHelper;
import dev.africa.pandaware.utils.network.GameListener;
import dev.africa.pandaware.utils.player.PlayerUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S0CPacketSpawnPlayer;

public class HypixelAntiBot extends ModuleMode<AntiBotModule> {
    private final BooleanSetting staffBot = new BooleanSetting("Staff Bot Detector (beta)", false);
    private boolean reset;
    private boolean bot;
    private final TimeHelper timer = new TimeHelper();
    GameListener listener = Client.getInstance().getGameListener();
    @EventHandler
    EventCallback<PacketEvent> onPacket = event -> {
        if (event.getPacket() instanceof S02PacketChat) {
            S02PacketChat packet = (S02PacketChat) event.getPacket();
            String text = packet.getChatComponent().getUnformattedText();
            if (text.contains("?????????????????????????????????")) {
                timer.reset();
                bot = false;
                Client.getInstance().getIgnoreManager().getIgnoreList().clear();
            }
        }

        if (staffBot.getValue()) {
            if ((listener.getLobbyType() != GameListener.LobbyType.SKY_WARS && listener.getLobbyType() != GameListener.LobbyType.BED_WARS)
                    && listener.getLobbyStatus() != GameListener.LobbyStatus.WAITING) return;
            if (event.getPacket() instanceof S0CPacketSpawnPlayer) {
                if (listener.getLobbyStatus() == GameListener.LobbyStatus.STARTED && !reset) {
                    reset = true;
                    timer.reset();
                }

                if (!bot && timer.reach(2000)) {
                    Printer.chat("STAFF BOT DETECTED");
                    bot = true;
                    Client.getInstance().getModuleManager().getByClass(KillAuraModule.class).toggle(false);
                    Client.getInstance().getModuleManager().getByClass(VelocityModule.class).toggle(false);
                }
            }
        }
    };

    @EventHandler
    EventCallback<UpdateEvent> onUpdate = event -> {
        if ((listener.getLobbyType() != GameListener.LobbyType.SKY_WARS && listener.getLobbyType() != GameListener.LobbyType.BED_WARS)
                || listener.getLobbyStatus() == GameListener.LobbyStatus.WAITING) {
            timer.reset();
            return;
        }

        if (mc.thePlayer.ticksExisted % 20 == 0) {
            Client.getInstance().getIgnoreManager().getIgnoreList().clear();
            for (Entity entity : mc.theWorld.loadedEntityList) {
                if (!PlayerUtils.getPlayerList().contains(entity) && entity.isInvisible() && entity instanceof EntityPlayer) {
                    Client.getInstance().getIgnoreManager().add((EntityPlayer) entity, false);
                }
            }
        }
    };

    public HypixelAntiBot(String name, AntiBotModule parent) {
        super(name, parent);

        this.registerSettings(this.staffBot);
    }

    @Override
    public void onDisable() {
        timer.reset();
        reset = false;
        bot = false;
        Client.getInstance().getIgnoreManager().getIgnoreList().clear();
    }
}
