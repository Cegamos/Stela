package keystrokesmod.client.module;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import keystrokesmod.client.module.modules.Mod;
import keystrokesmod.client.module.modules.client.*;
import keystrokesmod.client.module.modules.combat.*;
import keystrokesmod.client.module.modules.macros.*;
import keystrokesmod.client.module.modules.movement.*;
import keystrokesmod.client.module.modules.other.*;
import keystrokesmod.client.module.modules.player.*;
import keystrokesmod.client.module.modules.render.*;
import net.minecraft.client.gui.FontRenderer;

public class ModuleManager {

    private final List<Mod> modules = new ArrayList<>();
    
    private final Map<Class<? extends Mod>, Mod> moduleClassMap = new ConcurrentHashMap<>();
    private final Map<String, Mod> moduleNameMap = new ConcurrentHashMap<>();
    
    private boolean initialized = false;

    public ModuleManager() {
        if (initialized) return;

        registerModules(
            new FPSSpoofer(), new GuiModule(), new SelfDestruct(), new Terminal(), new HUD(),
            new AimAssist(), new AutoBlock(), new AutoWeapon(), new BlockHit(), new ClickAssist(),
            new DelayRemover(), new HitBox(), new LeftClicker(), new Reach(), new ShiftTap(),
            new WTap(), new STap(), new Velocity(),
            new Armour(), new Blocks(), new Healing(), new Ladders(), new Pearl(),
            new Trajectories(), new Weapon(),
            new MurderMystery(), new AutoHeader(), new Fly(), new Freeze(), new InvMove(),
            new KeepSprint(), new NoSlow(), new Speed(), new Sprint(), new StopMotion(),
            new Timer(), new VClip(),
            new NameHider(), new AutoPlay(), new MiddleClick(), new WaterBucket(), new StringEncrypt(),
            new AutoJump(), new AutoPlace(), new AutoTool(), new BedAura(), new Blink(),
            new BridgeAssist(), new FallSpeed(), new FastBreak(), new FastPlace(), new Freecam(),
            new NoFall(), new RightClicker(),
            new BedPlates(), new Chams(), new ChestESP(), new TimeChanger(), new NameTags(),
            new NameTagsV2(), new Fullbright(), new PlayerESP(), new Tracers(), new Xray()
        );

        this.initialized = true;
    }

    private void registerModules(Mod... mods) {
        for (Mod mod : mods) {
            this.modules.add(mod);
            this.moduleClassMap.put(mod.getClass(), mod);
            this.moduleNameMap.put(mod.getName().toLowerCase(), mod);
        }
    }

    public Mod getModuleByName(String name) {
        if (!initialized || name == null) return null;
        return moduleNameMap.get(name.toLowerCase());
    }

    public Mod getModuleByClazz(Class<? extends Mod> clazz) {
        if (!initialized || clazz == null) return null;
        return moduleClassMap.get(clazz);
    }

    public List<Mod> getModules() {
        return modules;
    }

    public List<Mod> getModulesInCategory(Category category) {
        return modules.stream()
                .filter(mod -> mod.moduleCategory() == category)
                .collect(Collectors.toList());
    }

    private HUD getHUD() {
        return (HUD) getModuleByClazz(HUD.class);
    }

    public void sort() {
        HUD hud = getHUD();
        if (hud == null) return;

        FontRenderer font = hud.getFont();
        if (font == null) {
            modules.sort(Comparator.comparing(Mod::getName));
            return;
        }

        if (hud.alphabeticalSort.getValue()) {
            modules.sort(Comparator.comparing(Mod::getName));
        } else {
            modules.sort(Comparator.comparingInt(mod -> -font.getStringWidth(mod.getName())));
        }
    }

    public void sortLongShort() {
        HUD hud = getHUD();
        FontRenderer font = (hud != null) ? hud.getFont() : null;
        if (font == null) return;

        modules.sort(Comparator.comparingInt(mod -> font.getStringWidth(mod.getName())));
    }

    public void sortShortLong() {
        HUD hud = getHUD();
        FontRenderer font = (hud != null) ? hud.getFont() : null;
        if (font == null) return;

        modules.sort((a, b) -> font.getStringWidth(b.getName()) - font.getStringWidth(a.getName()));
    }

    public int numberOfModules() {
        return modules.size();
    }

    public int getLongestActiveModule() {
        HUD hud = getHUD();
        FontRenderer font = (hud != null) ? hud.getFont() : null;
        if (font == null) return 0;

        return modules.stream()
                .filter(Mod::isEnabled)
                .mapToInt(mod -> font.getStringWidth(mod.getName()))
                .max()
                .orElse(0);
    }

    public int getBoxHeight(int margin) {
        HUD hud = getHUD();
        FontRenderer font = (hud != null) ? hud.getFont() : null;
        if (font == null) return 0;

        return (int) modules.stream()
                .filter(Mod::isEnabled)
                .count() * (font.FONT_HEIGHT + margin);
    }
}