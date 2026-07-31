package keystrokesmod.client.module.modules.render;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.setting.impl.SliderSetting;
import keystrokesmod.client.module.setting.impl.TickSetting;
import keystrokesmod.client.util.render.RenderUtil;
import keystrokesmod.client.util.system.ReflectUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@ModuleInfo(name = "NameTagsV2", category = Category.Render)
public class NameTagsV2 extends Mod {
    private final SliderSetting scaleSetting = new SliderSetting("Scale", this, 5.0D, 0.1D, 10.0D, 0.1D);
    private final SliderSetting rangeSetting = new SliderSetting("Range", this, 0.0D, 0.0D, 512.0D, 1.0D);
    private final TickSetting armorSetting = new TickSetting("Armor", this, true);
    private final TickSetting durabilitySetting = new TickSetting("Durability", this, false);
    private final TickSetting distanceSetting = new TickSetting("Distance", this, false);
    
    private float _x, _y, _z;

    @SubscribeEvent
    public void onRenderNameTag(RenderLivingEvent.Specials.Pre<? extends EntityLivingBase> event) {
        EntityLivingBase entity = event.entity;

        if (!(entity instanceof EntityPlayer)) return;
        if (mc.gameSettings.thirdPersonView == 0) return;
        
        if (rangeSetting.getInput() != 0.0D && mc.thePlayer.getDistanceToEntity(entity) > rangeSetting.getInput()) return;

        String displayName = entity.getDisplayName().getFormattedText();
        if (displayName == null || displayName.isEmpty()) return;

        event.setCanceled(true);
    }

    @SubscribeEvent
    public void render3d(RenderWorldLastEvent event) {
        List<EntityPlayer> validPlayers = new ArrayList<>();
        double range = rangeSetting.getInput();
        float partialTicks = ReflectUtil.getTimer().renderPartialTicks;
        if (mc.gameSettings.thirdPersonView == 0) return;

        for (EntityPlayer entity : mc.theWorld.playerEntities) {
            if (!(entity instanceof EntityPlayer)) continue;

            if (entity.getName().contains("[NPC]")) continue;

            double distance = mc.thePlayer.getDistanceToEntity(entity);
            if (range != 0.0D && distance > range) continue;

            validPlayers.add(entity);
            if (validPlayers.size() > 100) break;
        }

        for (EntityPlayer player : validPlayers) {
            if (!(player instanceof EntityPlayer)) continue;
            player.setAlwaysRenderNameTag(false);

            double interpX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
            double interpY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
            double interpZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;

            float renderX = (float) (interpX - mc.getRenderManager().viewerPosX);
            float renderY = (float) (interpY - mc.getRenderManager().viewerPosY);
            float renderZ = (float) (interpZ - mc.getRenderManager().viewerPosZ);

            renderNametag(player, renderX, renderY, renderZ);
        }
    }

    private void drawNames(EntityPlayer player) {
        String playerName = getPlayerName(player);
        float nameWidth = (float) getWidth(playerName);
        float halfNameWidth = nameWidth / 2.0F + 2.2F;

        float xStart = -halfNameWidth - 2.2F;
        float xEnd = halfNameWidth;
        float boxTop = -3.0F;
        float boxBottom = 10.0F;

        int borderColor = new Color(20, 20, 20, 180).getRGB();
        int backgroundColor = new Color(10, 10, 10, 200).getRGB();

        RenderUtil.drawBorderedRect(xStart, boxTop, xEnd, boxBottom, 1.0F, borderColor, backgroundColor);

        GlStateManager.disableDepth();
        drawString(playerName, -nameWidth / 2.0F, 0.0F, 0xFFFFFF);
        GlStateManager.enableDepth();
    }


    private void drawString(String string, float x, float y, int z) {
        mc.fontRendererObj.drawStringWithShadow(string, x, y, z);
    }

    private int getWidth(String string) {
        return mc.fontRendererObj.getStringWidth(string);
    }

    private void startDrawing(float x, float y, float z, EntityPlayer player) {
        float rotateX = mc.gameSettings.thirdPersonView == 2 ? -1.0F : 1.0F;
        double scaleRatio = (double) (getSize(player) / 10.0F * (float) scaleSetting.getInput()) * 1.5D;
        GL11.glPushMatrix();
        RenderUtil.startDrawing();
        GL11.glTranslatef(x, y, z);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(mc.getRenderManager().playerViewX, rotateX, 0.0F, 0.0F);
        GL11.glScaled(-0.01666666753590107D * scaleRatio, -0.01666666753590107D * scaleRatio, 0.01666666753590107D * scaleRatio);
    }

