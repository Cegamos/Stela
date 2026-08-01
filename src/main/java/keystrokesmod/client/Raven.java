package keystrokesmod.client;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import keystrokesmod.client.clickgui.ClickGui;
import keystrokesmod.client.command.CommandManager;
import keystrokesmod.client.config.ClientConfig;
import keystrokesmod.client.config.ConfigManager;
import keystrokesmod.client.event.EventBus;
import keystrokesmod.client.event.EventLink;
import keystrokesmod.client.event.Listener;
import keystrokesmod.client.event.impl.PostTickEvent;
import keystrokesmod.client.module.ModuleManager;
import keystrokesmod.client.util.Wrapper;
import keystrokesmod.client.util.player.ClickHandler;
import keystrokesmod.client.util.player.JitterHandler;
import keystrokesmod.client.util.player.ScaffoldClickHandler;
import keystrokesmod.client.util.system.CPSTracker;
import keystrokesmod.client.util.system.LagTracker;
import keystrokesmod.client.util.system.PingTracker;
import keystrokesmod.keystroke.KeyStrokeCommand;
import keystrokesmod.keystroke.KeyStrokeRenderer;
import net.minecraftforge.client.ClientCommandHandler;

public class Raven extends Wrapper {

    public static final String NAME = "Raven b+";
    public static final String VERSION = "1.0.27";

    public static final Raven INSTANCE = new Raven();

    public static ModuleManager moduleManager;
    public static CommandManager commandManager;
    public static ConfigManager configManager;
    public static ClientConfig clientConfig;
    public static ClickGui clickGui;

    private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

    public static void init() {
        initSystems();
        registerEvents();

        Runtime.getRuntime().addShutdownHook(new Thread(executor::shutdown));
    }

    private static void initSystems() {
        moduleManager = new ModuleManager();
        commandManager = new CommandManager();
        clickGui = new ClickGui();
        configManager = new ConfigManager();
        clientConfig = new ClientConfig();

        clientConfig.applyConfig();
    }

    private static void registerEvents() {
        KeyStrokeRenderer keyStrokeRenderer = new KeyStrokeRenderer();

        EventBus.INSTANCE.register(INSTANCE);
        EventBus.INSTANCE.register(keyStrokeRenderer);
        EventBus.INSTANCE.register(new CPSTracker());
        EventBus.INSTANCE.register(new LagTracker());
        EventBus.INSTANCE.register(new PingTracker());
        EventBus.INSTANCE.register(new JitterHandler());
        EventBus.INSTANCE.register(new ClickHandler());
        EventBus.INSTANCE.register(new ScaffoldClickHandler());

        ClientCommandHandler.instance.registerCommand(new KeyStrokeCommand());
    }


    @EventLink
    public final Listener<PostTickEvent> onTick = event -> {
        if (checkGame()) return;

        moduleManager.getModules().forEach(module -> {
            if (currentScreen() == null) {
                module.keybind();
            } else if (currentScreen() instanceof ClickGui) {
                module.guiUpdate();
            }

            if (module.isEnabled()) {
                module.update();
            }
        });
    };

    public static ScheduledExecutorService getExecutor() {
        return executor;
    }
}