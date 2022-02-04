package dev.africa.pandaware.manager.module;

import dev.africa.pandaware.Client;
import dev.africa.pandaware.api.interfaces.Initializable;
import dev.africa.pandaware.api.module.Module;
import dev.africa.pandaware.impl.container.MapContainer;
import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventListenable;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.module.interfaces.Category;
import dev.africa.pandaware.impl.event.game.GameLoopEvent;
import dev.africa.pandaware.impl.event.game.KeyEvent;
import dev.africa.pandaware.impl.module.combat.KillAuraModule;
import dev.africa.pandaware.impl.module.combat.TPAuraModule;
import dev.africa.pandaware.impl.module.combat.antibot.AntiBotModule;
import dev.africa.pandaware.impl.module.combat.criticals.CriticalsModule;
import dev.africa.pandaware.impl.module.combat.velocity.VelocityModule;
import dev.africa.pandaware.impl.module.misc.disabler.DisablerModule;
import dev.africa.pandaware.impl.module.misc.MiddleClickFriendModule;
import dev.africa.pandaware.impl.module.movement.InventoryMoveModule;
import dev.africa.pandaware.impl.module.movement.ScaffoldModule;
import dev.africa.pandaware.impl.module.movement.SprintModule;
import dev.africa.pandaware.impl.module.movement.TargetStrafeModule;
import dev.africa.pandaware.impl.module.movement.flight.FlightModule;
import dev.africa.pandaware.impl.module.movement.longjump.LongJumpModule;
import dev.africa.pandaware.impl.module.movement.speed.SpeedModule;
import dev.africa.pandaware.impl.module.player.AutoArmorModule;
import dev.africa.pandaware.impl.module.player.ChestStealerModule;
import dev.africa.pandaware.impl.module.player.InventoryManagerModule;
import dev.africa.pandaware.impl.module.player.NoRotateModule;
import dev.africa.pandaware.impl.module.player.nofall.NoFallModule;
import dev.africa.pandaware.impl.module.render.*;
import lombok.Getter;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Getter
public class ModuleManager extends MapContainer<Module, String> implements Initializable, EventListenable {

    @Override
    public void init() {
        this.addModules(
                // COMBAT
                new AntiBotModule(),
                new VelocityModule(),
                new CriticalsModule(),
                new KillAuraModule(),
                new TPAuraModule(),

                // Movement
                new SprintModule(),
                new FlightModule(),
                new SpeedModule(),
                new LongJumpModule(),
                new ScaffoldModule(),
                new InventoryMoveModule(),
                new TargetStrafeModule(),

                // Visual
                new AnimationsModule(),
                new HUDModule(),
                new ClickGUIModule(),
                new ESPModule(),
                new TargetHudModule(),
                new ChamsModule(),
                new DeathEffectModule(),

                // Player
                new NoFallModule(),
                new NoRotateModule(),
                new ChestStealerModule(),
                new InventoryManagerModule(),
                new AutoArmorModule(),

                // Misc
                new DisablerModule(),
                new MiddleClickFriendModule()
        );

        Client.getInstance().getEventDispatcher().subscribe(this);
    }

    private void addModules(Module... modules) {
        for (Module module : modules) {
            this.getMap().put(module, module.getData().getName().replace(" ", ""));
        }
    }

    public List<Module> getInCategory(Category category) {
        return this.getMap().keySet().stream().filter(module -> module.getData().getCategory() == category)
                .collect(Collectors.toList());
    }

    public <T extends Module> T getByClass(Class<? extends Module> clazz) {
        AtomicReference<T> atomicModule = new AtomicReference<>(null);

        this.getMap().keySet().forEach(module -> {
            if (module.getClass() == clazz) {
                atomicModule.set((T) module);
            }
        });

        return atomicModule.get();
    }

    public Module getByName(String name) {
        AtomicReference<Module> atomicModule = new AtomicReference<>(null);

        this.getMap().keySet().forEach(module -> {
            if (module.getData().getName().equalsIgnoreCase(name)) {
                atomicModule.set(module);
            }
        });

        return atomicModule.get();
    }

    @EventHandler
    EventCallback<GameLoopEvent> onGameLoop = event -> this.getMap().keySet()
            .stream()
            .filter(module -> module.getModeSetting() != null
                    && module.getModeSetting().getValue() != null
                    && module.getCurrentMode() != null
            ).forEach(Module::updateModes);

    @EventHandler
    EventCallback<KeyEvent> onKey = event -> this.getMap().keySet()
            .stream()
            .filter(module -> module.getData().getKey() == event.getKey())
            .forEach(Module::toggle);
}