    private void stopDrawing() {
        RenderUtil.stopDrawing();
        GlStateManager.color(1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    private void renderNametag(EntityPlayer player, float x, float y, float z) {
        y += (float) (1.55D + (player.isSneaking() ? 0.5D : 0.7D));
        startDrawing(x, y, z, player);
        drawNames(player);
        GL11.glColor4d(1.0D, 1.0D, 1.0D, 1.0D);
        if (armorSetting.isToggled()) {
            renderArmor(player);
        }

        stopDrawing();
    }

    private void renderArmor(EntityPlayer player) {
        ItemStack[] armor = player.inventory.armorInventory;
        int pos = 0;

        for (ItemStack is : armor) {
            if (is != null) {
                pos -= 8;
            }
        }

        if (player.getHeldItem() != null) {
            pos -= 8;
            ItemStack var10 = player.getHeldItem().copy();
            if (var10.hasEffect() && (var10.getItem() instanceof ItemTool || var10.getItem() instanceof ItemArmor)) {
                var10.stackSize = 1;
            }

            renderItemStack(var10, pos, -20);
            pos += 16;
        }

        armor = player.inventory.armorInventory;

        for (int i = 3; i >= 0; --i) {
            ItemStack var11 = armor[i];
            if (var11 != null) {
                renderItemStack(var11, pos, -20);
                pos += 16;
            }
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private String getPlayerName(EntityPlayer player) {
        boolean isDistanceSettingToggled = distanceSetting.isToggled();
        return (isDistanceSettingToggled ? (new DecimalFormat("#.##")).format(mc.thePlayer.getDistanceToEntity(player)) + "m " : "") + player.getDisplayName().getFormattedText();
    }

    private float getSize(EntityPlayer player) {
        return Math.max(mc.thePlayer.getDistanceToEntity(player) / 4.0F, 2.0F);
    }

    private void renderItemStack(ItemStack is, int xPos, int yPos) {
        GlStateManager.pushMatrix();
        GlStateManager.depthMask(true);
        GlStateManager.clear(256);
        RenderHelper.enableStandardItemLighting();
        mc.getRenderItem().zLevel = -150.0F;
        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        mc.getRenderItem().renderItemAndEffectIntoGUI(is, xPos, yPos);
        mc.getRenderItem().renderItemOverlays(mc.fontRendererObj, is, xPos, yPos);
        mc.getRenderItem().zLevel = 0.0F;
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableCull();
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.disableLighting();
        GlStateManager.scale(0.5D, 0.5D, 0.5D);
        GlStateManager.disableDepth();
        renderEnchantText(is, xPos, yPos);
        GlStateManager.enableDepth();
        GlStateManager.scale(2.0F, 2.0F, 2.0F);
        GlStateManager.popMatrix();
    }

    private void renderEnchantText(ItemStack is, int xPos, int yPos) {
        int newYPos = yPos - 24;

        int protection = EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, is);
        int powerLvl = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, is);
        int punchLvl = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, is);
        int sharpnessLvl = EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, is);
        int knockbackLvl = EnchantmentHelper.getEnchantmentLevel(Enchantment.knockback.effectId, is);
        int efficiencyLvl = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, is);

        if (is.getItem() instanceof ItemArmor) {
            int remainingDurability = is.getMaxDamage() - is.getItemDamage();

            if (durabilitySetting.isToggled()) {
                mc.fontRendererObj.drawStringWithShadow(String.valueOf(remainingDurability), (float) (xPos * 2), (float) yPos, 0xFFFFFF);
            }

            if (protection > 0) {
                mc.fontRendererObj.drawStringWithShadow("p" + protection, (float) (xPos * 2), (float) newYPos, 0xFFFFFF);
                newYPos += 8;
            }
        }

        if (is.getItem() instanceof ItemBow) {
            if (powerLvl > 0) {
                mc.fontRendererObj.drawStringWithShadow("pow" + powerLvl, (float) (xPos * 2), (float) newYPos, 0xFFFFFF);
                newYPos += 8;
            }

            if (punchLvl > 0) {
                mc.fontRendererObj.drawStringWithShadow("pun" + punchLvl, (float) (xPos * 2), (float) newYPos, 0xFFFFFF);
                newYPos += 8;
            }
        }

        if (is.getItem() instanceof ItemSword) {
            if (sharpnessLvl > 0) {
                mc.fontRendererObj.drawStringWithShadow("sh" + sharpnessLvl, (float) (xPos * 2), (float) newYPos, 0xFFFFFF);
                newYPos += 8;
            }

            if (knockbackLvl > 0) {
                mc.fontRendererObj.drawStringWithShadow("kb" + knockbackLvl, (float) (xPos * 2), (float) newYPos, 0xFFFFFF);
                newYPos += 8;
            }
        }

        if (is.getItem() instanceof ItemTool) {
            if (efficiencyLvl > 0) {
                mc.fontRendererObj.drawStringWithShadow("eff" + efficiencyLvl, (float) (xPos * 2), (float) newYPos, 0xFFFFFF);
                newYPos += 8;
            }
        }
    }
}