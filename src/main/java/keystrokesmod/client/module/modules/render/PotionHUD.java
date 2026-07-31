package keystrokesmod.client.module.modules.render;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import keystrokesmod.client.clickgui.raven.ClickGui;
import keystrokesmod.client.clickgui.theme.Theme;
import keystrokesmod.client.event.impl.DragEvent;
import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.util.Utils;
import keystrokesmod.client.utils.animations.ContinualAnimation;
import keystrokesmod.client.utils.font.Fonts;
import keystrokesmod.client.utils.render.RenderUtil;
import keystrokesmod.client.utils.render.RoundedUtil;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@ModuleInfo(name = "PotionHUD", category = Category.Render)
public class PotionHUD extends Mod {

    private final static ContinualAnimation widthAnimation = new ContinualAnimation();
    private final static ContinualAnimation heightAnimation = new ContinualAnimation();

    public static int potionHudX = 12;
    public static int potionHudY = 20;

    private static int maxPotionWidth = 80;
    private static int yOffset = 18;

    private static boolean draggingPotionHud = false;
    private static float dragOffsetX = 0;
    private static float dragOffsetY = 0;

    @SubscribeEvent
    public void a(final TickEvent.RenderTickEvent ev) {
        if (ev.phase != TickEvent.Phase.END || !Utils.Player.isPlayerInGame()) return;
        if (mc.gameSettings.showDebugInfo
        	    || mc.currentScreen instanceof ClickGui
        	    || mc.currentScreen instanceof GuiContainer
        	    || mc.currentScreen instanceof GuiIngameMenu) return;
        
        ArrayList<PotionEffect> potions = new ArrayList<>(mc.thePlayer.getActivePotionEffects());
        potionHudY = Math.max(potionHudY, 10);

        if (!potions.isEmpty()) {
            Optional<PotionEffect> longestNameEffect = potions.stream()
                .max(Comparator.comparingInt(effect -> {
                    Potion potion = Potion.potionTypes[effect.getPotionID()];
                    if (potion == null) return 0;
                    String name = I18n.format(potion.getName());
                    return Fonts.SEMIBOLD.get(16).getStringWidth(name);
                }));

            if (longestNameEffect.isPresent()) {
                PotionEffect effect = longestNameEffect.get();
                Potion potion = Potion.potionTypes[effect.getPotionID()];
                if (potion != null) {
                    String name = I18n.format(potion.getName());
                    int width = Fonts.SEMIBOLD.get(16).getStringWidth(name);
                    maxPotionWidth = (int) MathHelper.clamp_float(width + 20, 80, 999);
                }
            }

            heightAnimation.animate(potions.size() * 13, 18);
        } else {
            heightAnimation.animate(0, 18);
        }

        widthAnimation.animate(maxPotionWidth, 18);

        potions.sort(Comparator.comparingDouble(effect ->
            -Fonts.SEMIBOLD.get(16).getStringWidth(
                I18n.format(Potion.potionTypes[effect.getPotionID()].getName())
            )
        ));

        float totalHeight = 14f + heightAnimation.getOutput();

        RoundedUtil.drawRound(potionHudX, potionHudY, widthAnimation.getOutput() + 12, totalHeight - 8, 4, Theme.getBackColor());

        for (PotionEffect potion : potions) {
            String potionString = I18n.format(Potion.potionTypes[potion.getPotionID()].getName()) +
                    (potion.getAmplifier() > 0 ? " " + I18n.format("enchantment.level." + (potion.getAmplifier() + 1)) : "");

            String durationString = Potion.getDurationString(potion);

            if (Potion.potionTypes[potion.getPotionID()].hasStatusIcon()) {
                GL11.glPushMatrix();
                RenderUtil.resetColor();
                RenderHelper.enableGUIStandardItemLighting();
                int i1 = Potion.potionTypes[potion.getPotionID()].getStatusIconIndex();
                GL11.glScaled(0.5, 0.5, 0.5);
                mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/inventory.png"));
                new Gui().drawTexturedModalRect(
                    (potionHudX + 4) * 9 / 4.5f,
                    (potionHudY + yOffset + 1f + potions.indexOf(potion) * 13) * 9 / 4.5f - 30,
                    i1 % 8 * 18, 198 + i1 / 8 * 18, 18, 18);
                GL11.glScaled(2, 2, 2);
                RenderHelper.disableStandardItemLighting();
                GL11.glPopMatrix();
            }

            Fonts.SEMIBOLD.get(12).drawString(potionString, potionHudX + 15,
                potionHudY + yOffset + potions.indexOf(potion) * 13 - 10, -1);

            Fonts.SEMIBOLD.get(14).drawCenteredString(durationString,
                (potionHudX - 6 + widthAnimation.getOutput() - Fonts.SEMIBOLD.get(16).getStringWidth(durationString)) +
                Fonts.SEMIBOLD.get(16).getStringWidth(durationString) / 2f + 3 + 12,
                potionHudY + yOffset + potions.indexOf(potion) * 13 - 10,
                new Color(0x00BBFF).getRGB());
        }
    }

    @SubscribeEvent
    public void onDrag(DragEvent event) {
    	int mouseX = event.mouseX;
    	int mouseY = event.mouseY;
        boolean mouseDown = Mouse.isButtonDown(0);

        float width = widthAnimation.getOutput();
        float height = heightAnimation.getOutput() + 14 + 18;

        boolean hovering = mouseX >= potionHudX && mouseX <= potionHudX + width &&
                           mouseY >= potionHudY && mouseY <= potionHudY + height;

        if (mouseDown) {
            if (hovering && !draggingPotionHud) {
                draggingPotionHud = true;
                dragOffsetX = mouseX - potionHudX;
                dragOffsetY = mouseY - potionHudY;
            }

            if (draggingPotionHud) {
                potionHudX = (int) (mouseX - dragOffsetX);
                potionHudY = (int) (mouseY - dragOffsetY);
            }
        } else {
            draggingPotionHud = false;
        }
    }
}
