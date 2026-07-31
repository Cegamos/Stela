package keystrokesmod.client.event;

@FunctionalInterface
public interface Listener<T> {
    void call(T event);
}
