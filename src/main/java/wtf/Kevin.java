package wtf;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import wtf.clickgui.ClickGui;
import wtf.command.CommandManager;
import wtf.config.ClientConfig;
import wtf.config.ConfigManager;
import wtf.event.EventBus;
import wtf.event.EventLink;
import wtf.event.Listener;
import wtf.event.impl.PostTickEvent;
import wtf.handler.*;
import wtf.module.ModuleManager;
import wtf.util.Wrapper;

public class Kevin extends Wrapper {

    public static final String NAME = "Kevin";
    public static final String VERSION = "1.2";

    public static final Kevin INSTANCE = new Kevin();

    public static ModuleManager moduleManager;
    public static CommandManager commandManager;
    public static ConfigManager configManager;
    public static ClientConfig clientConfig;
    public static ClickGui clickGui;

    private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2, r -> {
        Thread thread = new Thread(r, "Kevin-Worker");
        thread.setDaemon(true);
        return thread;
    });

    public static void init() {
        initSystems();
        registerEvents();

        Runtime.getRuntime().addShutdownHook(new Thread(Kevin::shutdown));
    }

    public static void shutdown() {
        try {
            ConfigManager.saveConfigByName(ConfigManager.getCurrentProfileName());
        } catch (Throwable ignored) {}
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }
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
        registerMultiple(
            INSTANCE,
            new CPSHandler(),
            new LagHandler(),
            new PingHandler(),
            new JitterHandler(),
            new ClickHandler(),
            new ScaffoldClickHandler()
        );

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
    
    private static void registerMultiple(Object... handlers) {
        for (Object handler : handlers) {
            EventBus.INSTANCE.register(handler);
        }
    }

    public static ScheduledExecutorService getExecutor() {
        return executor;
    }
}