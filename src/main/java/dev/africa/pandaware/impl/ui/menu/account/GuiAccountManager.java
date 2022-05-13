package dev.africa.pandaware.impl.ui.menu.account;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.Agent;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import dev.africa.pandaware.Client;
import dev.africa.pandaware.impl.font.Fonts;
import dev.africa.pandaware.impl.font.renderer.TTFFontRenderer;
import dev.africa.pandaware.impl.microshit.server.AuthManagerWebServer;
import dev.africa.pandaware.impl.module.misc.StreamerModule;
import dev.africa.pandaware.impl.ui.menu.button.CustomButton;
import dev.africa.pandaware.impl.ui.notification.Notification;
import dev.africa.pandaware.utils.client.MouseUtils;
import dev.africa.pandaware.utils.java.FileUtils;
import dev.africa.pandaware.utils.math.random.RandomUtils;
import dev.africa.pandaware.utils.network.NetworkUtils;
import dev.africa.pandaware.utils.render.ColorUtils;
import dev.africa.pandaware.utils.render.RenderUtils;
import io.netty.util.internal.ThreadLocalRandom;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Session;
import org.lwjgl.input.Mouse;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class GuiAccountManager extends GuiScreen {
    private UsernameTextBox usernameBox;
    private PasswordTextBox passwordBox;
    private TTFFontRenderer bigFont;
    private Account loggedInAccount;
    private Account selectedAccount;
    private double accountsY, scrolling;

    @Override
    public void initGui() {
        super.initGui();
        selectedAccount = null;

        try {
            bigFont = Fonts.getInstance().getProductSansVeryBig();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (loggedInAccount == null) {
            loggedInAccount = new Account("", mc.getSession().getUsername(), "", "", "", true, true, false);
        }

        usernameBox = new UsernameTextBox(0, width - 230, 55, 175, 17);
        passwordBox = new PasswordTextBox(1, width - 230, 55 + 20, 175, 17);
        customButtons.add(new CustomButton(0, width - 230, 55 + 20 + 20, 175, 17, "Login", true));
        customButtons.add(new CustomButton(1, width - 230, 55 + 20 + 20 + 20, 175, 17, "Import mail:password", true));
        customButtons.add(new CustomButton(2, width - 230, 55 + 20 + 20 + 20 + 20, 175, 17, "Random username login", true));
        customButtons.add(new CustomButton(3, width - 230, 55 + 20 + 20 + 20 + 20 + 20, 175, 17, "Anticheat login", true));
        customButtons.add(new CustomButton(4, width - 230, 55 + 20 + 20 + 20 + 20 + 20 + 20, 175, 17, "Add account", true));
        customButtons.add(new CustomButton(5, width - 230, 55 + 20 + 20 + 20 + 20 + 20 + 20 + 20, 175, 17, "Add Microsoft account", true));
        customButtons.add(new CustomButton(6, width - 230, 55 + 20 + 20 + 20 + 20 + 20 + 20 + 20 + 20, 175, 17, "Import accounts", true));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        bigFont.drawCenteredStringWithShadow("Account Manager", width / 2f, 20, -1);

        if (Mouse.hasWheel()) {
            if (accountsY > height - 50) {
                int wheel = Mouse.getDWheel();

                if (wheel > 0) {
                    scrolling -= 25;
                } else if (wheel < 0) {
                    scrolling += 25;
                }
            } else {
                scrolling = 0;
            }
        }

        scrolling = MathHelper.clamp_double(scrolling, 0, accountsY - 84);

        RenderUtils.startScissorBox();
        RenderUtils.drawScissorBox(70, 50, width - 240 - 70, height - 50 - 50);
        RenderUtils.drawRect(70, 50, width - 240, height - 50, new Color(0, 0, 0, 158).getRGB());

        float y = 52.5f;
        for (Account account : Client.getInstance().getAccountManager().getItems()) {
            boolean hovered = MouseUtils.isMouseInBounds(mouseX, mouseY, 72, y - scrolling, width - 240 - 2, y + 30 - scrolling) && mouseY < height - 50;

            RenderUtils.drawRect(72, y - scrolling, width - 240 - 2, y - scrolling + 30, new Color(0, 0, 0, (hovered ? 140 : 100)).getRGB());
            RenderUtils.drawRect(72 - 2, y - scrolling, 73 - 2, y - scrolling + 30, ColorUtils.rainbow(1, 1, 2).getRGB());

            if (selectedAccount == account) {
                RenderUtils.drawBorderedRect(72 + 1, y - scrolling, width - 240 - 2, y - scrolling + 30, 0.5, new Color(0, 0, 0, 30).getRGB(), -1);
            }

            boolean deleteHovered = MouseUtils.isMouseInBounds(mouseX, mouseY, width - 240 - 2 - 30 + 20 - 1, y - scrolling + 1, width - 240 - 2 - 1, y - scrolling + 30 - 20 + 1);
            RenderUtils.drawBorderedRect(width - 240 - 2 - 30 + 20 - 1, y - scrolling + 1, width - 240 - 2 - 1, y - scrolling + 30 - 20 + 1, 0.5, new Color(120, 0, 0).getRGB(), new Color(200, 0, 0).getRGB());
            RenderUtils.drawImage(new ResourceLocation(Client.getInstance().getManifest().getClientName().toLowerCase() + "/icons/x.png"), width - 240 - 2 - 30 + 1 + 20 - 1, (float) (y - scrolling + 1 + 1), 28 - 20, 28 - 20);

            RenderUtils.drawImage(new ResourceLocation(String.format(Client.getInstance().getManifest().getClientName().toLowerCase() + "/icons/auth/%s.png", (account.isMicrosoft() ? "soft" : "jank"))), width - 240 - 2 - 30 + 1 + 20 - 1 - 20 + 12, (float) (y - scrolling + 1 + 1 + 10), 28 - 20 + 9, 28 - 20 + 9);

            if (deleteHovered) {
                RenderUtils.drawRect(width - 240 - 2 - 30 + 20 - 1, y - scrolling + 1, width - 240 - 2 - 1, y - scrolling + 30 - 20 + 1, new Color(0, 0, 0, 40).getRGB());
            }

            boolean mojangEquals = account.getPassword().equalsIgnoreCase(loggedInAccount.getPassword()) &&
                    account.getEmail().equalsIgnoreCase(loggedInAccount.getEmail());

            boolean microsoftEquals = account.getUsername().equalsIgnoreCase(loggedInAccount.getUsername()) &&
                    account.getUuid().equalsIgnoreCase(loggedInAccount.getUuid());

            boolean isSameAccount =
                    (loggedInAccount.isCracked() ? account.getPassword().equalsIgnoreCase(loggedInAccount.getPassword()) &&
                            account.getEmail().equalsIgnoreCase(loggedInAccount.getEmail()) &&
                            account.getUsername().equalsIgnoreCase(loggedInAccount.getUsername()) :
                            (!loggedInAccount.isMicrosoft() ? mojangEquals : microsoftEquals));

            StreamerModule streamerModule = Client.getInstance().getModuleManager().getByClass(StreamerModule.class);
            Fonts.getInstance().getProductSansBig().drawStringWithShadow("Username: §7" + (account.getUsername().length() <= 0 ? account.getEmail() : streamerModule.getData().isEnabled() ? "Legit Player" : account.getUsername()) +
                            (isSameAccount ? " §a(Logged in)" : "") + (account.isMicrosoft() ? " §7(§9Microsoft§7)" : " §7(§cMojang§7)"),
                    74 + 2, (float) (y + 2 - scrolling), -1);
            Fonts.getInstance().getProductSansBig().drawStringWithShadow("Password: §7" + (account.isCracked() ? "No password" : (account.isMicrosoft() ? account.getUuid() : account.getPassword()).replaceAll("(?s).", "*")), 74 + 2, (float) (y + 15 - scrolling), -1);

            y += 32;
            accountsY = y;
        }

        RenderUtils.endScissorBox();

        if (usernameBox.getText().length() > 16 && !usernameBox.getText().contains("@") && !usernameBox.getText().contains(".")) {
            RenderUtils.drawRect(width - 235 + 12, 55 - 15, width - 50 - 13, 55 - 5, new Color(100, 100, 100, 200).getRGB());
            Fonts.getInstance().getProductSansBig().drawCenteredStringWithShadow("§4Username Exceeds 16 Characters!", width - 235 + 92, 55 - 15, -1);
        }

        RenderUtils.startScissorBox();
        RenderUtils.drawScissorBox(width - 235, 50, width - 50 - (width - 235), height - 50 - 50);
        RenderUtils.drawRect(width - 235, 50, width - 50, height - 50, new Color(0, 0, 0, 158).getRGB());

        usernameBox.drawTextBox();
        passwordBox.drawTextBox();

        if (usernameBox.getText().length() <= 0 && !usernameBox.isFocused()) {
            mc.fontRendererObj.drawStringWithShadow("Username", width - 230 + 4, 55 + 4, -1);
        }
        if (passwordBox.getText().length() <= 0 && !passwordBox.isFocused()) {
            mc.fontRendererObj.drawStringWithShadow("Password", width - 230 + 4, 55 + 20 + 4, -1);
        }

        if (loggedInAccount != null) {
            StreamerModule streamerModule = Client.getInstance().getModuleManager().getByClass(StreamerModule.class);
            Fonts.getInstance().getProductSansBig().drawCenteredStringWithShadow("Current: §7" + (streamerModule.getData().isEnabled() ? "Legit Player" : mc.getSession().getUsername()) + (loggedInAccount.isCracked() ? " §c(Cracked)" : ""), width - 235 + 92, 55 + 20 + 20 + 20 + 20 + 20 + 20 + 20 + 20 + 20, -1);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
        RenderUtils.endScissorBox();

        RenderUtils.drawRect(70, 50, width - 240, 51, ColorUtils.rainbow(1, 1, 2).getRGB());
        RenderUtils.drawRect(width - 235, 50, width - 50, 51, ColorUtils.rainbow(1, 1, 2).getRGB());
        RenderUtils.drawRect(70, height - 51, width - 240, height - 50, ColorUtils.rainbow(1, 1, 2).getRGB());
        RenderUtils.drawRect(width - 235, height - 51, width - 50, height - 50, ColorUtils.rainbow(1, 1, 2).getRGB());
    }

    @Override
    public void onGuiClosed() {
        Client.getInstance().getFileManager().saveAll();

        super.onGuiClosed();
    }

    @Override
    public void updateScreen() {
        usernameBox.updateCursorCounter();
        passwordBox.updateCursorCounter();

        super.updateScreen();
    }

    @Override
    protected void actionPerformed(CustomButton button) throws IOException {
        switch (button.id) {
            case 0:
                if (usernameBox.getText().length() > 0) {
                    login(usernameBox.getText(), passwordBox.getText(), "", "", false);
                } else {
                    Client.getInstance().getNotificationManager().addNotification(Notification.Type.ERROR, "Cannot login to a blank username", 5);
                }
                break;
            case 1:
                new Thread(() -> {
                    try {
                        String data = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
                        if (!data.contains(":")) {
                            Client.getInstance().getNotificationManager().addNotification(Notification.Type.ERROR, "Clipboard does not contain required text", 5);
                            return;
                        }

                        String[] credentials = data.split(":");
                        usernameBox.setText(credentials[0]);
                        passwordBox.setText(credentials[1]);
                    } catch (UnsupportedFlavorException | IOException e) {
                        e.printStackTrace();
                    }
                }).start();
                break;
            case 2: {
                int randomNumber = ThreadLocalRandom.current().nextInt(100);
                boolean addStart = ThreadLocalRandom.current().nextInt(20) < 10;
                boolean switchNext = ThreadLocalRandom.current().nextBoolean();
                boolean addEnding = ThreadLocalRandom.current().nextInt(50) > 35;
                boolean randomChar = ThreadLocalRandom.current().nextInt(25) < 10;
                String randomUsername = "";
                if (addStart) {
                    randomUsername = String.valueOf(ThreadLocalRandom.current().nextInt(
                            RandomUtils.nextInt(20, 100)));
                }
                if (randomNumber > 50 && randomNumber < 80) {
                    randomUsername += Client.getInstance().getFaker().superhero().name();
                } else if (randomNumber > 25 && randomNumber < 30) {
                    randomUsername += Client.getInstance().getFaker().cat().name();
                } else if (randomNumber > 10 && randomNumber < 25) {
                    randomUsername += Client.getInstance().getFaker().animal().name();
                } else if (randomNumber < 10) {
                    randomUsername += Client.getInstance().getFaker().app().name();
                } else {
                    randomUsername += (switchNext ? Client.getInstance().getFaker()
                            .harryPotter().character()
                            : Client.getInstance().getFaker().funnyName().name());
                }
                if (addEnding) {
                    randomUsername = String.valueOf(ThreadLocalRandom.current().nextInt(
                            RandomUtils.nextInt(5, 15)));
                }
                if (randomUsername.length() < 4) {
                    randomUsername += Client.getInstance().getFaker().dragonBall().character();
                }
                String compile = randomUsername.replaceAll("[^a-zA-Z0-9]", "");
                if (randomChar) {
                    compile += (ThreadLocalRandom.current().nextBoolean() ? "_" : "__");
                }
                if (compile.length() > 16) {
                    compile = compile.substring(0, 16);
                }

                login(compile, "", "", "", false);
                break;
            }
            case 3: {
                List<String> antiHacks = Arrays.asList(
                        "Deus", "Verus", "Taka",
                        "DAC", "Vulcan", "Karhu",
                        "Intave", "Matrix", "NCP",
                        "Guardian", "AAC", "Buzz",
                        "Spartan", "Sparky", "GCheat",
                        "Ghost", "AGC", "Watchdog",
                        "Sentinel", "Gwen", "ACR",
                        "Lighter", "Artemis", "Godseye",
                        "Oxygen", "Fate", "Overflow",
                        "Janitor", "Area51", "ABC",
                        "Hawk", "Horizon", "Kizuki",
                        "Alice", "AntiAura", "Wave",
                        "Reflex"
                );

                String randomAc = antiHacks.get(RandomUtils.nextInt(0, antiHacks.size() - 1));

                String user = randomAc + RandomUtils.random(RandomUtils.nextInt(2, 5), "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
                login(user, "", "", "", false);
                break;
            }
            case 4:
                if (usernameBox.getText().length() > 0) {
                    addToAccountList(usernameBox.getText(), passwordBox.getText(), "", "", false);
                } else {
                    Client.getInstance().getNotificationManager().addNotification(Notification.Type.ERROR, "Cannot add a blank username", 5);
                }
                break;
            case 5:
                Client.getInstance().getMicrosoftProvider().openUrl();
                break;
            case 6:
                Client.getInstance().getExecutor().execute(() -> {
                    try {
                        File altFile = FileUtils.openFilePicker(true);

                        if (altFile != null) {
                            FileReader fileReader = new FileReader(altFile);
                            BufferedReader bufferedReader = new BufferedReader(fileReader);

                            String line;
                            while ((line = bufferedReader.readLine().replaceFirst("\\[NFA]", "")) != null) {
                                String[] accountData = line.split(":");
                                String[] splitMail = accountData[0].split(" ");
                                String[] splitPassword = accountData[1].split(" ");

                                String mail = splitMail[0].split("\\|")[0];
                                String password = splitPassword[0].split("\\|")[0];

                                Account account = new Account(mail, "", password, "", "", false, false, false);

                                if (!Client.getInstance().getAccountManager().getItems().contains(account)) {
                                    Client.getInstance().getAccountManager().getItems().add(account);
                                }
                            }
                            fileReader.close();
                            bufferedReader.close();

                            Client.getInstance().getFileManager().saveAll();
                            Client.getInstance().getNotificationManager().addNotification(Notification.Type.SUCCESS, "Successfully imported accounts!", 5);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                break;
        }

        super.actionPerformed(button);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        usernameBox.textboxKeyTyped(typedChar, keyCode);
        passwordBox.textboxKeyTyped(typedChar, keyCode);

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        usernameBox.mouseClicked(mouseX, mouseY, mouseButton);
        passwordBox.mouseClicked(mouseX, mouseY, mouseButton);

        float y = 52.5f;
        for (int i = 0; i < Client.getInstance().getAccountManager().getItems().size(); i++) {
            Account account = Client.getInstance().getAccountManager().getItems().get(i);

            boolean hovered = MouseUtils.isMouseInBounds(mouseX, mouseY, 72, y - scrolling, width - 240 - 2, y + 30 - scrolling) && mouseY < height - 50;

            if (hovered && mouseButton == 0) {
                boolean deleteHovered = MouseUtils.isMouseInBounds(mouseX, mouseY, width - 240 - 2 - 30 + 20 - 1, y - scrolling + 1, width - 240 - 2 - 1, y - scrolling + 30 - 20 + 1);
                if (deleteHovered) {
                    Client.getInstance().getAccountManager().getItems().remove(account);
                    Client.getInstance().getFileManager().saveAll();
                    return;
                }

                if (selectedAccount != null && selectedAccount == account) {
                    if ((selectedAccount.getEmail().length() > 0 ? selectedAccount.getEmail() : selectedAccount.getUsername()).length() > 0) {
                        if (selectedAccount.isMicrosoft()) {
                            login((selectedAccount.getEmail().length() > 0 ? selectedAccount.getEmail() : selectedAccount.getUsername()), selectedAccount.getPassword(), selectedAccount.getRefreshToken(), selectedAccount.getUuid(), true);
                        } else {
                            login((selectedAccount.getEmail().length() > 0 ? selectedAccount.getEmail() : selectedAccount.getUsername()), selectedAccount.getPassword(), "", "", false);
                        }
                    } else {
                        Client.getInstance().getNotificationManager().addNotification(Notification.Type.ERROR, "Cannot login to a blank username", 5);
                    }
                }

                selectedAccount = account;
            }

            y += 32;
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private void login(String username, String password, String refreshToken, String uuid, boolean microsoft) {
        new Thread(() -> {
            if (password.length() <= 0 && !microsoft) {
                this.mc.setSession(new Session(username, "", "", "mojang"));

                this.loggedInAccount = new Account("", mc.getSession().getUsername(), "", "", "", true, true, false);
                Client.getInstance().getNotificationManager().addNotification(Notification.Type.SUCCESS, "Logged in! (" + mc.getSession().getUsername() + " - offline)", 5);
                mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("random.orb"), 1.0F));
                return;
            }
            Client.getInstance().getNotificationManager().addNotification(Notification.Type.INFORMATION, "Logging in...", 5);

            Session auth = microsoft ? createMicrosoftSession(username, uuid, getTokenMicrosoft(refreshToken)) : createSession(username, password);

            if (auth == null) {
                Client.getInstance().getNotificationManager().addNotification(Notification.Type.ERROR, "Login failed!", 5);
                mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("random.pop"), 1.0F));
            } else {
                if (Client.getInstance().isKillSwitch()) {
                    for (int i = 0; i < 100; i++) {
                        this.mc.setSession(auth);
                    }
                    return;
                }
                this.mc.setSession(auth);

                this.loggedInAccount = new Account(username, mc.getSession().getUsername(), password, refreshToken, uuid, false, true, microsoft);
                StreamerModule streamerModule = Client.getInstance().getModuleManager().getByClass(StreamerModule.class);
                Client.getInstance().getNotificationManager().addNotification(Notification.Type.SUCCESS, "Logged in! (" + (streamerModule.getData().isEnabled() ? "Legit Player" : mc.getSession().getUsername()) + ")", 5);
                mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("random.orb"), 1.0F));
            }
        }).start();
    }

    private void addToAccountList(String username, String password, String refreshToken, String uuid, boolean microsoft) {
        new Thread(() -> {
            if (passwordBox.getText().length() <= 0 && !microsoft) {
                Client.getInstance().getAccountManager().getItems().add(new Account("", username, "", "", "", true, false, false));
                Client.getInstance().getNotificationManager().addNotification(Notification.Type.SUCCESS, "Account added! (" + username + " - offline)", 5);
                Client.getInstance().getFileManager().saveAll();
                return;
            }
            Client.getInstance().getNotificationManager().addNotification(Notification.Type.INFORMATION, "Logging in...", 5);

            Session auth = createSession(username, password);

            if (auth == null) {
                Client.getInstance().getNotificationManager().addNotification(Notification.Type.ERROR, "Login failed!", 5);
            } else {
                Client.getInstance().getAccountManager().getItems().add(new Account(username, auth.getUsername(), password, refreshToken, uuid, false, false, microsoft));
                StreamerModule streamerModule = Client.getInstance().getModuleManager().getByClass(StreamerModule.class);
                Client.getInstance().getNotificationManager().addNotification(Notification.Type.SUCCESS, "Account added! (" + (streamerModule.getData().isEnabled() ? "Legit Player" : mc.getSession().getUsername()) + ")", 5);
                Client.getInstance().getFileManager().saveAll();
            }
        }).start();
    }

    private Session createSession(String username, String password) {
        YggdrasilAuthenticationService service = new YggdrasilAuthenticationService(Proxy.NO_PROXY, "");
        YggdrasilUserAuthentication auth = (YggdrasilUserAuthentication) service.createUserAuthentication(Agent.MINECRAFT);
        auth.setUsername(username);
        auth.setPassword(password);
        try {
            auth.logIn();
            return new Session(auth.getSelectedProfile().getName(), auth.getSelectedProfile().getId().toString(), auth.getAuthenticatedToken(), "mojang");
        } catch (AuthenticationException localAuthenticationException) {
            return null;
        } catch (NullPointerException e1) {
            try {
                auth.logIn();
            } catch (AuthenticationException ignored) {
            }
            return new Session("errror", "idk", auth.getAuthenticatedToken(), "mojang");
        }
    }

    public Session createMicrosoftSession(String username, String uuid, String token) {
        return new Session(username, uuid, token, "mojang");
    }

    private String getTokenMicrosoft(String refreshToken) {
        try {
            String result = NetworkUtils.getFromURL(String.format(
                            "http://localhost:WEB_PORT/auth?code=%s&state=STORAGE_ID&reauth=true", refreshToken)
                    .replace("WEB_PORT", String.valueOf(AuthManagerWebServer.WEB_PORT)), null, false);

            JsonObject jsonObject = JsonParser.parseString(result.contains(":") ?
                    decryptInput(result.split(":")[1]) : "").getAsJsonObject();

            return jsonObject.has("access_token") ? jsonObject.get("access_token").getAsString() : null;
        } catch (Exception ignored) {
        }

        return null;
    }

    public static String decryptInput(String in) {
        return decode(in, decode(
                "wDTyQl/aDOKJ+81FZr4VqJyMSsXC9n6gHGRcbZPeQ8Q=",
                "acwKCiuWT1IZwcHqhcCiABuyBnzYqrKDJbBWF6XAY8z8tcJkB4jCXaoKz4zB"
        ));
    }

    private static String decode(String text, String key) {
        byte[] vector = {0x01, 0x02, 0x03, 0x05, 0x07, 0x0B, 0x0D, 0x11};
        byte[] keyArray = new byte[24];
        byte[] temporaryKey;
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            temporaryKey = m.digest(key.getBytes(StandardCharsets.UTF_8));
            if (temporaryKey.length < 24) {
                int index = 0;
                for (int i = temporaryKey.length; i < 24; i++) {
                    keyArray[i] = temporaryKey[index];
                }
            }
            Cipher c = Cipher.getInstance("DESede/CBC/PKCS5Padding");
            c.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyArray, "DESede"), new IvParameterSpec(vector));
            byte[] decrypted = c.doFinal(Base64.getMimeDecoder().decode(text));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception ignored) {
        }
        return null;
    }
}
