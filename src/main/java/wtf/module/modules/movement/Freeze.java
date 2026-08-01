package wtf.module.modules.movement;

import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import wtf.event.EventLink;
import wtf.event.Listener;
import wtf.event.impl.PacketReceiveEvent;
import wtf.event.impl.PacketSendEvent;
import wtf.module.Category;
import wtf.module.ModuleInfo;
import wtf.module.modules.Mod;

@ModuleInfo(name = "Freeze", category = Category.Movement)
public class Freeze extends Mod {
    public double motionX = 0.0, motionY = 0.0, motionZ = 0.0;
    public double x = 0.0, y = 0.0, z = 0.0;
    
    @Override
    public void onEnable() {
        if (mc.thePlayer == null) return;
        x = mc.thePlayer.posX;
        y = mc.thePlayer.posY;
        z = mc.thePlayer.posZ;
        motionX = mc.thePlayer.motionX;
        motionY = mc.thePlayer.motionY;
        motionZ = mc.thePlayer.motionZ;
    }
    
    @Override
    public void onDisable() {
    	if (mc.thePlayer == null) return;
        mc.thePlayer.motionX = motionX;
        mc.thePlayer.motionY = motionY;
        mc.thePlayer.motionZ = motionZ;
        mc.thePlayer.setPositionAndRotation(x, y, z, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
    }

    @Override
    public void update() {
        mc.thePlayer.motionX = 0.0;
        mc.thePlayer.motionY = 0.0;
        mc.thePlayer.motionZ = 0.0;
        mc.thePlayer.setPositionAndRotation(x, y, z, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
    }
    
    @EventLink
    private Listener<PacketSendEvent> packetSend = event -> {
    	if (event.getPacket() instanceof C03PacketPlayer) event.cancel();
    };
    
    @EventLink
    private Listener<PacketReceiveEvent> packetReceive = event -> {
        if (event.getPacket() instanceof S08PacketPlayerPosLook) {
            S08PacketPlayerPosLook p = (S08PacketPlayerPosLook) event.getPacket();
            x = p.getX();
            y = p.getY();
            z = p.getZ();
            motionX = 0.0;
            motionY = 0.0;
            motionZ = 0.0;
        }
    };
}
