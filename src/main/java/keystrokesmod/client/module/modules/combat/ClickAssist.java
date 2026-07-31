package keystrokesmod.client.module.modules.combat;

import java.awt.AWTException;
import java.awt.Robot;

import org.lwjgl.input.Mouse;

import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.value.impl.DescriptionValue;
import keystrokesmod.client.module.value.impl.NumberValue;
import keystrokesmod.client.module.value.impl.BooleanValue;
import keystrokesmod.client.util.Utils;
import keystrokesmod.client.util.input.MouseManager;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@ModuleInfo(name = "ClickAssist", category = Category.Combat)
public class ClickAssist extends Mod {
    private final DescriptionValue desc = new DescriptionValue("Boost your CPS.", this);
    private final NumberValue chance = new NumberValue("Chance", this, 80.0, 0.0, 100.0, 1.0);
    private final BooleanValue L = new BooleanValue("Left click", this, true);
    private final BooleanValue weaponOnly = new BooleanValue("Weapon only", this, true);
    private final BooleanValue onlyWhileTargeting = new BooleanValue("Only while targeting", this, false);
    private final BooleanValue above5 = new BooleanValue("Above 5 cps",this,  false);
    private final BooleanValue R = new BooleanValue("Right click", this, false);
    private final BooleanValue blocksOnly = new BooleanValue("Blocks only", this, true);
    private Robot bot;
    private boolean engagedLeft;
    private boolean engagedRight;
    
    public ClickAssist() {
        this.engagedLeft = false;
        this.engagedRight = false;
    }
    
    public void onEnable() {
        try {
            this.bot = new Robot();
        }
        catch (AWTException var2) {
            this.disable();
        }
    }
    
    public void onDisable() {
        this.engagedLeft = false;
        this.engagedRight = false;
        this.bot = null;
    }
    
    @SubscribeEvent
    public void onMouseUpdate(final MouseEvent ev) {
        if (ev.button >= 0 && ev.buttonstate && chance.getInput() != 0.0 && Utils.Player.isPlayerInGame()) {
            if (mc.currentScreen == null && !mc.thePlayer.isEating() && !mc.thePlayer.isBlocking()) {
                if (ev.button == 0 && L.isToggled()) {
                    if (this.engagedLeft) {
                        this.engagedLeft = false;
                    }
                    else {
                        if (weaponOnly.isToggled() && !Utils.Player.isPlayerHoldingWeapon()) {
                            return;
                        }
                        if (onlyWhileTargeting.isToggled() && (mc.objectMouseOver == null || mc.objectMouseOver.entityHit == null)) {
                            return;
                        }
                        if (chance.getInput() != 100.0) {
                            final double ch = Math.random();
                            if (ch >= chance.getInput() / 100.0) {
                                this.fix(0);
                                return;
                            }
                        }
                        this.bot.mouseRelease(16);
                        this.bot.mousePress(16);
                        this.engagedLeft = true;
                    }
                }
                else if (ev.button == 1 && R.isToggled()) {
                    if (this.engagedRight) {
                        this.engagedRight = false;
                    }
                    else {
                        if (blocksOnly.isToggled()) {
                            final ItemStack item = mc.thePlayer.getHeldItem();
                            if (item == null || !(item.getItem() instanceof ItemBlock)) {
                                this.fix(1);
                                return;
                            }
                        }
                        if (above5.isToggled() && MouseManager.getRightClickCounter() <= 5) {
                            this.fix(1);
                            return;
                        }
                        if (chance.getInput() != 100.0) {
                            final double ch = Math.random();
                            if (ch >= chance.getInput() / 100.0) {
                                this.fix(1);
                                return;
                            }
                        }
                        this.bot.mouseRelease(4);
                        this.bot.mousePress(4);
                        this.engagedRight = true;
                    }
                }
                this.fix(0);
                this.fix(1);
            }
            else {
                this.fix(0);
                this.fix(1);
            }
        }
    }
    
    private void fix(final int t) {
        if (t == 0) {
            if (this.engagedLeft && !Mouse.isButtonDown(0)) {
                this.bot.mouseRelease(16);
            }
        }
        else if (t == 1 && this.engagedRight && !Mouse.isButtonDown(1)) {
            this.bot.mouseRelease(4);
        }
    }
}
