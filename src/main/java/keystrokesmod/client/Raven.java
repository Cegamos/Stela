package keystrokesmod.client;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import keystrokesmod.client.clickgui.raven.ClickGui;
import keystrokesmod.client.command.CommandManager;
import keystrokesmod.client.config.ClientConfig;
import keystrokesmod.client.config.ConfigManager;
import keystrokesmod.client.event.EventBus;
import keystrokesmod.client.event.EventLink;
import keystrokesmod.client.event.Listener;
import keystrokesmod.client.event.impl.PostTickEvent;
import keystrokesmod.client.module.ModuleManager;
import keystrokesmod.client.util.Utils;
import keystrokesmod.client.util.input.MouseManager;
import keystrokesmod.keystroke.KeyStrokeCommand;
import keystrokesmod.keystroke.KeyStrokeRenderer;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientCommandHandler;

public class Raven {

    public static final String NAME = "Raven b+";
    public static final String VERSION = "1.0.26";
    public static boolean debugger = false;

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
        MouseManager mouseManager = new MouseManager();
        KeyStrokeRenderer keyStrokeRenderer = new KeyStrokeRenderer();

        EventBus.INSTANCE.register(INSTANCE);
        EventBus.INSTANCE.register(mouseManager);
        EventBus.INSTANCE.register(keyStrokeRenderer);

        ClientCommandHandler.instance.registerCommand(new KeyStrokeCommand());
    }


    @EventLink
    public final Listener<PostTickEvent> onTick = event -> {
        if (!Utils.Player.isPlayerInGame()) {
            return;
        }

        final Minecraft mc = Minecraft.getMinecraft();
        final boolean inGame = mc.currentScreen == null;
        final boolean inGui = mc.currentScreen instanceof ClickGui;

        moduleManager.getModules().forEach(module -> {
            if (inGame) {
                module.keybind();
            } else if (inGui) {
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