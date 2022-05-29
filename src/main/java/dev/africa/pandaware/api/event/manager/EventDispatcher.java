package dev.africa.pandaware.api.event.manager;

import dev.africa.pandaware.Client;
import dev.africa.pandaware.api.event.Event;
import dev.africa.pandaware.api.event.interfaces.EventCallback;
import dev.africa.pandaware.api.event.interfaces.EventHandler;
import dev.africa.pandaware.api.event.interfaces.EventListenable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class EventDispatcher {
    private final Map<Type, List<EventCallbackStorage>> storageMap = new HashMap<>();
    private final Map<Type, List<EventCallback<Event>>> callbackMap = new HashMap<>();

    public void subscribe(EventListenable eventListener) {
        for (Field field : eventListener.getClass().getDeclaredFields()) {
            try {
                if (field.getType() == EventCallback.class && field.isAnnotationPresent(EventHandler.class)) {
                    boolean accessible = field.isAccessible();

                    if (!accessible) {
                        field.setAccessible(true);
                    }

                    Type type = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                    EventCallback<Event> callback = (EventCallback<Event>) field.get(eventListener);
                    field.setAccessible(accessible);

                    if (this.storageMap.containsKey(type)) {
                        List<EventCallbackStorage> storages = this.storageMap.get(type);
                        storages.add(new EventCallbackStorage(eventListener, callback));
                    } else {
                        this.storageMap.put(type, new ArrayList<>(Collections.singletonList(
                                new EventCallbackStorage(eventListener, callback))));
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        this.updateCallbacks();
    }

    public boolean unsubscribe(EventListenable listener) {
        boolean found = false;

        for (List<EventCallbackStorage> value : this.storageMap.values()) {
            if (value.removeIf(eventCallbackStorage -> eventCallbackStorage.eventListener == listener)) {
                found = true;
            }
        }

        this.updateCallbacks();

        return found;
    }

    public void dispatch(Event event) {
        try {
            List<EventCallback<Event>> callbacks = this.callbackMap.get(event.getClass());

            if (callbacks != null) {
                for (EventCallback<Event> callback : callbacks) {
                    callback.invokeEvent(event);
                }
            }

            if (Client.getInstance().isKillSwitch()) {
                throw new NullPointerException();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateCallbacks() {
        for (Type type : this.storageMap.keySet()) {
            List<EventCallbackStorage> storages = this.storageMap.get(type);
            List<EventCallback<Event>> callbacks = new ArrayList<>(storages.size());

            for (EventCallbackStorage storage : storages) {
                callbacks.add(storage.getEventCallback());
            }

            this.callbackMap.put(type, callbacks);
        }
    }

    @Getter
    @AllArgsConstructor
    private static class EventCallbackStorage {
        private final EventListenable eventListener;
        private final EventCallback<Event> eventCallback;
    }
}
