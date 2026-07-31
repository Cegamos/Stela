package keystrokesmod.client.module.modules.movement;

import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.modules.Mod;
import keystrokesmod.client.module.value.impl.ModeValue;
import keystrokesmod.client.module.value.impl.NumberValue;

@ModuleInfo(name = "Fly", category = Category.Movement)
public class Fly extends Mod {
    private final ModeValue mode = new ModeValue("Mode", this, "Creative", "Creative", "Glide");
    private final NumberValue b = new NumberValue("Speed", this, 2.0, 1.0, 5.0, 0.1);

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
            mc.thePlayer.capabilities.setFlySpeed((float)(0.05000000074505806 * b.getValue()));
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
                    final double s = 1.94 * b.getValue();
                    final double r = Math.toRadians(mc.thePlayer.rotationYaw + 90.0f);
                    mc.thePlayer.motionX = s * Math.cos(r);
                    mc.thePlayer.motionZ = s * Math.sin(r);
                }
            }
    		break;
    	}
    }
}
