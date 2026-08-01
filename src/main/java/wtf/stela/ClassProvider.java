package wtf.stela;

@FunctionalInterface
public interface ClassProvider {
    Class<?> get(String name) throws ClassNotFoundException;
}
