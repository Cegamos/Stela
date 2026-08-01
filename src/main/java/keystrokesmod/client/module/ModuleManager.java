package keystrokesmod.client.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import keystrokesmod.client.module.modules.Mod;
import keystrokesmod.client.module.modules.client.*;
import keystrokesmod.client.module.modules.combat.*;
import keystrokesmod.client.module.modules.macros.*;
import keystrokesmod.client.module.modules.movement.*;
import keystrokesmod.client.module.modules.player.*;
import keystrokesmod.client.module.modules.render.*;
import net.minecraft.client.gui.FontRenderer;

public class ModuleManager {

    private final List<Mod> modules = new ArrayList<>();
    private final Map<Class<? extends Mod>, Mod> moduleClassMap = new HashMap<>(64);
    private final Map<String, Mod> moduleNameMap = new HashMap<>(64);
    private final Map<Category, List<Mod>> categoryMap = new EnumMap<>(Category.class);

    private HUD cachedHUD;
    private boolean initialized = false;

    public ModuleManager() {
        if (initialized) return;

        for (Category category : Category.values()) {
            categoryMap.put(category, new ArrayList<>());
        }

        registerModules(
            new FPSSpoofer(), new GuiModule(), new SelfDestruct(), new Terminal(), new HUD(),
            new AimAssist(), new AutoBlock(), new AutoWeapon(), new BlockHit(),
            new DelayRemover(), new HitBox(), new LeftClicker(), new Reach(), new ShiftTap(),
            new WTap(), new STap(), new Velocity(),
            new Armour(), new Blocks(), new Healing(), new Ladders(), new Pearl(),
            new Trajectories(), new Weapon(),
            new MurderMystery(), new AutoHeader(), new Freeze(), new InvMove(),
            new KeepSprint(), new NoSlow(),new Sprint(),
            new Timer(), 
            new AutoPlay(), new MiddleClick(), new StringEncrypt(),
            new AutoJump(), new AutoTool(), new BedAura(), new Blink(),
            new BridgeAssist(), new FastBreak(), new FastPlace(),
            new RightClicker(),
            new BedPlates(), new Chams(), new ChestESP(), new TimeChanger(), new NameTags(),
            new NameTagsV2(), new Fullbright(), new PlayerESP(), new Tracers(), new Xray()
        );

        this.cachedHUD = (HUD) getModuleByClazz(HUD.class);
        this.initialized = true;
    }

    private void registerModules(Mod... mods) {
        for (Mod mod : mods) {
            this.modules.add(mod);
            this.moduleClassMap.put(mod.getClass(), mod);
            this.moduleNameMap.put(mod.getName().toLowerCase(), mod);

            List<Mod> categoryList = this.categoryMap.get(mod.moduleCategory());
            if (categoryList != null) {
                categoryList.add(mod);
            }
        }
    }

    public Mod getModuleByName(String name) {
        if (!initialized || name == null) return null;
        return moduleNameMap.get(name.toLowerCase());
    }

    @SuppressWarnings("unchecked")
    public <T extends Mod> T getModuleByClazz(Class<T> clazz) {
        if (!initialized || clazz == null) return null;
        return (T) moduleClassMap.get(clazz);
    }

    public List<Mod> getModules() {
        return modules;
    }

    public List<Mod> getModulesInCategory(Category category) {
        List<Mod> list = categoryMap.get(category);
        return list != null ? list : Collections.emptyList();
    }

    private HUD getHUD() {
        if (cachedHUD == null) {
            cachedHUD = getModuleByClazz(HUD.class);
        }
        return cachedHUD;
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

        modules.sort((a, b) -> font.getStringWidth(b.getName()) - font.getStringWidth(a.getName()));
    }

    public void sortShortLong() {
        HUD hud = getHUD();
        FontRenderer font = (hud != null) ? hud.getFont() : null;
        if (font == null) return;

        modules.sort(Comparator.comparingInt(mod -> font.getStringWidth(mod.getName())));
    }

    public int numberOfModules() {
        return modules.size();
    }

    public int getLongestActiveModule() {
        HUD hud = getHUD();
        FontRenderer font = (hud != null) ? hud.getFont() : null;
        if (font == null) return 0;

        int max = 0;
        for (int i = 0; i < modules.size(); i++) {
            Mod mod = modules.get(i);
            if (mod.isEnabled()) {
                int width = font.getStringWidth(mod.getName());
                if (width > max) max = width;
            }
        }
        return max;
    }

    public int getBoxHeight(int margin) {
        HUD hud = getHUD();
        FontRenderer font = (hud != null) ? hud.getFont() : null;
        if (font == null) return 0;

        int count = 0;
        for (int i = 0; i < modules.size(); i++) {
            if (modules.get(i).isEnabled()) {
                count++;
            }
        }
        return count * (font.FONT_HEIGHT + margin);
    }
}