package keystrokesmod.client.event;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventBus {
    public static final EventBus INSTANCE = new EventBus();
    private final Map<Class<?>, List<Field>> fieldCache = new ConcurrentHashMap<>();
    
    private final Map<Class<?>, List<RegisteredListener>> listenerMap = new ConcurrentHashMap<>();

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

    public void register(Object parent) {
        if (parent == null) return;

        Class<?> clazz = parent.getClass();
        
        List<Field> fields = fieldCache.computeIfAbsent(clazz, this::getEventLinkFields);

        for (Field field : fields) {
            try {
                Listener<?> listener = (Listener<?>) field.get(parent);
                if (listener == null) continue;

                Type genericType = field.getGenericType();
                if (genericType instanceof ParameterizedType) {
                    ParameterizedType pType = (ParameterizedType) genericType;
                    Type[] actualArgs = pType.getActualTypeArguments();
                    
                    if (actualArgs.length > 0 && actualArgs[0] instanceof Class) {
                        Class<?> eventClass = (Class<?>) actualArgs[0];
                        EventLink link = field.getAnnotation(EventLink.class);
                        RegisteredListener regListener = new RegisteredListener(parent, listener, link.value());

                        listenerMap.compute(eventClass, (k, existingListeners) -> {
                            List<RegisteredListener> newList = (existingListeners == null) 
                                ? new ArrayList<>() 
                                : new ArrayList<>(existingListeners);
                            
                            newList.add(regListener);
                            newList.sort(Comparator.comparingInt(RegisteredListener::getPriority).reversed());
                            
                            return new CopyOnWriteArrayList<>(newList);
                        });
                    }
                }
            } catch (Exception e) {
                System.err.println("[EventBus] Failed to register listener in " + clazz.getName() + ": " + e.getMessage());
            }
        }
    }

    public void unregister(Object parent) {
        if (parent == null) return;
        
        for (List<RegisteredListener> listeners : listenerMap.values()) {
            if (listeners != null) {
                listeners.removeIf(l -> l.getOwner().equals(parent));
            }
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

    private List<Field> getEventLinkFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;
        
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (field.isAnnotationPresent(EventLink.class) && Listener.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    fields.add(field);
                }
            }
            current = current.getSuperclass();
        }
        return fields;
    }
}