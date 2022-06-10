package dev.africa.pandaware;

import com.github.javafaker.Faker;
import dev.africa.pandaware.api.event.manager.EventDispatcher;
import dev.africa.pandaware.api.interfaces.Initializable;
import dev.africa.pandaware.api.manifest.Manifest;
import dev.africa.pandaware.impl.event.EventListener;
import dev.africa.pandaware.impl.font.Fonts;
import dev.africa.pandaware.impl.microshit.MicrosoftProvider;
import dev.africa.pandaware.impl.protection.Debugger;
import dev.africa.pandaware.impl.protection.HWIDCheck;
import dev.africa.pandaware.impl.socket.SocketHandler;
import dev.africa.pandaware.impl.ui.clickgui.ClickGUI;
import dev.africa.pandaware.impl.ui.menu.account.GuiAccountManager;
import dev.africa.pandaware.manager.account.AccountManager;
import dev.africa.pandaware.manager.command.CommandManager;
import dev.africa.pandaware.manager.config.ConfigManager;
import dev.africa.pandaware.manager.discord.DiscordRP;
import dev.africa.pandaware.manager.file.FileManager;
import dev.africa.pandaware.manager.ignore.IgnoreManager;
import dev.africa.pandaware.manager.module.ModuleManager;
import dev.africa.pandaware.manager.notification.NotificationManager;
import dev.africa.pandaware.manager.script.ScriptManager;
import dev.africa.pandaware.switcher.ViaMCP;
import dev.africa.pandaware.utils.OsUtils;
import dev.africa.pandaware.utils.client.ServerUtils;
import dev.africa.pandaware.utils.java.FileUtils;
import dev.africa.pandaware.utils.network.GameListener;
import dev.africa.pandaware.utils.network.NetworkUtils;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.main.Main;
import net.minecraft.client.network.OldServerPinger;
import org.lwjgl.opengl.Display;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static dev.africa.pandaware.api.interfaces.MinecraftInstance.mc;

@Getter
public class Client implements Initializable {
    @Getter
    private static final Client instance = new Client();

    public String randomTitleText = FileUtils.getRandomTitleLine();

    private final Manifest manifest = new Manifest(
            "Pandaware", "0.4",
            "cummy", "0069", false
    );

    private final ScheduledExecutorService executor = Executors
            .newScheduledThreadPool(Runtime.getRuntime().availableProcessors() + 1);

    private final Debugger debugger = new Debugger();
    private final HWIDCheck hwid = new HWIDCheck();

    private final EventDispatcher eventDispatcher = new EventDispatcher();
    private final FileManager fileManager = new FileManager();
    private final ModuleManager moduleManager = new ModuleManager();
    private final CommandManager commandManager = new CommandManager();
    @Setter
    private ClickGUI clickGUI = new ClickGUI();

    private final ConfigManager configManager = new ConfigManager();
    private final AccountManager accountManager = new AccountManager();
    private final Faker faker = new Faker();
    private final GuiAccountManager guiAccountManager = new GuiAccountManager();
    private final NotificationManager notificationManager = new NotificationManager();
    private final IgnoreManager ignoreManager = new IgnoreManager();
    private final MicrosoftProvider microsoftProvider = new MicrosoftProvider();
    private final SocketHandler socketHandler = new SocketHandler();
    private final GameListener gameListener = new GameListener();
    private final ScriptManager scriptManager = new ScriptManager();
    private final DiscordRP discordRP = new DiscordRP();

    private final List<String> files = new ArrayList<>();

    @Setter
    private boolean isKillSwitch;

    @Setter
    private boolean firstLaunch = true;

    @Setter
    private float renderDeltaTime;

    @Getter
    private boolean fdpClient;

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(10);

    private final OldServerPinger oldServerPinger = new OldServerPinger();

    private final Runnable pingRunnable = () -> {
        try {
            if (!(mc.currentScreen instanceof GuiMultiplayer) && !(mc.getCurrentServerData() == null) &&
                    mc.getCurrentServerData().serverIP.contains("hypixel")) {
                oldServerPinger.ping(mc.getCurrentServerData());
            }
        } catch (Throwable ignored) {
        }
    };

