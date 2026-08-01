package wtf.util.render;

import java.awt.Color;
import java.lang.reflect.Method;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import wtf.util.IMinecraft;
import wtf.util.system.ReflectUtil;

public class RenderUtil implements IMinecraft {
    public static void stopDrawing() {
        GL11.glDisable(3042);
        GL11.glEnable(3553);
        GL11.glDisable(2848);
        GL11.glDisable(3042);
        GL11.glEnable(2929);
    }
    
    public static void startDrawing() {
        GL11.glEnable(3042);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        
        try {
            final Method m = ReflectionHelper.findMethod((Class)EntityRenderer.class, (Object)Minecraft.getMinecraft().entityRenderer, new String[] { "setupCameraTransform", "setupCameraTransform" }, new Class[] { Float.TYPE, Integer.TYPE });
            m.setAccessible(true);
            m.invoke(Minecraft.getMinecraft().entityRenderer, ReflectUtil.getTimer().renderPartialTicks, 0);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public static void color(double red, double green, double blue, double alpha) {
        GL11.glColor4d(red, green, blue, alpha);
    }
    
    public static void color(int color) {
    	GL11.glColor4ub(
                (byte) (color >> 16 & 0xFF),
                (byte) (color >> 8 & 0xFF),
                (byte) (color & 0xFF),
                (byte) (color >> 24 & 0xFF));
    }

    public static void resetColor() {
        color(1, 1, 1, 1);
    }
    
    public static void setAlphaLimit(float limit) {
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, (float) (limit * .01));
    }
    
    public static int applyOpacity(int color, float opacity) {
        Color old = new Color(color);
        return applyOpacity(old, opacity).getRGB();
    }

    public static Color applyOpacity(final Color color, float opacity) {
        opacity = Math.min(1.0f, Math.max(0.0f, opacity));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (color.getAlpha() * opacity));
    }
    
    public static void drawRect(double d0, double d1, double d2, double d3, final int i) {
        if (d0 < d2) {
            final double d4 = d0;
            d0 = d2;
            d2 = d4;
        }
        if (d1 < d3) {
            final double d4 = d1;
            d1 = d3;
            d3 = d4;
        }
        final float f = (i >> 24 & 0xFF) / 255.0f;
        final float f2 = (i >> 16 & 0xFF) / 255.0f;
        final float f3 = (i >> 8 & 0xFF) / 255.0f;
        final float f4 = (i & 0xFF) / 255.0f;
        final Tessellator tessellator = Tessellator.getInstance();
        final WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f2, f3, f4, f);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(d0, d3, 0.0).endVertex();
        worldrenderer.pos(d2, d3, 0.0).endVertex();
        worldrenderer.pos(d2, d1, 0.0).endVertex();
        worldrenderer.pos(d0, d1, 0.0).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
    
    public static void drawBorderedRect(final float f, final float f1, final float f2, final float f3, final float f4, final int i, final int j) {
        drawRect(f, f1, f2, f3, j);
        final float f5 = (i >> 24 & 0xFF) / 255.0f;
        final float f6 = (i >> 16 & 0xFF) / 255.0f;
        final float f7 = (i >> 8 & 0xFF) / 255.0f;
        final float f8 = (i & 0xFF) / 255.0f;
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GL11.glPushMatrix();
        GL11.glColor4f(f6, f7, f8, f5);
        GL11.glLineWidth(f4);
        GL11.glBegin(1);
        GL11.glVertex2d((double)f, (double)f1);
        GL11.glVertex2d((double)f, (double)f3);
        GL11.glVertex2d((double)f2, (double)f3);
        GL11.glVertex2d((double)f2, (double)f1);
        GL11.glVertex2d((double)f, (double)f1);
        GL11.glVertex2d((double)f2, (double)f1);
        GL11.glVertex2d((double)f, (double)f3);
        GL11.glVertex2d((double)f2, (double)f3);
        GL11.glEnd();
        GL11.glPopMatrix();
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glDisable(2848);
    }
    
    public static void drawBorderedRoundedRect1(float x, float y, float x1, float y1, float radius, float borderSize, int borderC, int insideC) {
        drawRoundedRect1(x, y, x1, y1, radius, insideC);
        drawRoundedOutline(x, y, x1, y1, radius, borderSize, borderC);
    }
    
    public static void drawRoundedRect1(float x, float y, float x1, float y1, final float radius, final int color) {
        drawRoundedRect(x, y, x1, y1, radius, color,  new boolean[] {true,true,true,true} );
    }
    
