package wtf.stela;

@FunctionalInterface
public interface ClassBytesProvider {
    byte[] getClassBytes(String className) throws Throwable;
}
