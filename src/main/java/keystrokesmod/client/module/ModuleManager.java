package keystrokesmod.client.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import keystrokesmod.client.module.modules.client.*;
import keystrokesmod.client.module.modules.combat.*;
import keystrokesmod.client.module.modules.macros.*;
import keystrokesmod.client.module.modules.movement.*;
import keystrokesmod.client.module.modules.other.*;
import keystrokesmod.client.module.modules.player.*;
import keystrokesmod.client.module.modules.render.*;
import keystrokesmod.client.module.modules.world.*;

public class ModuleManager {

    private final List<Mod> modules = new ArrayList<>();
    public static boolean initialized = false;

    public ModuleManager() {
        if (initialized) return;

        addModules(
            new FPSSpoofer(),
            new GuiModule(),
            new SelfDestruct(),
            new Terminal(),
            new HUD(),
            
            new AimAssist(),
            new AutoBlock(),
            new AutoWeapon(),
            new BlockHit(),
            new ClickAssist(),
            new DelayRemover(),
            new HitBox(),
            new LeftClicker(),
            new Reach(),
            new ShiftTap(),
            new WTap(),
            new STap(),
            new Velocity(),
            
            new Armour(),
            new Blocks(),
            new Healing(),
            new Ladders(),
            new Pearl(),
            new Trajectories(),
            new Weapon(),
            
            new MurderMystery(),            
            new AutoHeader(),
            new Fly(),
            new Freeze(),
            new InvMove(),
            new KeepSprint(),
            new NoSlow(),
            new SlyPort(),
            new Speed(),
            new Sprint(),
            new StopMotion(),
            new Timer(),
            new VClip(),
            
            new NameHider(),
            new AutoPlay(),
            new MiddleClick(),
            new WaterBucket(),
            new StringEncrypt(),
            new FakeChat(),
            
            new AutoJump(),
            new AutoPlace(),
            new AutoTool(),
            new BedAura(),
            new Blink(),
            new BridgeAssist(),
            new FallSpeed(),
            new FastBreak(),
            new FastPlace(),
            new Freecam(),
            new NoFall(),
            new RightClicker(),
            
            new BedPlates(),
            new Chams(),
            new ChestESP(),
            new TimeChanger(),
            new NameTags(),
            new NameTagsV2(),
            new Fullbright(),
            new PotionHUD(),
            new PlayerESP(),
            new Tracers(),
            new Xray(),
            
            new ChatLogger(),
            new AntiBot()
        );

        initialized = true;
    }

    private void addModules(Mod... modules) {
        Collections.addAll(this.modules, modules);
    }

    public Mod getModuleByName(String name) {
        if (!initialized) return null;
        return modules.stream()
                .filter(mod -> mod.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public Mod getModuleByClazz(Class<? extends Mod> clazz) {
        if (!initialized) return null;
        return modules.stream()
                .filter(mod -> mod.getClass().equals(clazz))
                .findFirst()
                .orElse(null);
    }

    public List<Mod> getModules() {
        return modules;
    }

    public List<Mod> getModulesInCategory(Category category) {
        List<Mod> filtered = new ArrayList<>();
        for (Mod mod : modules) {
            if (mod.moduleCategory() == category) {
                filtered.add(mod);
            }
        }
        return filtered;
    }
    
    private HUD getHUD() {
        return (HUD) this.getModuleByClazz(HUD.class);
    }

    public void sort() {
        HUD hud = getHUD();
        if (hud.alphabeticalSort.isToggled()) {
            modules.sort(Comparator.comparing(Mod::getName));
        }

        modules.sort(Comparator.comparingInt(mod -> -hud.getFont().getStringWidth(mod.getName())));
    }

    public void sortLongShort() {
    	HUD hud = getHUD();
        modules.sort(Comparator.comparingInt(mod -> hud.getFont().getStringWidth(mod.getName())));
    }

    public void sortShortLong() {
    	HUD hud = getHUD();
        modules.sort((a, b) -> hud.getFont().getStringWidth(b.getName()) - hud.getFont().getStringWidth(a.getName()));
    }

    public int numberOfModules() {
        return modules.size();
    }
    
    public int getLongestActiveModule() {
    	HUD hud = getHUD();
        int maxLength = 0;
        for (Mod mod : modules) {
            if (mod.isEnabled()) {
                int width = hud.getFont().getStringWidth(mod.getName());
                if (width > maxLength) maxLength = width;
            }
        }
        return maxLength;
    }

    public int getBoxHeight(int margin) {
    	HUD hud = getHUD();
        int height = 0;
        for (Mod mod : modules) {
            if (mod.isEnabled()) {
                height += hud.getFont().getHeight() + margin;
            }
        }
        return height;
    }
}
