package dev.africa.pandaware.api.module;

import dev.africa.pandaware.Client;
import dev.africa.pandaware.api.event.interfaces.EventListenable;
import dev.africa.pandaware.api.interfaces.MinecraftInstance;
import dev.africa.pandaware.api.interfaces.Toggleable;
import dev.africa.pandaware.api.module.event.TaskedEventListener;
import dev.africa.pandaware.api.module.interfaces.Category;
import dev.africa.pandaware.api.module.interfaces.ModuleInfo;
import dev.africa.pandaware.api.module.mode.ModuleMode;
import dev.africa.pandaware.api.setting.Setting;
import dev.africa.pandaware.impl.module.render.ClickGUIModule;
import dev.africa.pandaware.impl.setting.ModeSetting;
import dev.africa.pandaware.impl.ui.notification.Notification;
import dev.africa.pandaware.utils.client.Printer;
import dev.africa.pandaware.utils.render.animator.Animator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class Module implements EventListenable, Toggleable, MinecraftInstance {
    private ModuleData data;

    private Map<Setting<?>, String> settings;

    private Animator animatorX;
    private Animator animatorY;

    private ModuleMode<?> lastMode;
    private ModuleMode<?> currentMode;
    private ModeSetting modeSetting;

    private TaskedEventListener<?> taskedEvent;

    public Module() {
        if (this.getClass().isAnnotationPresent(ModuleInfo.class)) {
            ModuleInfo moduleInfo = this.getClass().getAnnotation(ModuleInfo.class);


            this.data = new ModuleData(
                    moduleInfo.name(),
                    moduleInfo.shortcut(),
                    moduleInfo.description(),
                    moduleInfo.category(),
                    moduleInfo.key(),
                    false, false
            );

            this.settings = new LinkedHashMap<>();

            this.animatorX = new Animator().setMax(1).setMin(0);
            this.animatorY = new Animator().setMax(1).setMin(0);
        } else {
            Printer.consoleError("Module info not found in " + this.getClass().getName());
            System.exit(1);
        }
    }

    @Override
    public void toggle(boolean enabled) {
        this.data.setEnabled(enabled);

        if (enabled) {
            Client.getInstance().getEventDispatcher().subscribe(this);

            if (this.modeSetting != null && this.modeSetting.getValue() != null) {
                this.currentMode = this.modeSetting.getValue();

                if (this.currentMode != null) {
                    this.lastMode = this.currentMode;
                    Client.getInstance().getEventDispatcher().subscribe(this.currentMode);

                    if (mc.thePlayer != null) {
                        this.currentMode.onEnable();
                    }
                }
            }

            if (mc.thePlayer != null) {
                if (!(this instanceof ClickGUIModule)) {
                    Client.getInstance().getNotificationManager().addNotification(Notification.Type.INFORMATION,
                            "§aEnabled §7" + this.getData().getName(), 2);
                }
                this.onEnable();
            }
        } else {
            Client.getInstance().getEventDispatcher().unsubscribe(this);

            if (this.modeSetting != null && this.modeSetting.getValue() != null) {
                if (this.currentMode != null) {
                    Client.getInstance().getEventDispatcher().unsubscribe(this.currentMode);
                    if (mc.thePlayer != null) {
                        this.currentMode.onDisable();
                    }

                    if (this.lastMode != null) {
                        if (Client.getInstance().getEventDispatcher().unsubscribe(this.lastMode)) {
                            if (mc.thePlayer != null) {
                                this.lastMode.onDisable();
                            }
                        }
                    }
                }
            }

            if (mc.thePlayer != null) {
                if (!(this instanceof ClickGUIModule)) {
                    Client.getInstance().getNotificationManager().addNotification(Notification.Type.INFORMATION,
                            "§4Disabled §7" + this.getData().getName(), 2);
                }
                this.onDisable();
            }
        }
    }

    @Override
    public void toggle() {
        this.toggle(!this.data.isEnabled());
    }

    public String getSuffix() {
        return null;
    }

    public void updateModes() {
        this.lastMode = this.currentMode;
        this.currentMode = this.modeSetting.getValue();

        if (this.getData().isEnabled() && this.lastMode != null && this.lastMode != this.currentMode) {
            Client.getInstance().getEventDispatcher().unsubscribe(this.lastMode);

            Client.getInstance().getEventDispatcher().subscribe(this.currentMode);
        }
    }

    protected void registerSettings(Setting<?>... settings) {
        for (Setting<?> setting : settings) {
            this.settings.put(setting, setting.getName());
        }
    }

    protected void setTaskedEvent(TaskedEventListener<?> taskedEvent) {
        if (this.taskedEvent == null) {
            this.taskedEvent = taskedEvent;

            Client.getInstance().getEventDispatcher().subscribe(taskedEvent);
        }
    }

    protected final void registerModes(ModuleMode<?>... moduleModes) {
        this.modeSetting = new ModeSetting("Mode", Arrays.asList(moduleModes), moduleModes[0]);

        this.currentMode = this.modeSetting.getValue();
        this.lastMode = this.currentMode;

        this.registerSettings(this.modeSetting);
    }


    @Setter
    @Getter
    @AllArgsConstructor
    public static class ModuleData {
        private final String name;
        private final String[] shortcuts;
        private final String description;
        private final Category category;

        private int key;
        private boolean enabled;
        private boolean hidden;
    }
}
