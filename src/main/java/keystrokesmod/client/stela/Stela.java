package keystrokesmod.client.stela;

public class Stela {
    public static ClassProvider classProvider;
    public static Logger Logger;

    public static void init(ClassProvider provider, Logger logger) {
        classProvider = provider;
        Logger = logger;
    }
}
