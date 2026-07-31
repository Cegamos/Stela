package keystrokesmod.client.util.system;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import io.netty.util.concurrent.GenericFutureListener;
import keystrokesmod.client.util.IMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.shader.Shader;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Session;
import net.minecraft.util.Timer;
import net.minecraft.util.Vec3;

@SuppressWarnings({"unchecked", "rawtypes"})
public class ReflectUtil implements IMinecraft {
    public static final boolean hasOptifine = Arrays.stream(GameSettings.class.getFields()).anyMatch(f -> f.getName().equals("ofFastRender"));

    private static final ConcurrentMap<String, Handle> fieldsHandles = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Handle> methodsHandles = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Constructor> constructors = new ConcurrentHashMap<>();
    private static final ConcurrentMap<Constructor, MethodHandle> constructorsHandles = new ConcurrentHashMap<>();

    private static final MethodHandles.Lookup LOOKUP;

    static {
        try {
            Field implLookup = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            implLookup.setAccessible(true);
            LOOKUP = (MethodHandles.Lookup) implLookup.get(null);
        } catch (Exception e) {
            throw new RuntimeException("[ReflectUtil] Falla crítica inicializando IMPL_LOOKUP", e);
        }
    }

    private static final class Handle {
        final MethodHandle handle;
        final boolean isStatic;

        Handle(MethodHandle handle, boolean isStatic) {
            this.handle = handle;
            this.isStatic = isStatic;
        }
    }

    public static void setBlockHitDelay(int val) { setPrivateField(PlayerControllerMP.class, mc.playerController, val, "blockHitDelay", "field_78781_i"); }
    public static void setRightClickDelayTimer(int val) { setPrivateField(Minecraft.class, mc, val, "rightClickDelayTimer", "field_71467_ac"); }
    public static void setLeftClickCounter(int val) { setPrivateField(Minecraft.class, mc, val, "leftClickCounter", "field_71429_W"); }
    public static void setRightClickCounter(int val) { setPrivateField(Minecraft.class, mc, val, "rightClickCounter", "field_71429_W"); }
    public static void setJumpTicks(int val) { setPrivateField(EntityLivingBase.class, mc.thePlayer, val, "jumpTicks", "field_70773_bE"); }
    public static void setCurBlockDamage(float val) { setPrivateField(PlayerControllerMP.class, mc.playerController, val, "curBlockDamageMP", "field_78770_f"); }
    public static boolean isInWeb() { return getPrivateField(Entity.class, mc.thePlayer, "isInWeb", "field_70134_J"); }
    public static void setInWeb(boolean bool) { setPrivateField(Entity.class, mc.thePlayer, bool, "isInWeb", "field_70134_J"); }
    public static boolean isHittingBlock() { return getPrivateField(PlayerControllerMP.class, mc.playerController, "isHittingBlock", "field_78778_j"); }
    public static int getBlockHitDelay() { return getPrivateField(PlayerControllerMP.class, mc.playerController, "blockHitDelay", "field_78781_i"); }
    public static void setFpsCounter(int val) { setPrivateField(Minecraft.class, mc, val, "fpsCounter", "field_71420_M"); }
    public static float getCurBlockDamage() { return getPrivateField(PlayerControllerMP.class, mc.playerController, "curBlockDamageMP", "field_78770_f"); }
    public static float getLastReportedYaw() { return getPrivateField(EntityPlayerSP.class, mc.thePlayer, "lastReportedYaw", "field_175164_bL"); }
    public static float getLastReportedPitch() { return getPrivateField(EntityPlayerSP.class, mc.thePlayer, "lastReportedPitch", "field_175165_bM"); }
    
    public static int getMotionX(S12PacketEntityVelocity packet) { return getPrivateField(S12PacketEntityVelocity.class, packet, "motionX", "field_149415_b"); }
    public static int getMotionY(S12PacketEntityVelocity packet) { return getPrivateField(S12PacketEntityVelocity.class, packet, "motionY", "field_149416_c"); }
    public static int getMotionZ(S12PacketEntityVelocity packet) { return getPrivateField(S12PacketEntityVelocity.class, packet, "motionZ", "field_149414_d"); }
    public static void setMotionX(S12PacketEntityVelocity packet, int val) { setPrivateField(S12PacketEntityVelocity.class, packet, val, "motionX", "field_149415_b"); }
    public static void setMotionY(S12PacketEntityVelocity packet, int val) { setPrivateField(S12PacketEntityVelocity.class, packet, val, "motionY", "field_149416_c"); }
    public static void setMotionZ(S12PacketEntityVelocity packet, int val) { setPrivateField(S12PacketEntityVelocity.class, packet, val, "motionZ", "field_149414_d"); }
    
