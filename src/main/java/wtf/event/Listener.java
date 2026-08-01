package wtf.event;

@FunctionalInterface
public interface Listener<T> {
    void call(T event);
}
