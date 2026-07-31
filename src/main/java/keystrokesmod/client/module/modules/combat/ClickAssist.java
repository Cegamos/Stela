package keystrokesmod.client.module.modules.combat;

import java.awt.AWTException;
import java.awt.Robot;

import org.lwjgl.input.Mouse;

import keystrokesmod.client.event.EventLink;
import keystrokesmod.client.event.Listener;
import keystrokesmod.client.event.impl.MouseEvent;
import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.modules.Mod;
import keystrokesmod.client.module.value.impl.BooleanValue;
import keystrokesmod.client.module.value.impl.DescriptionValue;
import keystrokesmod.client.module.value.impl.NumberValue;
import keystrokesmod.client.util.Utils;
import keystrokesmod.client.util.input.MouseManager;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

@ModuleInfo(name = "ClickAssist", category = Category.Combat)
public class ClickAssist extends Mod {
	protected final DescriptionValue desc = new DescriptionValue("Boost your CPS.", this);
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
    
    @Override
    public void onEnable() {
    	super.onEnable();
        try {
            this.bot = new Robot();
        }
        catch (AWTException var2) {
            this.disable();
        }
    }
    
    @Override
    public void onDisable() {
    	super.onDisable();
        this.engagedLeft = false;
        this.engagedRight = false;
        this.bot = null;
    }
    
    @EventLink
    private Listener<MouseEvent> mouse = event -> {
    	if (event.getButton() >= 0 && event.isButtonstate() && chance.getValue() != 0.0 && Utils.Player.isPlayerInGame()) {
            if (mc.currentScreen == null && !mc.thePlayer.isEating() && !mc.thePlayer.isBlocking()) {
                if (event.getButton() == 0 && L.getValue()) {
                    if (this.engagedLeft) {
                        this.engagedLeft = false;
                    }
                    else {
                        if (weaponOnly.getValue() && !Utils.Player.isPlayerHoldingWeapon()) {
                            return;
                        }
                        if (onlyWhileTargeting.getValue() && (mc.objectMouseOver == null || mc.objectMouseOver.entityHit == null)) {
                            return;
                        }
                        if (chance.getValue() != 100.0) {
                            final double ch = Math.random();
                            if (ch >= chance.getValue() / 100.0) {
                                this.fix(0);
                                return;
                            }
                        }
                        this.bot.mouseRelease(16);
                        this.bot.mousePress(16);
                        this.engagedLeft = true;
                    }
                }
                else if (event.getButton() == 1 && R.getValue()) {
                    if (this.engagedRight) {
                        this.engagedRight = false;
                    }
                    else {
                        if (blocksOnly.getValue()) {
                            final ItemStack item = mc.thePlayer.getHeldItem();
                            if (item == null || !(item.getItem() instanceof ItemBlock)) {
                                this.fix(1);
                                return;
                            }
                        }
                        if (above5.getValue() && MouseManager.getRightClickCounter() <= 5) {
                            this.fix(1);
                            return;
                        }
                        if (chance.getValue() != 100.0) {
                            final double ch = Math.random();
                            if (ch >= chance.getValue() / 100.0) {
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
    };

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
