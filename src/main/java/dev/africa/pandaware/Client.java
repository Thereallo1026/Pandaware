package dev.africa.pandaware;

import com.github.javafaker.Faker;
import dev.africa.pandaware.api.event.manager.EventDispatcher;
import dev.africa.pandaware.api.interfaces.Initializable;
import dev.africa.pandaware.api.manifest.Manifest;
import dev.africa.pandaware.impl.event.EventListener;
import dev.africa.pandaware.impl.font.Fonts;
import dev.africa.pandaware.impl.microshit.MicrosoftProvider;
import dev.africa.pandaware.impl.socket.SocketHandler;
import dev.africa.pandaware.impl.ui.clickgui.ClickGUI;
import dev.africa.pandaware.impl.ui.menu.account.GuiAccountManager;
import dev.africa.pandaware.manager.account.AccountManager;
import dev.africa.pandaware.manager.command.CommandManager;
import dev.africa.pandaware.manager.config.ConfigManager;
import dev.africa.pandaware.manager.file.FileManager;
import dev.africa.pandaware.manager.ignore.IgnoreManager;
import dev.africa.pandaware.manager.module.ModuleManager;
import dev.africa.pandaware.manager.notification.NotificationManager;
import dev.africa.pandaware.switcher.ViaMCP;
import dev.africa.pandaware.impl.socket.util.HWIDUtil;
import dev.africa.pandaware.utils.client.ServerUtils;
import dev.africa.pandaware.utils.network.GameListener;
import dev.africa.pandaware.utils.network.NetworkUtils;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.main.Main;
import org.lwjgl.opengl.Display;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Getter
public class Client implements Initializable {
    @Getter
    private static final Client instance = new Client();

    private final Manifest manifest = new Manifest(
            "Pandaware", "0.3",
            "cummy", "0069", false
    );

    private final ScheduledExecutorService executor = Executors
            .newScheduledThreadPool(Runtime.getRuntime().availableProcessors() + 1);

    private final EventDispatcher eventDispatcher = new EventDispatcher();
    private final FileManager fileManager = new FileManager();
    private final ModuleManager moduleManager = new ModuleManager();
    private final CommandManager commandManager = new CommandManager();
    private final ClickGUI clickGUI = new ClickGUI();

    private final ConfigManager configManager = new ConfigManager();
    private final AccountManager accountManager = new AccountManager();
    private final Faker faker = new Faker();
    private final GuiAccountManager guiAccountManager = new GuiAccountManager();
    private final NotificationManager notificationManager = new NotificationManager();
    private final IgnoreManager ignoreManager = new IgnoreManager();
    private final MicrosoftProvider microsoftProvider = new MicrosoftProvider();
    private final SocketHandler socketHandler = new SocketHandler();
    private final HWIDUtil hwidUtil = new HWIDUtil();
    private final GameListener gameListener = new GameListener();

    @Setter
    private boolean isKillSwitch;

    @Setter
    private boolean firstLaunch = true;

    @Setter
    private float renderDeltaTime;

    @Setter
    private String discordUsername;

    @Setter
    private String discordID;

    @Override
    public void init() {
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

        this.initMisc();

        new Thread(() -> {
            this.socketHandler.start();
            String result = null;

            try {
                result = NetworkUtils.getFromURL("https://pastebin.com/raw/diKx5qkE", null, false);
            } catch (IOException ignored) {
                isKillSwitch = true;
            }

            if (result == null || result.length() < 2 || Boolean.parseBoolean(result)) {
                isKillSwitch = true;
            }
        }).start();

        this.initVia();

        Fonts.getInstance().init();

        this.moduleManager.init();
        this.commandManager.init();
        this.clickGUI.init();
        this.fileManager.init();
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

        try {
            ServerUtils.checkHosts();
        } catch (Exception ignored) {
        }

        Display.setTitle("Pandaware (Beta) - " + this.manifest.getClientVersion());
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
}