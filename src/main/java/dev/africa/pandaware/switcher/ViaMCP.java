package dev.africa.pandaware.switcher;

import dev.africa.pandaware.switcher.gui.GuiProtocolSlider;
import dev.africa.pandaware.switcher.loader.MCPBackwardsLoader;
import dev.africa.pandaware.switcher.loader.MCPRewindLoader;
import dev.africa.pandaware.switcher.loader.MCPViaLoader;
import dev.africa.pandaware.switcher.platform.MCPViaInjector;
import dev.africa.pandaware.switcher.platform.MCPViaPlatform;
import dev.africa.pandaware.switcher.utils.JLoggerToLog4j;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.viaversion.viaversion.ViaManagerImpl;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.data.MappingDataLoader;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import io.netty.channel.EventLoop;
import io.netty.channel.local.LocalEventLoopGroup;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Logger;

public class ViaMCP {
    public final static int PROTOCOL_VERSION = 47;
    private static final ViaMCP instance = new ViaMCP();
    public GuiProtocolSlider guiProtocolSlider =
            new GuiProtocolSlider(69, 4 + 45, 10, 110, 20, ProtocolVersion.getProtocol(ViaMCP.PROTOCOL_VERSION));

    public static ViaMCP getInstance() {
        return instance;
    }

    public boolean loaded;
    private final Logger jLogger = new JLoggerToLog4j(LogManager.getLogger("ViaMCP"));
    private final CompletableFuture<Void> INIT_FUTURE = new CompletableFuture<>();

    private ExecutorService ASYNC_EXEC;
    private EventLoop EVENT_LOOP;

    private File file;
    private int version;
    private String lastServer;

    public void start() {
        ThreadFactory factory = new ThreadFactoryBuilder().setDaemon(true).setNameFormat("ViaMCP-%d").build();
        ASYNC_EXEC = Executors.newFixedThreadPool(8, factory);

        EVENT_LOOP = new LocalEventLoopGroup(1, factory).next();
        EVENT_LOOP.submit(INIT_FUTURE::join);

        setVersion(PROTOCOL_VERSION);
        this.file = new File("ViaMCP");
        if (this.file.mkdir()) {
            this.getjLogger().info("Creating ViaMCP Folder");
        }

        Via.init(ViaManagerImpl.builder().injector(new MCPViaInjector()).loader(new MCPViaLoader()).platform(new MCPViaPlatform(file)).build());

        MappingDataLoader.enableMappingsCache();
        ((ViaManagerImpl) Via.getManager()).init();

        new MCPBackwardsLoader(file);
        new MCPRewindLoader(file);

        INIT_FUTURE.complete(null);
        loaded = true;
    }

    public Logger getjLogger() {
        return jLogger;
    }

    public CompletableFuture<Void> getInitFuture() {
        return INIT_FUTURE;
    }

    public ExecutorService getAsyncExecutor() {
        return ASYNC_EXEC;
    }

    public EventLoop getEventLoop() {
        return EVENT_LOOP;
    }

    public File getFile() {
        return file;
    }

    public String getLastServer() {
        return lastServer;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setLastServer(String lastServer) {
        this.lastServer = lastServer;
    }
}
