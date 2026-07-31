package keystrokesmod.client.stela;

@FunctionalInterface
public interface ClassProvider {
    Class<?> get(String name) throws ClassNotFoundException;
}
