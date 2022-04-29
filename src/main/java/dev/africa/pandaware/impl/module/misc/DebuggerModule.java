package dev.africa.pandaware.impl.module.misc;

import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.Module;
import dev.africa.pandaware.api.module.interfaces.Category;
import dev.africa.pandaware.api.module.interfaces.ModuleInfo;
import dev.africa.pandaware.impl.event.player.PacketEvent;
import dev.africa.pandaware.impl.setting.BooleanSetting;
import dev.africa.pandaware.utils.client.Printer;
import net.minecraft.network.play.client.C00PacketKeepAlive;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.play.server.S3FPacketCustomPayload;

@ModuleInfo(name = "Debugger", category = Category.MISC)
public class DebuggerModule extends Module {
    private final BooleanSetting keepAlive = new BooleanSetting("KeepAlives", true);
    private final BooleanSetting transaction = new BooleanSetting("Transactions", true);
    private final BooleanSetting payloads = new BooleanSetting("Custom Payloads", false);
    private final BooleanSetting everyPacket = new BooleanSetting("Debug every packet", false);
    private final BooleanSetting everyPacketSent = new BooleanSetting("Debug only sent packets", false, this.everyPacket::getValue);
    private final BooleanSetting everyPacketReceived = new BooleanSetting("Debug only received packets", false, this.everyPacket::getValue);
    private long lastKeepAlive;
    private long lastTransaction;

    public DebuggerModule() {
        this.registerSettings(
                this.transaction,
                this.keepAlive,
                this.payloads,
                this.everyPacket,
                this.everyPacketSent,
                this.everyPacketReceived
        );
    }

    @EventHandler
    EventCallback<PacketEvent> onPacket = event -> {
        if (this.everyPacket.getValue()) Printer.chat(event.getPacket());
        if ((event.getPacket() instanceof C0FPacketConfirmTransaction) && transaction.getValue()) {
            long lastPacket = System.currentTimeMillis() - lastTransaction;
            Printer.chat("sent tranny packet " + ((C0FPacketConfirmTransaction) event.getPacket()).getWindowId()
                    + " " + ((C0FPacketConfirmTransaction) event.getPacket()).getUid() + " " + lastPacket + "ms");
            this.lastTransaction = System.currentTimeMillis();
        }
        if (event.getPacket() instanceof C00PacketKeepAlive && keepAlive.getValue()) {
            long lastPacket = System.currentTimeMillis() - lastKeepAlive;
            Printer.chat("sent african packet " + ((C00PacketKeepAlive) event.getPacket()).getKey() + " " + lastPacket + "ms");
            this.lastKeepAlive = System.currentTimeMillis();
        }
        if (event.getPacket() instanceof S3FPacketCustomPayload && payloads.getValue()) {
            Printer.chat("Server: DROPPING PAYLOAD ON HIROSHIMA " + ((S3FPacketCustomPayload) event.getPacket()).getChannelName());
        }
    };
}
