package keystrokesmod.client.module.modules.combat;

import org.lwjgl.input.Keyboard;

import keystrokesmod.client.event.EventLink;
import keystrokesmod.client.event.Listener;
import keystrokesmod.client.event.impl.AttackEvent;
import keystrokesmod.client.event.impl.PacketReceiveEvent;
import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.setting.impl.ComboSetting;
import keystrokesmod.client.module.setting.impl.SliderSetting;
import keystrokesmod.client.module.setting.impl.TickSetting;
import keystrokesmod.client.utils.system.ReflectUtil;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S12PacketEntityVelocity;

@ModuleInfo(name = "Velocity", category = Category.Combat)
public class Velocity extends Mod {
	
	private final ComboSetting mode = new ComboSetting("Mode", this, Mode.MOTION, Mode.values());
	private final ComboSetting intaveMode = new ComboSetting("Intave Mode", this, () -> mode.is(Mode.INTAVE), IntaveMode.SAFE, IntaveMode.values());
	
    private final SliderSetting horizontal = new SliderSetting("Horizontal", this, 90, 0, 100, 1, () -> mode.is(Mode.MOTION));
    private final SliderSetting vertical = new SliderSetting("Vertical", this, 100, 0, 100, 1, () -> mode.is(Mode.MOTION));
    private final SliderSetting chance = new SliderSetting("Chance", this, 100, 0, 100, 1);
    private final TickSetting d = new TickSetting("Only while targeting", this, false);
    private final TickSetting e = new TickSetting("Disable while holding S", this, false);
    
    @EventLink
    private Listener<PacketReceiveEvent> packetReceive = event -> {
    	final Packet<?> packet = event.getPacket();
    	
        if (mode.is(Mode.MOTION)) {
            if (d.isToggled() && (mc.objectMouseOver == null || mc.objectMouseOver.entityHit == null)) {
                return;
            }
            
            if (e.isToggled() && Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode())) {
            	return;
            }
            
            if (chance.getInput() != 100) {
                final double ch = Math.random();
                if (ch >= chance.getInput() / 100) {
                	return;
                }
            }
        	
            if (packet instanceof S12PacketEntityVelocity) {
                S12PacketEntityVelocity p = (S12PacketEntityVelocity) packet;
                if (p.getEntityID() == mc.thePlayer.getEntityId()) {
                	ReflectUtil.setMotionX(p, (int) (p.getMotionX() * horizontal.getInput() / 100.0));
                	ReflectUtil.setMotionY(p, (int) (p.getMotionY() * vertical.getInput() / 100.0));
                	ReflectUtil.setMotionZ(p, (int) (p.getMotionZ() * horizontal.getInput() / 100.0));
                }
            }
        }
    };
	
	@EventLink
	private Listener<AttackEvent> attackEvent = event -> {
		if (mode.is(Mode.INTAVE)) {
    		
            if (d.isToggled() && (mc.objectMouseOver == null || mc.objectMouseOver.entityHit == null)) {
                return;
            }
            
            if (e.isToggled() && Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode())) {
            	return;
            }
    		
            if (chance.getInput() != 100) {
                final double ch = Math.random();
                if (ch >= chance.getInput() / 100) {
                	return;
                }
            }
    		
    		if (intaveMode.is(IntaveMode.SAFE)) {
                if (event.getTarget() instanceof EntityLivingBase && mc.thePlayer.hurtTime > 0) {
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.motionX *= 0.52;
                        mc.thePlayer.motionZ *= 0.52;
                    } else {
                        mc.thePlayer.motionX *= 0.8;
                        mc.thePlayer.motionZ *= 0.8;
                    }
                }
            } else {
                if (event.getTarget() instanceof EntityLivingBase && mc.thePlayer.hurtTime > 0) {
                    mc.thePlayer.motionX *= 0.6;
                    mc.thePlayer.motionZ *= 0.6;
                }
            }
        }
	};
	
	@Override
	public void update() {
		if (mode.is(Mode.JUMP)) {
			
            if (d.isToggled() && (mc.objectMouseOver == null || mc.objectMouseOver.entityHit == null)) {
                return;
            }
            
            if (e.isToggled() && Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode())) {
            	return;
            }
			
            if (chance.getInput() != 100) {
                final double ch = Math.random();
                if (ch >= chance.getInput() / 100) {
                	return;
                }
            }
			
            if (mc.thePlayer.hurtTime >= 8) {
            	ReflectUtil.setPressed(mc.gameSettings.keyBindJump, true);
            }
            if (mc.thePlayer.hurtTime >= 4) {
                ReflectUtil.setPressed(mc.gameSettings.keyBindJump, false);
            } else if (mc.thePlayer.hurtTime > 1) {
            	ReflectUtil.setPressed(mc.gameSettings.keyBindJump, GameSettings.isKeyDown(mc.gameSettings.keyBindJump));
            }
        }
	}
    
    public enum Mode {
        MOTION, INTAVE, JUMP;
    }

    public enum IntaveMode {
        SAFE, BLATANT;
    }
}