	public static void drawRoundedRect2(double d, double e, double g, double h, float radius, int color) {
		float x1 = (float) (d + g), y1 = (float) (e + h);
		final float f = (color >> 24 & 0xFF) / 255.0F, f1 = (color >> 16 & 0xFF) / 255.0F,
				f2 = (color >> 8 & 0xFF) / 255.0F, f3 = (color & 0xFF) / 255.0F;
		GL11.glPushAttrib(0);
		GL11.glScaled(0.5, 0.5, 0.5);

		d *= 2;
		e *= 2;
		x1 *= 2;
		y1 *= 2;

		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glColor4f(f1, f2, f3, f);
		GlStateManager.enableBlend();
		GL11.glEnable(GL11.GL_LINE_SMOOTH);

		GL11.glBegin(GL11.GL_POLYGON);
		final double v = Math.PI / 180;

		for (int i = 0; i <= 90; i += 3) {
			GL11.glVertex2d(d + radius + MathHelper.sin((float) (i * v)) * (radius * -1),
					e + radius + MathHelper.cos((float) (i * v)) * (radius * -1));
		}

		for (int i = 90; i <= 180; i += 3) {
			GL11.glVertex2d(d + radius + MathHelper.sin((float) (i * v)) * (radius * -1),
					y1 - radius + MathHelper.cos((float) (i * v)) * (radius * -1));
		}

		for (int i = 0; i <= 90; i += 3) {
			GL11.glVertex2d(x1 - radius + MathHelper.sin((float) (i * v)) * radius,
					y1 - radius + MathHelper.cos((float) (i * v)) * radius);
		}

		for (int i = 90; i <= 180; i += 3) {
			GL11.glVertex2d(x1 - radius + MathHelper.sin((float) (i * v)) * radius,
					e + radius + MathHelper.cos((float) (i * v)) * radius);
		}

		GL11.glEnd();

		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_LINE_SMOOTH);
		GL11.glEnable(GL11.GL_TEXTURE_2D);

		GL11.glScaled(2, 2, 2);

