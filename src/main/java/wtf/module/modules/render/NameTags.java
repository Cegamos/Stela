package wtf.module.modules.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import wtf.module.Category;
import wtf.module.ModuleInfo;
import wtf.module.modules.Mod;
import wtf.module.value.impl.BooleanValue;
import wtf.module.value.impl.NumberValue;
import wtf.util.Utils;

@ModuleInfo(name = "Nametags", category = Category.Render)
public class NameTags extends Mod {
    private final NumberValue a = new NumberValue("Offset", this, 0.0, -40.0, 40.0, 1.0);
    private final BooleanValue b = new BooleanValue("Rect", this, true);
    private final BooleanValue c = new BooleanValue("Show health", this, true);
    private final BooleanValue d = new BooleanValue("Show invis", this, true);
    private final BooleanValue rm = new BooleanValue("Remove tags", this, false);

    @SubscribeEvent
    public void r(final RenderLivingEvent.Specials.Pre e) {
        if (rm.getValue()) {
            e.setCanceled(true);
        }
        else if (e.entity instanceof EntityPlayer && e.entity.deathTime == 0) {
            final EntityPlayer en = (EntityPlayer)e.entity;
            if (!d.getValue() && en.isInvisible()) {
                return;
            }
            e.setCanceled(true);
            String str = en.getDisplayName().getFormattedText();
            if (c.getValue()) {
                final double r = en.getHealth() / en.getMaxHealth();
                final String h = ((r < 0.3) ? "§c" : ((r < 0.5) ? "§6" : ((r < 0.7) ? "§e" : "§a"))) + Utils.Java.round(en.getHealth(), 1);
                str = str + " " + h;
            }
            GlStateManager.pushMatrix();
            GlStateManager.translate((float)e.x + 0.0f, (float)e.y + en.height + 0.5f, (float)e.z);
            GL11.glNormal3f(0.0f, 1.0f, 0.0f);
            GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0f, 1.0f, 0.0f);
            GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0f, 0.0f, 0.0f);
            final float f1 = 0.02666667f;
            GlStateManager.scale(-f1, -f1, f1);
            if (en.isSneaking()) {
                GlStateManager.translate(0.0f, 9.374999f, 0.0f);
            }
            GlStateManager.disableLighting();
            GlStateManager.depthMask(false);
            GlStateManager.disableDepth();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            final Tessellator tessellator = Tessellator.getInstance();
            final WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            final int i = (int)(-a.getValue());
            final int j = mc.fontRendererObj.getStringWidth(str) / 2;
            GlStateManager.disableTexture2D();
            if (b.getValue()) {
                worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
                worldrenderer.pos((double)(-j - 1), (double)(-1 + i), 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex();
                worldrenderer.pos((double)(-j - 1), (double)(8 + i), 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex();
                worldrenderer.pos((double)(j + 1), (double)(8 + i), 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex();
                worldrenderer.pos((double)(j + 1), (double)(-1 + i), 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex();
                tessellator.draw();
            }
            GlStateManager.enableTexture2D();
            mc.fontRendererObj.drawString(str, -mc.fontRendererObj.getStringWidth(str) / 2, i, -1);
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            GlStateManager.enableLighting();
            GlStateManager.disableBlend();
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.popMatrix();
        }
    }
}
