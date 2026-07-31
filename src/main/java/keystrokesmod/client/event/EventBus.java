package keystrokesmod.client.event;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventBus {
    public static final EventBus INSTANCE = new EventBus();

    public static class RegisteredListener {
        private final Object owner;
        private final Listener<Object> listener;
        private final int priority;

        @SuppressWarnings("unchecked")
        public RegisteredListener(Object owner, Listener<?> listener, int priority) {
            this.owner = owner;
            this.listener = (Listener<Object>) listener;
            this.priority = priority;
        }

        public void invoke(Object event) {
            listener.call(event);
        }

        public Object getOwner() {
            return owner;
        }

        public int getPriority() {
            return priority;
        }
    }

    private final Map<Class<?>, List<RegisteredListener>> listenerMap = new ConcurrentHashMap<>();

    public void register(Object parent) {
        if (parent == null) return;
        Class<?> clazz = parent.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(EventLink.class) && Listener.class.isAssignableFrom(field.getType())) {
                EventLink link = field.getAnnotation(EventLink.class);
                field.setAccessible(true);
                try {
                    Listener<?> listener = (Listener<?>) field.get(parent);
                    if (listener == null) continue;

                    Type genericType = field.getGenericType();
                    if (genericType instanceof ParameterizedType) {
                        ParameterizedType pType = (ParameterizedType) genericType;
                        Type[] actualArgs = pType.getActualTypeArguments();
                        if (actualArgs.length > 0 && actualArgs[0] instanceof Class) {
                            Class<?> eventClass = (Class<?>) actualArgs[0];
                            RegisteredListener regListener = new RegisteredListener(parent, listener, link.value());
                            listenerMap.computeIfAbsent(eventClass, k -> new CopyOnWriteArrayList<>()).add(regListener);
                            listenerMap.get(eventClass).sort(Comparator.comparingInt(RegisteredListener::getPriority).reversed());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void unregister(Object parent) {
        if (parent == null) return;
        for (List<RegisteredListener> listeners : listenerMap.values()) {
            listeners.removeIf(l -> l.getOwner().equals(parent));
        }
    }

    public void post(Object event) {
        if (event == null) return;
        List<RegisteredListener> listeners = listenerMap.get(event.getClass());
        if (listeners != null && !listeners.isEmpty()) {
            for (RegisteredListener listener : listeners) {
                listener.invoke(event);
            }
        }
    }
}