    public static void setServerSprintState(boolean bool) { setPrivateField(EntityPlayerSP.class, mc.thePlayer, bool, "serverSprintState", "field_175171_bO"); }
    public static boolean isServerSprintState() { return getPrivateField(EntityPlayerSP.class, mc.thePlayer, "serverSprintState", "field_175171_bO"); }
    public static IInventory isLowerChestInventory() { return getPrivateField(GuiChest.class, ((GuiChest) mc.currentScreen), "lowerChestInventory", "field_147015_w"); }
    public static void setYawC03(C03PacketPlayer packet, float flot) { setPrivateField(C03PacketPlayer.class, packet, flot, "yaw", "field_149476_e"); }
    public static void setPitchC03(C03PacketPlayer packet, float flot) { setPrivateField(C03PacketPlayer.class, packet, flot, "pitch", "field_149473_f"); }
    public static void setRotatingC03(C03PacketPlayer packet, boolean bool) { setPrivateField(C03PacketPlayer.class, packet, bool, "rotating", "field_149481_i"); }
    
    public static void setItemInUse(int block) { setPrivateField(EntityPlayer.class, mc.thePlayer, block, "itemInUseCount", "field_71072_f"); }
    public static boolean setItemInUse(boolean blocking) { setPrivateField(EntityPlayer.class, mc.thePlayer, blocking ? 1 : 0, "itemInUseCount", "field_71072_f"); return blocking; }
    
    public static Vec3 getVectorForRotation(float pitch, float yaw) { return (Vec3) getPrivateMethod(Entity.class, mc.thePlayer, float.class, float.class, pitch, yaw, "getVectorForRotation", "func_174806_f"); }
    public static void setSession(Session obj) { setPrivateField(Minecraft.class, mc, obj, "session", "field_178752_a"); }
    
    public static void clickMouse() { getPrivateMethod(Minecraft.class, mc, "func_147116_af", "clickMouse"); }
    public static void mouseClicked(int mouseX, int mouseY, int button) { getPrivateMethod(GuiScreen.class, mc.currentScreen, int.class, int.class, int.class, mouseX, mouseY, button, "mouseClicked", "func_146192_a"); }
    public static void rightClickMouse() { getPrivateMethod(Minecraft.class, mc, "func_147121_ag", "rightClickMouse"); }
    
    public static void setPressTime(KeyBinding key, int value) { setPrivateField(KeyBinding.class, key, value, "pressTime", "field_151474_i"); }
    public static void setPressed(KeyBinding key, boolean bool) { setPrivateField(KeyBinding.class, key, bool, "pressed", "field_74513_e"); }
    public static boolean isPressed(KeyBinding key) { return getPrivateField(KeyBinding.class, key, "pressed", "field_74513_e"); }
    
    public static void loadShader(ResourceLocation shader) { getPrivateMethod(EntityRenderer.class, mc.entityRenderer, ResourceLocation.class, shader, "func_175069_a", "loadShader"); }
    public static Timer getTimer() { return getPrivateField(Minecraft.class, mc, "timer", "field_71428_T"); }
    public static void resetTimer() { getTimer().timerSpeed = 1.0f; }
    
    public static double getRenderPosX() { return getPrivateField(RenderManager.class, mc.getRenderManager(), "renderPosX", "field_78725_b"); }
    public static double getRenderPosY() { return getPrivateField(RenderManager.class, mc.getRenderManager(), "renderPosY", "field_78726_c"); }
    public static double getRenderPosZ() { return getPrivateField(RenderManager.class, mc.getRenderManager(), "renderPosZ", "field_78723_d"); }
    
    public static void orientCamera(float flot) { getPrivateMethod(EntityRenderer.class, mc.entityRenderer, flot, "orientCamera", "func_78467_g"); }
    public static ShaderGroup isTheShaderGroup() { return getPrivateField(EntityRenderer.class, mc.entityRenderer, "theShaderGroup", "field_147707_d"); }
    public static void setTheShaderGroup(ShaderGroup shaderGroup) { setPrivateField(EntityRenderer.class, mc.entityRenderer, shaderGroup,"theShaderGroup", "field_147707_d"); }
    public static List<Shader> getListShaders(ShaderGroup shaderGroup) { return getPrivateField(ShaderGroup.class, shaderGroup, "listShaders", "field_148031_d"); }
    
    public static void flushOutboundQueue() { getPrivateMethod(NetworkManager.class, mc.getNetHandler().getNetworkManager(), "flushOutboundQueue", "func_150733_h"); }
    public static void dispatchPacket(Packet packet, GenericFutureListener[] listeners) { getPrivateMethod(NetworkManager.class, mc.getNetHandler().getNetworkManager(), Packet.class, GenericFutureListener[].class, packet, listeners, "dispatchPacket", "func_150732_b"); }
    public static ReentrantReadWriteLock readWriteLock() { return getPrivateField(NetworkManager.class, mc.getNetHandler().getNetworkManager(),"readWriteLock", "field_181680_j"); }
    public static Queue<Object> outboundPacketsQueue() { return getPrivateField(NetworkManager.class, mc.getNetHandler().getNetworkManager(), "outboundPacketsQueue", "field_150745_j"); }

    public static Object InboundHandlerTuplePacketListener(Packet packet) {
        Constructor constructor = getPrivateConstructor(getPrivateClass(NetworkManager.class, "InboundHandlerTuplePacketListener"), Packet.class, GenericFutureListener[].class);
        return newInstance(constructor, packet, null);
    }

