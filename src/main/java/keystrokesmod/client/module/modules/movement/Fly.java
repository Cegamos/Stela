package keystrokesmod.client.module.modules.movement;

import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.setting.impl.ComboSetting;
import keystrokesmod.client.module.setting.impl.SliderSetting;

@ModuleInfo(name = "Fly", category = Category.Movement)
public class Fly extends Mod {
    private final ComboSetting mode = new ComboSetting("Mode", this, "Creative", "Creative", "Glide");
    private final SliderSetting b = new SliderSetting("Speed", this, 2.0, 1.0, 5.0, 0.1);

    private boolean opf = false;
    
    @Override
    public void onDisable() {
    	opf = false;
    	
    	if (mode.is("Creative")) {
            if (mc.thePlayer == null) return;
            
            if (mc.thePlayer.capabilities.isFlying) mc.thePlayer.capabilities.isFlying = false;
            
            mc.thePlayer.capabilities.setFlySpeed(0.05f);
    	}
    }

    @Override
    public void update() {
    	switch (mode.getMode()) {
    	case "Creative":
            mc.thePlayer.motionY = 0.0;
            mc.thePlayer.capabilities.setFlySpeed((float)(0.05000000074505806 * b.getInput()));
            mc.thePlayer.capabilities.isFlying = true;
    		break;
    	case "Glide":
            if (mc.thePlayer.movementInput.moveForward > 0.0f) {
                if (!this.opf) {
                    this.opf = true;
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.jump();
                    }
                }
                else {
                    if (mc.thePlayer.onGround || mc.thePlayer.isCollidedHorizontally) {
                        this.disable();
                        return;
                    }
                    final double s = 1.94 * b.getInput();
                    final double r = Math.toRadians(mc.thePlayer.rotationYaw + 90.0f);
                    mc.thePlayer.motionX = s * Math.cos(r);
                    mc.thePlayer.motionZ = s * Math.sin(r);
                }
            }
    		break;
    	}
    }
}