		GL11.glPopAttrib();
		GL11.glColor4f(1, 1, 1, 1);
	}

    public static void drawRoundedRect(float x, float y, float x1, float y1, final float radius, final int color, boolean[] round) {
        GL11.glPushAttrib(0);
        GL11.glScaled(0.5, 0.5, 0.5);
        x *= 2.0;
        y *= 2.0;
        x1 *= 2.0;
        y1 *= 2.0;
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glEnable(2848);
        setColor(color);
        GL11.glEnable(2848);
        GL11.glBegin(GL11.GL_POLYGON);
        round(x, y, x1, y1, radius, round);
        GL11.glEnd();
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glDisable(2848);
        GL11.glDisable(3042);
        GL11.glDisable(2848);
        GL11.glScaled(2.0, 2.0, 2.0);
        GL11.glEnable(3042);
        GL11.glPopAttrib();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
    }
    
    public static void drawRoundedOutline(float x, float y, float x1, float y1, final float radius, final float borderSize, final int color) {
        drawRoundedOutline(x, y, x1, y1, radius, borderSize , color, new boolean[] {true,true,true,true});
    }

    public static void drawRoundedOutline(float x, float y, float x1, float y1, final float radius, final float borderSize, final int color, boolean[] drawCorner) {
        GL11.glPushAttrib(0);
        GL11.glScaled(0.5, 0.5, 0.5);
        x *= 2.0;
        y *= 2.0;
        x1 *= 2.0;
        y1 *= 2.0;
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        setColor(color);
        GL11.glEnable(2848);
        GL11.glLineWidth(borderSize);
        GL11.glBegin(2);
        round(x, y, x1, y1, radius, drawCorner);
        GL11.glEnd();
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glDisable(2848);
        GL11.glDisable(3042);
        GL11.glEnable(3553);
        GL11.glScaled(2.0, 2.0, 2.0);
        GL11.glPopAttrib();
        GL11.glLineWidth(1.0f);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
    }
    
    public static void round(float x, float y, float x1, float y1, float radius, final boolean[] round) {
        if(round[0])
            roundHelper(x, y, radius, -1, 1,0, 90);
        else
            GL11.glVertex2d(x, y);

        if(round[1])
            roundHelper(x, y1, radius, -1, -1, 90, 180);
        else
            GL11.glVertex2d(x, y1);

        if(round[2])
            roundHelper(x1, y1, radius, 1, -1, 0, 90);
        else
            GL11.glVertex2d(x1, y1);

        if(round[3])
            roundHelper(x1, y, radius, 1, 1, 90, 180);
        else
            GL11.glVertex2d(x1, y);
    }

    public static void roundHelper(float x, float y, float radius, int pn, int pn2, int originalRotation, int finalRotation) {
        for (int i = originalRotation; i <= finalRotation; i += 3)
            GL11.glVertex2d(x + (radius * -pn) + (Math.sin((i * 3.141592653589793) / 180.0) * radius * pn), y + (radius * pn2) + (Math.cos((i * 3.141592653589793) / 180.0) * radius * pn));
    }
    
    public static void setColor(final int color) {
        final float a = ((color >> 24) & 0xFF) / 255.0f;
        final float r = ((color >> 16) & 0xFF) / 255.0f;
        final float g = ((color >> 8) & 0xFF) / 255.0f;
        final float b = (color & 0xFF) / 255.0f;
        GL11.glColor4f(r, g, b, a);
    }
    
    public static void drawSimpleItemBox(final Entity entity, final Color color) {
		GL11.glPushMatrix();

		final RenderManager renderManager = mc.getRenderManager();

		double x = entity.posX - renderManager.viewerPosX;
		double y = entity.posY - renderManager.viewerPosY + entity.height / 2.0D;
		double z = entity.posZ - renderManager.viewerPosZ;

		GL11.glTranslated(x, y, z);

		GL11.glRotated(-renderManager.playerViewY, 0.0D, 1.0D, 0.0D);
		GL11.glRotated(renderManager.playerViewX, mc.gameSettings.thirdPersonView == 2 ? -1.0D : 1.0D, 0.0D, 0.0D);

		final float scale = .5f / 90f;
		GL11.glScalef(-scale, -scale, scale);

		final Color c = color;

		lineNoGl(-50, 0, 50, 0, c);
		lineNoGl(-50, -95, -50, 0, c);
		lineNoGl(-50, -95, 50, -95, c);
		lineNoGl(50, -95, 50, 0, c);

		GL11.glPopMatrix();
	}
    
	public static void drawSimpleBox(EntityPlayer player, int color, float partialTicks) {
		double expand = 0.0D;
		float alpha = (float) ((color >> 24) & 255) / 255.0F;
		float red = (float) ((color >> 16) & 255) / 255.0F;
		float green = (float) ((color >> 8) & 255) / 255.0F;
		float blue = (float) (color & 255) / 255.0F;

		double x = (player.lastTickPosX + ((player.posX - player.lastTickPosX) * (double) partialTicks))
				- mc.getRenderManager().viewerPosX;
		double y = (player.lastTickPosY + ((player.posY - player.lastTickPosY) * (double) partialTicks))
				- mc.getRenderManager().viewerPosY;
		double z = (player.lastTickPosZ + ((player.posZ - player.lastTickPosZ) * (double) partialTicks))
				- mc.getRenderManager().viewerPosZ;

		AxisAlignedBB bbox = player.getEntityBoundingBox().expand(0.1D + expand, 0.1D + expand, 0.1D + expand);
		AxisAlignedBB axis = new AxisAlignedBB((bbox.minX - player.posX) + x, (bbox.minY - player.posY) + y,
				(bbox.minZ - player.posZ) + z, (bbox.maxX - player.posX) + x, (bbox.maxY - player.posY) + y,
				(bbox.maxZ - player.posZ) + z);
		GL11.glPushMatrix();
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDepthMask(false);
		GL11.glLineWidth(1.0F);
		GL11.glColor4f(red, green, blue, alpha);

		RenderGlobal.drawSelectionBoundingBox(axis);

		GL11.glColor4f(1f, 1f, 1f, 1f);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDepthMask(true);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glPopMatrix();
	}
	
	public static void drawLine(double x, double y, double z, double x1, double y1, double z1, final Color color, final float width) {
		double renderX = ReflectUtil.getRenderPosX();
		double renderY = ReflectUtil.getRenderPosY();
		double renderZ = ReflectUtil.getRenderPosZ();
		
		x = x - renderX;
		x1 = x1 - renderX;
		y = y - renderY;
		y1 = y1 - renderY;
		z = z - renderZ;
		z1 = z1 - renderZ;

		GL11.glPushMatrix();
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glBlendFunc(770, 771);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glLineWidth(width);

		color(color);
		GL11.glBegin(2);
		GL11.glVertex3d(x, y, z);
		GL11.glVertex3d(x1, y1, z1);
		GL11.glEnd();

		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_LINE_SMOOTH);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glPopMatrix();
		color(Color.WHITE);
	}

	public static void lineNoGl(final double firstX, final double firstY, final double secondX, final double secondY,
			final Color color) {

		start();
		if (color != null)
			color(color);
		GL11.glLineWidth(2);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		begin(GL11.GL_LINES);
		{
			GL11.glVertex2d(firstX, firstY);
			GL11.glVertex2d(secondX, secondY);
		}
		end();
		GL11.glDisable(GL11.GL_LINE_SMOOTH);
		stop();
	}
	
	public static void begin(final int glMode) {
		GL11.glBegin(glMode);
	}
	
	public static void color(Color color) {
		if (color == null)
			color = Color.white;
		GL11.glColor4d(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, color.getAlpha() / 255F);
	}
	
	public static void start() {
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.disableTexture2D();
		GlStateManager.disableCull();
		GlStateManager.disableAlpha();
		GlStateManager.disableDepth();
	}

	public static void stop() {
		GlStateManager.enableDepth();
		GlStateManager.enableAlpha();
		GlStateManager.enableCull();
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
		GlStateManager.resetColor();
	}
	
	public static void end() {
		GL11.glEnd();
	}

    public static void drawRoundedRect(float x, float y, float width, float height, float radius, int color) {
        try {
            RoundedUtil.drawRound(x, y, width, height, radius, new Color(color, true));
        } catch (Exception ex) {
            drawRoundedRect1(x, y, x + width, y + height, radius, color);
        }
    }

    public static void drawCircle(float x, float y, float radius, int color) {
        try {
            RoundedUtil.drawRound(x - radius, y - radius, radius * 2, radius * 2, radius, new Color(color, true));
        } catch (Exception ex) {
            drawRoundedRect1(x - radius, y - radius, x + radius, y + radius, radius, color);
        }
    }
}