    public static boolean isShaders() {
        try {
            Class configClass = Class.forName("Config");
            return (boolean) configClass.getMethod("isShaders").invoke(null);
        } catch (Exception e) {
            return false;
        }
    }

    public static void setGameSetting(Minecraft mc, String fieldName, boolean value) {
        try {
            setPrivateField(GameSettings.class, mc.gameSettings, value, fieldName);
            return;
        } catch (Exception ignored) {}

        try {
            Class configClass = Class.forName("Config");
            Field field = configClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.setBoolean(null, value);
        } catch (Exception e) {}
    }

    public static <T> Object getPrivateMethod(Class<? super T> classToAccess, T instance, Object... values) {
        try {
            int stringIndex = -1;
            for (int i = 0; i < values.length; i++) {
                if (values[i] instanceof String) {
                    stringIndex = i;
                    break;
                }
            }
            if (stringIndex == -1 || stringIndex % 2 != 0) {
                throw new IllegalArgumentException("Invalid method call parameters.");
            }

            int paramCount = stringIndex / 2;
            Class[] paramTypes = new Class[paramCount];
            Object[] args = new Object[paramCount];
            for (int i = 0; i < paramCount; i++) {
                paramTypes[i] = (Class) values[i];
                args[i] = values[i + paramCount];
            }

            String[] methodNames = Arrays.copyOfRange(values, stringIndex, values.length, String[].class);

            for (String name : methodNames) {
                Handle entry = findMethod(classToAccess, name, paramTypes);
                if (entry == null) continue;

                try {
                    Object[] callArgs;
                    if (entry.isStatic) {
                        callArgs = args;
                    } else {
                        callArgs = new Object[args.length + 1];
                        callArgs[0] = instance;
                        System.arraycopy(args, 0, callArgs, 1, args.length);
                    }
                    return entry.handle.invokeWithArguments(callArgs);
                } catch (Throwable ignored) {}
            }
        } catch (Exception ignored) {}
        return null;
    }

    public static <T> T getPrivateField(Class clazz, Object instance, String... fieldNames) {
        for (String name : fieldNames) {
            Handle entry = findField(clazz, name, false);
            if (entry == null) continue;
            try {
                return (T) (entry.isStatic ? entry.handle.invoke() : entry.handle.invoke(instance));
            } catch (Throwable ignored) {}
        }
        return null;
    }

    public static <T> void setPrivateField(Class<? super T> classToAccess, T instance, Object value, String... fieldNames) {
        for (String name : fieldNames) {
            Handle entry = findField(classToAccess, name, true);
            if (entry == null) continue;
            try {
                if (entry.isStatic) {
                    entry.handle.invoke(value);
                } else {
                    entry.handle.invoke(instance, value);
                }
                return;
            } catch (Throwable ignored) {}
        }
    }

    public static Class getPrivateClass(Class parentClass, String... innerClassSimpleNames) {
        for (String simpleName : innerClassSimpleNames) {
            for (Class innerClass : parentClass.getDeclaredClasses()) {
                if (innerClass.getSimpleName().equals(simpleName)) return innerClass;
            }
        }
        throw new RuntimeException("No matching inner class found");
    }

    public static Constructor getPrivateConstructor(Class clazz, Class... parameterTypes) {
        String key = clazz.getName() + Arrays.toString(parameterTypes);
        Constructor cached = constructors.get(key);
        if (cached != null) return cached;

        try {
            Constructor constructor = clazz.getDeclaredConstructor(parameterTypes);
            MethodHandle handle = LOOKUP.unreflectConstructor(constructor);
            constructorsHandles.put(constructor, handle);
            constructors.put(key, constructor);
            return constructor;
        } catch (Exception e) {
            return null;
        }
    }

    public static Object newInstance(Constructor constructor, Object... args) {
        MethodHandle handle = constructorsHandles.get(constructor);
        try {
            if (handle != null) return handle.invokeWithArguments(args);
            constructor.setAccessible(true);
            return constructor.newInstance(args);
        } catch (Throwable e) {
            return null;
        }
    }

    private static Handle findField(Class clazz, String name, boolean isSetter) {
        String key = clazz.getName() + "#" + name + (isSetter ? "=s" : "=g");
        Handle cached = fieldsHandles.get(key);
        if (cached != null) return cached;

        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            MethodHandle handle = isSetter ? LOOKUP.unreflectSetter(field) : LOOKUP.unreflectGetter(field);
            Handle entry = new Handle(handle, Modifier.isStatic(field.getModifiers()));
            fieldsHandles.put(key, entry);
            return entry;
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    private static Handle findMethod(Class clazz, String name, Class[] paramTypes) {
        String key = clazz.getName() + "#" + name + Arrays.toString(paramTypes);
        Handle cached = methodsHandles.get(key);
        if (cached != null) return cached;

        try {
            Method method = clazz.getDeclaredMethod(name, paramTypes);
            method.setAccessible(true);
            MethodHandle handle = LOOKUP.unreflect(method);
            Handle entry = new Handle(handle, Modifier.isStatic(method.getModifiers()));
            methodsHandles.put(key, entry);
            return entry;
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }
}