    @Override
    public void init() {
        if (System.getProperty("98aef67c-7cfe-4cb2-afc4-17fe02efdf37") == null) {
            return;
        }
        new Thread(() -> {
            try {
                ServerSocket socket = new ServerSocket(3000);
                while (true) {
                    Socket soc = socket.accept();
                    soc.setTcpNoDelay(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        scheduledExecutorService.scheduleAtFixedRate(pingRunnable, 0, 5, TimeUnit.SECONDS);

        this.checkFDPClient();
        this.initTitle();
        this.initDiscordRP();
        this.initMisc();

        new Thread(() -> {
            this.socketHandler.start();
            String result = null;

            try {
                result = NetworkUtils.getFromURL("https://pastebin.com/raw/dgULkjXS", null, false);
            } catch (IOException ignored) {
                isKillSwitch = true;
            }

            if (result == null || result.length() != 5 || Boolean.parseBoolean(result)) {
                isKillSwitch = true;
            }
        }).start();

        new Thread(() -> {
            String result2 = null;

            try {
                result2 = NetworkUtils.getFromURL("https://raw.githubusercontent.com/PhoenixHaven/PandawareVersion/main/version.txt", null, false);
            } catch (IOException e) {
                System.exit(-2);
            }

            if (result2 == null || !result2.equals(Client.getInstance().getManifest().getClientVersion())) {
                isKillSwitch = true;
            }
        }).start();

        this.initVia();

        Fonts.getInstance().init();

        this.moduleManager.init();
        this.fileManager.init();
        this.scriptManager.init();
        this.commandManager.init();
        this.clickGUI.init();
        this.configManager.init();
        try {
            this.microsoftProvider.getAuthManagerWebServer().start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.eventDispatcher.subscribe(new EventListener());

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    @Override
    public void shutdown() {
        this.fileManager.shutdown();
        this.configManager.shutdown();
    }

    void initMisc() {
        OptionParser optionParser = new OptionParser();
        optionParser.allowsUnrecognizedOptions();
        OptionSpec<Boolean> devMode = optionParser.accepts("pandawareDevMode").withRequiredArg()
                .ofType(Boolean.class).defaultsTo(false);
        OptionSpec<String> user = optionParser.accepts("pandawareUser").withRequiredArg()
                .defaultsTo("");
        OptionSpec<String> uid = optionParser.accepts("pandawareUid").withRequiredArg()
                .defaultsTo("");

        OptionSet optionSet = optionParser.parse(Main.args);

        if (optionSet.valueOf(devMode)) {
            this.manifest.setUsername(optionSet.valueOf(user));
            this.manifest.setUserId(optionSet.valueOf(uid));
            this.manifest.setDevMode(true);
        }

        for (String file : files) {
            if (file.toLowerCase().contains("fdpclient")) {
                fdpClient = true;
                break;
            }
        }

        try {
            ServerUtils.checkHosts();
        } catch (Exception ignored) {
        }
    }

    void initVia() {
        try {
            if (this.isKillSwitch) {
                throw new IllegalArgumentException();
            }
            ViaMCP.getInstance().start();
            ViaMCP.getInstance().initAsyncSlider();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void initTitle() {

        if (randomTitleText != null) {
            Display.setTitle("Pandaware (Beta) - " + this.manifest.getClientVersion() + " - " + randomTitleText);
            System.out.println("[STARTUP - TITLE SETTING] Set Title to: " + randomTitleText + ".");
        } else {
            Display.setTitle("Pandaware (Beta) - " + this.manifest.getClientVersion());
            System.out.println("[STARTUP - TITLE SETTING] Failed to set title, set default title.");
        }
    }

    void listFilesForFolder(final File folder) {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                files.add(fileEntry.getName());
            }
        }
    }

    void checkFDPClient() {
        String userHome = System.getProperty("user.home", ".");
        File fdp;
        switch (OsUtils.getOsType()) {
            case LINUX:
                fdp = new File(userHome, ".minecraft/mods");
                break;
            case WINDOWS:
                String applicationData = System.getenv("APPDATA");
                String folder = applicationData != null ? applicationData : userHome;
                fdp = new File(folder, ".minecraft/mods");
                break;
            case MAC:
                fdp = new File(userHome, "Library/Application Support/minecraft/mods");
                break;
            default:
                fdp = new File(userHome, "minecraft/mods");
                break;
        }
        if (fdp.exists()) {
            listFilesForFolder(fdp);
        }
    }

    void initDiscordRP() {
        System.out.println("Starting Discord RP...");
        discordRP.start();
    }
    //TODO: COMMIT ON RELEASE
   static {
       System.setProperty("142d97db-2d4e-45a4-94a0-a976cd34cce6", "a");
       System.setProperty("a755e611-6014-4ffa-8ab8-7204b31a840e", "b");
       System.setProperty("98aef67c-7cfe-4cb2-afc4-17fe02efdf37", "c");
       System.setProperty("24c6ba8a-4e4f-4bad-906a-8eff47f36e15", "d");
       System.setProperty("fcb4a890-3d2f-4c50-895a-845b4dde1a12", "e");
   }
}