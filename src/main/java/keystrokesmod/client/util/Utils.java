package keystrokesmod.client.util;

import java.awt.Color;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.lwjgl.Sys;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import keystrokesmod.client.Raven;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.modules.combat.LeftClicker;
import keystrokesmod.client.module.value.impl.RangeValue;
import keystrokesmod.client.module.value.impl.NumberValue;
import keystrokesmod.client.util.system.ReflectUtil;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class Utils implements IMinecraft {
	private static final Random rand = new Random();
	public static final String md = "Mode: ";

    public static void display(String txt) {
        if (Player.isPlayerInGame()) {
            mc.thePlayer.addChatMessage(new ChatComponentText(txt));
        }
    }
	
	public static class Player {
		public static void hotkeyToSlot(final int slot) {
			if (!isPlayerInGame()) {
				return;
			}
			mc.thePlayer.inventory.currentItem = slot;
		}

		public static void sendMessageToSelf(final String txt) {
			if (isPlayerInGame()) {
				final String m = Client.reformat("&7[&dR&7]&r " + txt);
				mc.thePlayer.addChatMessage((IChatComponent) new ChatComponentText(m));
			}
		}

		public static boolean isPlayerInGame() {
			return mc.thePlayer != null && mc.theWorld != null;
		}

		public static boolean isMoving() {
			return mc.thePlayer.moveForward != 0.0f || mc.thePlayer.moveStrafing != 0.0f;
		}

		public static void aim(final Entity en, final float ps, final boolean pc) {
			if (en != null) {
				final float[] t = getTargetRotations(en);
				if (t != null) {
					final float y = t[0];
					final float p = t[1] + 4.0f + ps;
					if (pc) {
						mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C05PacketPlayerLook(y, p, mc.thePlayer.onGround));
					} else {
						mc.thePlayer.rotationYaw = y;
						mc.thePlayer.rotationPitch = p;
					}
				}
			}
		}

		public static double fovFromEntity(final Entity en) {
			return ((mc.thePlayer.rotationYaw - fovToEntity(en)) % 360.0 + 540.0) % 360.0 - 180.0;
		}

		public static float fovToEntity(final Entity ent) {
			final double x = ent.posX - mc.thePlayer.posX;
			final double z = ent.posZ - mc.thePlayer.posZ;
			final double yaw = Math.atan2(x, z) * 57.2957795;
			return (float) (yaw * -1.0);
		}

		public static boolean fov(final Entity entity, float fov) {
			fov *= 0.5;
			final double v = ((mc.thePlayer.rotationYaw - fovToEntity(entity)) % 360.0 + 540.0) % 360.0 - 180.0;
			return (v > 0.0 && v < fov) || (-fov < v && v < 0.0);
		}

		public static double getPlayerBPS(final Entity en, final int d) {
			final double x = en.posX - en.prevPosX;
			final double z = en.posZ - en.prevPosZ;
			final double sp = Math.sqrt(x * x + z * z) * 20.0;
			return Java.round(sp, d);
		}

		public static boolean playerOverAir() {
			final double x = mc.thePlayer.posX;
			final double y = mc.thePlayer.posY - 1.0;
			final double z = mc.thePlayer.posZ;
			final BlockPos p = new BlockPos(MathHelper.floor_double(x), MathHelper.floor_double(y),
					MathHelper.floor_double(z));
			return mc.theWorld.isAirBlock(p);
		}

		public static boolean playerUnderBlock() {
			final double x = mc.thePlayer.posX;
			final double y = mc.thePlayer.posY + 2.0;
			final double z = mc.thePlayer.posZ;
			final BlockPos p = new BlockPos(MathHelper.floor_double(x), MathHelper.floor_double(y),
					MathHelper.floor_double(z));
			return mc.theWorld.isBlockFullCube(p) || mc.theWorld.isBlockNormalCube(p, false);
		}

		public static int getCurrentPlayerSlot() {
			return mc.thePlayer.inventory.currentItem;
		}

		public static boolean isPlayerHoldingWeapon() {
			if (mc.thePlayer.getCurrentEquippedItem() == null) {
				return false;
			}
			final Item item = mc.thePlayer.getCurrentEquippedItem().getItem();
			return item instanceof ItemSword || item instanceof ItemAxe;
		}

		public static int getMaxDamageSlot() {
			int index = -1;
			double damage = -1.0;
			for (int slot = 0; slot <= 8; ++slot) {
				final ItemStack itemInSlot = mc.thePlayer.inventory.getStackInSlot(slot);
				if (itemInSlot != null) {
					for (final AttributeModifier mooommHelp : itemInSlot.getAttributeModifiers().values()) {
						if (mooommHelp.getAmount() > damage) {
							damage = mooommHelp.getAmount();
							index = slot;
						}
					}
				}
			}
			return index;
		}

		public static double getSlotDamage(final int slot) {
			final ItemStack itemInSlot = mc.thePlayer.inventory.getStackInSlot(slot);
			if (itemInSlot == null) {
				return -1.0;
			}
			final Iterator<AttributeModifier> iterator = itemInSlot.getAttributeModifiers().values().iterator();
			if (iterator.hasNext()) {
				final AttributeModifier mooommHelp = iterator.next();
				return mooommHelp.getAmount();
			}
			return -1.0;
		}

		public static ArrayList<Integer> playerWearingArmor() {
			final ArrayList<Integer> wearingArmor = new ArrayList<Integer>();
			for (int armorPiece = 0; armorPiece < 4; ++armorPiece) {
				if (mc.thePlayer.getCurrentArmor(armorPiece) != null) {
					if (armorPiece == 0) {
						wearingArmor.add(3);
					} else if (armorPiece == 1) {
						wearingArmor.add(2);
					} else if (armorPiece == 2) {
						wearingArmor.add(1);
					} else if (armorPiece == 3) {
						wearingArmor.add(0);
					}
				}
			}
			return wearingArmor;
		}

		public static int getBlockAmountInCurrentStack(final int currentItem) {
			if (mc.thePlayer.inventory.getStackInSlot(currentItem) == null) {
				return 0;
			}
			final ItemStack itemStack = mc.thePlayer.inventory.getStackInSlot(currentItem);
			if (itemStack.getItem() instanceof ItemBlock) {
				return itemStack.stackSize;
			}
			return 0;
		}

		public static boolean tryingToCombo() {
			return Mouse.isButtonDown(0) && Mouse.isButtonDown(1);
		}

		public static float[] getTargetRotations(final Entity q) {
			if (q == null) {
				return null;
			}
			final double diffX = q.posX - mc.thePlayer.posX;
			double diffY;
			if (q instanceof EntityLivingBase) {
				final EntityLivingBase en = (EntityLivingBase) q;
				diffY = en.posY + en.getEyeHeight() * 0.9
						- (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
			} else {
				diffY = (q.getEntityBoundingBox().minY + q.getEntityBoundingBox().maxY) / 2.0
						- (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
			}
			final double diffZ = q.posZ - mc.thePlayer.posZ;
			final double dist = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);
			final float yaw = (float) (Math.atan2(diffZ, diffX) * 180.0 / 3.141592653589793) - 90.0f;
			final float pitch = (float) (-(Math.atan2(diffY, dist) * 180.0 / 3.141592653589793));
			return new float[] {
					mc.thePlayer.rotationYaw
							+ MathHelper.wrapAngleTo180_float(yaw - mc.thePlayer.rotationYaw),
					mc.thePlayer.rotationPitch
							+ MathHelper.wrapAngleTo180_float(pitch - mc.thePlayer.rotationPitch) };
		}

		public static void fixMovementSpeed(final double s, final boolean m) {
			if (!m || isMoving()) {
				mc.thePlayer.motionX = -Math.sin(correctRotations()) * s;
				mc.thePlayer.motionZ = Math.cos(correctRotations()) * s;
			}
		}

		public static void bop(final double s) {
			double forward = mc.thePlayer.movementInput.moveForward;
			double strafe = mc.thePlayer.movementInput.moveStrafe;
			float yaw = mc.thePlayer.rotationYaw;
			if (forward == 0.0 && strafe == 0.0) {
				mc.thePlayer.motionX = 0.0;
				mc.thePlayer.motionZ = 0.0;
			} else {
				if (forward != 0.0) {
					if (strafe > 0.0) {
						yaw += ((forward > 0.0) ? -45 : 45);
					} else if (strafe < 0.0) {
						yaw += ((forward > 0.0) ? 45 : -45);
					}
					strafe = 0.0;
					if (forward > 0.0) {
						forward = 1.0;
					} else if (forward < 0.0) {
						forward = -1.0;
					}
				}
				final double rad = Math.toRadians(yaw + 90.0f);
				final double sin = Math.sin(rad);
				final double cos = Math.cos(rad);
				mc.thePlayer.motionX = forward * s * cos + strafe * s * sin;
				mc.thePlayer.motionZ = forward * s * sin - strafe * s * cos;
			}
		}

		public static float correctRotations() {
			float yw = mc.thePlayer.rotationYaw;
			if (mc.thePlayer.moveForward < 0.0f) {
				yw += 180.0f;
			}
			float f;
			if (mc.thePlayer.moveForward < 0.0f) {
				f = -0.5f;
			} else if (mc.thePlayer.moveForward > 0.0f) {
				f = 0.5f;
			} else {
				f = 1.0f;
			}
			if (mc.thePlayer.moveStrafing > 0.0f) {
				yw -= 90.0f * f;
			}
			if (mc.thePlayer.moveStrafing < 0.0f) {
				yw += 90.0f * f;
			}
			yw *= 0.017453292f;
			return yw;
		}

		public static double pythagorasMovement() {
			return Math.sqrt(mc.thePlayer.motionX * mc.thePlayer.motionX
					+ mc.thePlayer.motionZ * mc.thePlayer.motionZ);
		}

		public static void swing() {
			final EntityPlayerSP p = mc.thePlayer;
			final int armSwingEnd = p.isPotionActive(Potion.digSpeed)
					? (6 - (1 + p.getActivePotionEffect(Potion.digSpeed).getAmplifier()))
					: (p.isPotionActive(Potion.digSlowdown)
							? (6 + (1 + p.getActivePotionEffect(Potion.digSlowdown).getAmplifier()) * 2)
							: 6);
			if (!p.isSwingInProgress || p.swingProgressInt >= armSwingEnd / 2 || p.swingProgressInt < 0) {
				p.swingProgressInt = -1;
				p.isSwingInProgress = true;
			}
		}
	}

	public static class Client {
		public static List<NetworkPlayerInfo> getPlayers() {
			final List<NetworkPlayerInfo> yes = new ArrayList<NetworkPlayerInfo>();
			final List<NetworkPlayerInfo> mmmm = new ArrayList<NetworkPlayerInfo>();
			try {
				yes.addAll(mc.getNetHandler().getPlayerInfoMap());
			} catch (NullPointerException r) {
				return yes;
			}
			for (final NetworkPlayerInfo ergy43d : yes) {
				if (!mmmm.contains(ergy43d)) {
					mmmm.add(ergy43d);
				}
			}
			return mmmm;
		}

		public static boolean othersExist() {
			for (final Entity wut : mc.theWorld.getLoadedEntityList()) {
				if (wut instanceof EntityPlayer) {
					return true;
				}
			}
			return false;
		}

		public static void setMouseButtonState(final int mouseButton, final boolean held) {
		    MouseEvent mouseEvent = new MouseEvent();

		    ObfuscationReflectionHelper.setPrivateValue(MouseEvent.class, mouseEvent, mouseButton, "button");
		    ObfuscationReflectionHelper.setPrivateValue(MouseEvent.class, mouseEvent, held, "buttonstate");

		    MinecraftForge.EVENT_BUS.post(mouseEvent);

		    ByteBuffer buttons = ObfuscationReflectionHelper.getPrivateValue(Mouse.class, null, "buttons");

		    buttons.put(mouseButton, (byte) (held ? 1 : 0));

		    ObfuscationReflectionHelper.setPrivateValue(Mouse.class, null, buttons, "buttons");
		}
		
		public static void correctSliders(final NumberValue c, final NumberValue d) {
			if (c.getInput() > d.getInput()) {
				final double p = c.getInput();
				c.setValue(d.getInput());
				d.setValue(p);
			}
		}

		public static double ranModuleVal(final NumberValue a, final NumberValue b, final Random r) {
			return (a.getInput() == b.getInput()) ? a.getInput()
					: (a.getInput() + r.nextDouble() * (b.getInput() - a.getInput()));
		}

		public static double ranModuleVal(final RangeValue a, final Random r) {
			return (a.getInputMin() == a.getInputMax()) ? a.getInputMin()
					: (a.getInputMin() + r.nextDouble() * (a.getInputMax() - a.getInputMin()));
		}

		public static boolean isHyp() {
			if (!Player.isPlayerInGame()) {
				return false;
			}
			try {
				return !mc.isSingleplayer()
						&& mc.getCurrentServerData().serverIP.toLowerCase().contains("hypixel.net");
			} catch (Exception welpBruh) {
				welpBruh.printStackTrace();
				return false;
			}
		}

		public static boolean autoClickerClicking() {
			final Mod autoClicker = Raven.moduleManager.getModuleByClazz(LeftClicker.class);
			return autoClicker != null && autoClicker.isEnabled() && autoClicker.isEnabled() && Mouse.isButtonDown(0);
		}

		public static int rainbowDraw(final long speed, final long... delay) {
			final long time = System.currentTimeMillis() + ((delay.length > 0) ? delay[0] : 0L);
			return Color.getHSBColor(time % (15000L / speed) / (15000.0f / speed), 1.0f, 1.0f).getRGB();
		}

		public static int astolfoColorsDraw(final int yOffset, final int yTotal, final float speed) {
			float hue;
			for (hue = System.currentTimeMillis() % (int) speed
					+ (float) ((yTotal - yOffset) * 9); hue > speed; hue -= speed) {
			}
			hue /= speed;
			if (hue > 0.5) {
				hue = 0.5f - (hue - 0.5f);
			}
			hue += 0.5f;
			return Color.HSBtoRGB(hue, 0.5f, 1.0f);
		}

		public static int astolfoColorsDraw(final int yOffset, final int yTotal) {
			return astolfoColorsDraw(yOffset, yTotal, 2900.0f);
		}

		public static int kopamedColoursDraw(final int yOffset, final int yTotal) {
			final float speed = 6428.0f;
			float hue;
			try {
				hue = System.currentTimeMillis() % (int) speed + (float) ((yTotal - yOffset) / (yOffset / yTotal));
			} catch (ArithmeticException divisionByZero) {
				hue = System.currentTimeMillis() % (int) speed
						+ (float) ((yTotal - yOffset) / (yOffset / yTotal + 1 + 1));
			}
			while (hue > speed) {
				hue -= speed;
			}
			hue /= speed;
			if (hue > 2.0f) {
				hue = 2.0f - (hue - 2.0f);
			}
			hue += 2.0f;
			float current;
			for (current = System.currentTimeMillis() % speed
					+ (yOffset + yTotal) * 9; current > speed; current -= speed) {
			}
			current /= speed;
			if (current > 2.0f) {
				current = 2.0f - (current - 2.0f);
			}
			current += 2.0f;
			return Color.HSBtoRGB(current / (current - yTotal) + current, 1.0f, 1.0f);
		}

		public static boolean currentScreenMinecraft() {
			return mc.currentScreen == null;
		}

		public static int serverResponseTime() {
			return mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID()).getResponseTime();
		}

		public static List<String> getPlayersFromScoreboard() {
			final List<String> lines = new ArrayList<String>();
			if (mc.theWorld == null) {
				return lines;
			}
			final Scoreboard scoreboard = mc.theWorld.getScoreboard();
			if (scoreboard != null) {
				final ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
				if (objective != null) {
					Collection<Score> scores = (Collection<Score>) scoreboard.getSortedScores(objective);
					final List<Score> list = new ArrayList<Score>();
					for (final Score score : scores) {
						if (score != null && score.getPlayerName() != null && !score.getPlayerName().startsWith("#")) {
							list.add(score);
						}
					}
					if (list.size() > 15) {
					    scores = Lists.newArrayList(Iterables.skip(list, list.size() - 15));
					} else {
					    scores = list;
					}
					for (final Score score : scores) {
						final ScorePlayerTeam team = scoreboard.getPlayersTeam(score.getPlayerName());
						lines.add(ScorePlayerTeam.formatPlayerName((Team) team, score.getPlayerName()));
					}
				}
			}
			return lines;
		}

		public static String reformat(final String txt) {
			return txt.replace("&", "�");
		}
	}

	public static class Java {
		public static int getValue(final JsonObject type, final String member) {
			try {
				return type.get(member).getAsInt();
			} catch (NullPointerException er) {
				return 0;
			}
		}

		public static int indexOf(final String key, final String[] wut) {
			for (int o = 0; o < wut.length; ++o) {
				if (wut[o].equals(key)) {
					return o;
				}
			}
			return -1;
		}

		public static long getSystemTime() {
			return Sys.getTime() * 1000L / Sys.getTimerResolution();
		}

		public static Random rand() {
			return rand;
		}

		public static double round(final double n, final int d) {
			if (d == 0) {
				return (double) Math.round(n);
			}
			final double p = Math.pow(10.0, d);
			return Math.round(n * p) / p;
		}

		public static String str(final String s) {
			final char[] n = StringUtils.stripControlCodes(s).toCharArray();
			final StringBuilder v = new StringBuilder();
			for (final char c : n) {
				if (c < '\u007f' && c > '\u0014') {
					v.append(c);
				}
			}
			return v.toString();
		}

		public static String capitalizeWord(final String s) {
			return s.substring(0, 1).toUpperCase() + s.substring(1);
		}

		public static String getDate() {
			final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
			final LocalDateTime now = LocalDateTime.now();
			return dtf.format(now);
		}

		public static String joinStringList(final String[] wtf, final String okwaht) {
			if (wtf == null) {
				return "";
			}
			if (wtf.length <= 1) {
				return "";
			}
			final StringBuilder finalString = new StringBuilder(wtf[0]);
			for (int i = 1; i < wtf.length; ++i) {
				finalString.append(okwaht).append(wtf[i]);
			}
			return finalString.toString();
		}

		public static ArrayList<String> toArrayList(final String[] fakeList) {
			return new ArrayList<String>(Arrays.asList(fakeList));
		}

		public static List<String> StringListToList(final String[] whytho) {
			final List<String> howTohackNasaWorking2021NoScamDotCom = new ArrayList<String>();
			Collections.addAll(howTohackNasaWorking2021NoScamDotCom, whytho);
			return howTohackNasaWorking2021NoScamDotCom;
		}

		public static JsonObject getStringAsJson(final String text) {
			return new JsonParser().parse(text).getAsJsonObject();
		}

		public static String randomChoice(final String[] strings) {
			return strings[rand.nextInt(strings.length)];
		}

		public static int randomInt(final double inputMin, final double v) {
			return (int) (Math.random() * (v - inputMin) + inputMin);
		}
	}

	public static class HUD implements IMinecraft {
		public static final int rc = -1089466352;
		public static boolean ring_c = false;

		public static void re(final BlockPos bp, final int color, final boolean shade) {
			if (bp != null) {
				final double x = bp.getX() - mc.getRenderManager().viewerPosX;
				final double y = bp.getY() - mc.getRenderManager().viewerPosY;
				final double z = bp.getZ() - mc.getRenderManager().viewerPosZ;
				GL11.glBlendFunc(770, 771);
				GL11.glEnable(3042);
				GL11.glLineWidth(2.0f);
				GL11.glDisable(3553);
				GL11.glDisable(2929);
				GL11.glDepthMask(false);
				final float a = (color >> 24 & 0xFF) / 255.0f;
				final float r = (color >> 16 & 0xFF) / 255.0f;
				final float g = (color >> 8 & 0xFF) / 255.0f;
				final float b = (color & 0xFF) / 255.0f;
				GL11.glColor4d((double) r, (double) g, (double) b, (double) a);
				RenderGlobal.drawSelectionBoundingBox(new AxisAlignedBB(x, y, z, x + 1.0, y + 1.0, z + 1.0));
				if (shade) {
					dbb(new AxisAlignedBB(x, y, z, x + 1.0, y + 1.0, z + 1.0), r, g, b);
				}
				GL11.glEnable(3553);
				GL11.glEnable(2929);
				GL11.glDepthMask(true);
				GL11.glDisable(3042);
			}
		}

		public static void drawBoxAroundEntity(final Entity e, final int type, final double expand, final double shift,
				int color, final boolean damage) {
			if (e instanceof EntityLivingBase) {
				final double x = e.lastTickPosX + (e.posX - e.lastTickPosX) * ReflectUtil.getTimer().renderPartialTicks
						- mc.getRenderManager().viewerPosX;
				final double y = e.lastTickPosY + (e.posY - e.lastTickPosY) * ReflectUtil.getTimer().renderPartialTicks
						- mc.getRenderManager().viewerPosY;
				final double z = e.lastTickPosZ + (e.posZ - e.lastTickPosZ) * ReflectUtil.getTimer().renderPartialTicks
						- mc.getRenderManager().viewerPosZ;
				final float d = (float) expand / 40.0f;
				if (e instanceof EntityPlayer && damage && ((EntityPlayer) e).hurtTime != 0) {
					color = Color.RED.getRGB();
				}
				GlStateManager.pushMatrix();
				if (type == 3) {
					GL11.glTranslated(x, y - 0.2, z);
					GL11.glRotated((double) (-mc.getRenderManager().playerViewY), 0.0, 1.0, 0.0);
					GlStateManager.disableDepth();
					GL11.glScalef(0.03f + d, 0.03f + d, 0.03f + d);
					final int outline = Color.black.getRGB();
					Gui.drawRect(-20, -1, -26, 75, outline);
					Gui.drawRect(20, -1, 26, 75, outline);
					Gui.drawRect(-20, -1, 21, 5, outline);
					Gui.drawRect(-20, 70, 21, 75, outline);
					if (color != 0) {
						Gui.drawRect(-21, 0, -25, 74, color);
						Gui.drawRect(21, 0, 25, 74, color);
						Gui.drawRect(-21, 0, 24, 4, color);
						Gui.drawRect(-21, 71, 25, 74, color);
					} else {
						final int st = Client.rainbowDraw(2L, 0L);
						final int en = Client.rainbowDraw(2L, 1000L);
						dGR(-21, 0, -25, 74, st, en);
						dGR(21, 0, 25, 74, st, en);
						Gui.drawRect(-21, 0, 21, 4, en);
						Gui.drawRect(-21, 71, 21, 74, st);
					}
					GlStateManager.enableDepth();
				} else if (type == 4) {
					final EntityLivingBase en2 = (EntityLivingBase) e;
					final double r = en2.getHealth() / en2.getMaxHealth();
					final int b = (int) (74.0 * r);
					final int hc = (r < 0.3) ? Color.red.getRGB()
							: ((r < 0.5) ? Color.orange.getRGB()
									: ((r < 0.7) ? Color.yellow.getRGB() : Color.green.getRGB()));
					GL11.glTranslated(x, y - 0.2, z);
					GL11.glRotated((double) (-mc.getRenderManager().playerViewY), 0.0, 1.0, 0.0);
					GlStateManager.disableDepth();
					GL11.glScalef(0.03f + d, 0.03f + d, 0.03f + d);
					final int i = (int) (21.0 + shift * 2.0);
					Gui.drawRect(i, -1, i + 5, 75, Color.black.getRGB());
					Gui.drawRect(i + 1, b, i + 4, 74, Color.darkGray.getRGB());
					Gui.drawRect(i + 1, 0, i + 4, b, hc);
					GlStateManager.enableDepth();
				} else if (type == 6) {
					d3p(x, y, z, 0.699999988079071, 45, 1.5f, color, color == 0);
				} else {
					if (color == 0) {
						color = Client.rainbowDraw(2L, 0L);
					}
					final float a = (color >> 24 & 0xFF) / 255.0f;
					final float r2 = (color >> 16 & 0xFF) / 255.0f;
					final float g = (color >> 8 & 0xFF) / 255.0f;
					final float b2 = (color & 0xFF) / 255.0f;
					if (type == 5) {
						GL11.glTranslated(x, y - 0.2, z);
						GL11.glRotated((double) (-mc.getRenderManager().playerViewY), 0.0, 1.0, 0.0);
						GlStateManager.disableDepth();
						GL11.glScalef(0.03f + d, 0.03f, 0.03f + d);
						d2p(0.0, 95.0, 10, 3, Color.black.getRGB());
						for (int i = 0; i < 6; ++i) {
							d2p(0.0, 95 + (10 - i), 3, 4, Color.black.getRGB());
						}
						for (int i = 0; i < 7; ++i) {
							d2p(0.0, 95 + (10 - i), 2, 4, color);
						}
						d2p(0.0, 95.0, 8, 3, color);
						GlStateManager.enableDepth();
					} else {
						final AxisAlignedBB bbox = e.getEntityBoundingBox().expand(0.1 + expand, 0.1 + expand,
								0.1 + expand);
						final AxisAlignedBB axis = new AxisAlignedBB(bbox.minX - e.posX + x, bbox.minY - e.posY + y,
								bbox.minZ - e.posZ + z, bbox.maxX - e.posX + x, bbox.maxY - e.posY + y,
								bbox.maxZ - e.posZ + z);
						GL11.glBlendFunc(770, 771);
						GL11.glEnable(3042);
						GL11.glDisable(3553);
						GL11.glDisable(2929);
						GL11.glDepthMask(false);
						GL11.glLineWidth(2.0f);
						GL11.glColor4f(r2, g, b2, a);
						if (type == 1) {
							RenderGlobal.drawSelectionBoundingBox(axis);
						} else if (type == 2) {
							dbb(axis, r2, g, b2);
						}
						GL11.glEnable(3553);
						GL11.glEnable(2929);
						GL11.glDepthMask(true);
						GL11.glDisable(3042);
					}
				}
				GlStateManager.popMatrix();
			}
		}

		public static void dbb(final AxisAlignedBB abb, final float r, final float g, final float b) {
			final float a = 0.25f;
			final Tessellator ts = Tessellator.getInstance();
			final WorldRenderer vb = ts.getWorldRenderer();
			vb.begin(7, DefaultVertexFormats.POSITION_COLOR);
			vb.pos(abb.minX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
			vb.pos(abb.minX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
			vb.pos(abb.maxX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
			vb.pos(abb.maxX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
			vb.pos(abb.maxX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
			vb.pos(abb.maxX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
			vb.pos(abb.minX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
			vb.pos(abb.minX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
			ts.draw();
			vb.begin(7, DefaultVertexFormats.POSITION_COLOR);
			vb.pos(abb.maxX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
			vb.pos(abb.maxX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
			vb.pos(abb.minX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
			vb.pos(abb.minX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
			vb.pos(abb.minX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
			vb.pos(abb.minX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
			vb.pos(abb.maxX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
			vb.pos(abb.maxX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
			ts.draw();
			vb.begin(7, DefaultVertexFormats.POSITION_COLOR);
			vb.pos(abb.minX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
			vb.pos(abb.maxX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
			vb.pos(abb.maxX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
			vb.pos(abb.minX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
			vb.pos(abb.minX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
			vb.pos(abb.minX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
			vb.pos(abb.maxX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
			vb.pos(abb.maxX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
			ts.draw();
			vb.begin(7, DefaultVertexFormats.POSITION_COLOR);
			vb.pos(abb.minX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
			vb.pos(abb.maxX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
			vb.pos(abb.maxX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
			vb.pos(abb.minX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
			vb.pos(abb.minX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
			vb.pos(abb.minX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
			vb.pos(abb.maxX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
			vb.pos(abb.maxX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
			ts.draw();
			vb.begin(7, DefaultVertexFormats.POSITION_COLOR);
			vb.pos(abb.minX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
			vb.pos(abb.minX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
			vb.pos(abb.minX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
			vb.pos(abb.minX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
			vb.pos(abb.maxX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
			vb.pos(abb.maxX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
			vb.pos(abb.maxX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
			vb.pos(abb.maxX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
			ts.draw();
			vb.begin(7, DefaultVertexFormats.POSITION_COLOR);
			vb.pos(abb.minX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
			vb.pos(abb.minX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
			vb.pos(abb.minX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
			vb.pos(abb.minX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
			vb.pos(abb.maxX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
			vb.pos(abb.maxX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
			vb.pos(abb.maxX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
			vb.pos(abb.maxX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
			ts.draw();
		}

		public static void dtl(final Entity e, final int color, final float lw) {
			if (e != null) {
				final double x = e.lastTickPosX + (e.posX - e.lastTickPosX) * ReflectUtil.getTimer().renderPartialTicks
						- mc.getRenderManager().viewerPosX;
				final double y = e.getEyeHeight() + e.lastTickPosY
						+ (e.posY - e.lastTickPosY) * ReflectUtil.getTimer().renderPartialTicks
						- mc.getRenderManager().viewerPosY;
				final double z = e.lastTickPosZ + (e.posZ - e.lastTickPosZ) * ReflectUtil.getTimer().renderPartialTicks
						- mc.getRenderManager().viewerPosZ;
				final float a = (color >> 24 & 0xFF) / 255.0f;
				final float r = (color >> 16 & 0xFF) / 255.0f;
				final float g = (color >> 8 & 0xFF) / 255.0f;
				final float b = (color & 0xFF) / 255.0f;
				GL11.glPushMatrix();
				GL11.glEnable(3042);
				GL11.glEnable(2848);
				GL11.glDisable(2929);
				GL11.glDisable(3553);
				GL11.glBlendFunc(770, 771);
				GL11.glEnable(3042);
				GL11.glLineWidth(lw);
				GL11.glColor4f(r, g, b, a);
				GL11.glBegin(2);
				GL11.glVertex3d(0.0, (double) mc.thePlayer.getEyeHeight(), 0.0);
				GL11.glVertex3d(x, y, z);
				GL11.glEnd();
				GL11.glDisable(3042);
				GL11.glEnable(3553);
				GL11.glEnable(2929);
				GL11.glDisable(2848);
				GL11.glDisable(3042);
				GL11.glPopMatrix();
			}
		}

		public static void dGR(int left, int top, int right, int bottom, final int startColor, final int endColor) {
			if (left < right) {
				final int j = left;
				left = right;
				right = j;
			}
			if (top < bottom) {
				final int j = top;
				top = bottom;
				bottom = j;
			}
			final float f = (startColor >> 24 & 0xFF) / 255.0f;
			final float f2 = (startColor >> 16 & 0xFF) / 255.0f;
			final float f3 = (startColor >> 8 & 0xFF) / 255.0f;
			final float f4 = (startColor & 0xFF) / 255.0f;
			final float f5 = (endColor >> 24 & 0xFF) / 255.0f;
			final float f6 = (endColor >> 16 & 0xFF) / 255.0f;
			final float f7 = (endColor >> 8 & 0xFF) / 255.0f;
			final float f8 = (endColor & 0xFF) / 255.0f;
			GlStateManager.disableTexture2D();
			GlStateManager.enableBlend();
			GlStateManager.disableAlpha();
			GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
			GlStateManager.shadeModel(7425);
			final Tessellator tessellator = Tessellator.getInstance();
			final WorldRenderer worldrenderer = tessellator.getWorldRenderer();
			worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
			worldrenderer.pos((double) right, (double) top, 0.0).color(f2, f3, f4, f).endVertex();
			worldrenderer.pos((double) left, (double) top, 0.0).color(f2, f3, f4, f).endVertex();
			worldrenderer.pos((double) left, (double) bottom, 0.0).color(f6, f7, f8, f5).endVertex();
			worldrenderer.pos((double) right, (double) bottom, 0.0).color(f6, f7, f8, f5).endVertex();
			tessellator.draw();
			GlStateManager.shadeModel(7424);
			GlStateManager.disableBlend();
			GlStateManager.enableAlpha();
			GlStateManager.enableTexture2D();
		}

		public static void db(final int w, final int h, final int r) {
			final int c = (r == -1) ? -1089466352 : r;
			Gui.drawRect(0, 0, w, h, c);
		}

		public static void drawColouredText(final String text, final char lineSplit, int leftOffset, int topOffset,
				final long colourParam1, final long shift, final boolean rect, final FontRenderer fontRenderer) {
			final int bX = leftOffset;
			int l = 0;
			long colourControl = 0L;
			for (int i = 0; i < text.length(); ++i) {
				final char c = text.charAt(i);
				if (c == lineSplit) {
					++l;
					leftOffset = bX;
					topOffset += fontRenderer.FONT_HEIGHT + 5;
					colourControl = shift * l;
				} else {
					fontRenderer.drawString(String.valueOf(c), (float) leftOffset, (float) topOffset,
							Client.astolfoColorsDraw((int) colourParam1, (int) colourControl), rect);
					leftOffset += fontRenderer.getCharWidth(c);
					if (c != ' ') {
						colourControl -= 90L;
					}
				}
			}
		}

		public static void d2p(final double x, final double y, final int radius, final int sides, final int color) {
			final float a = (color >> 24 & 0xFF) / 255.0f;
			final float r = (color >> 16 & 0xFF) / 255.0f;
			final float g = (color >> 8 & 0xFF) / 255.0f;
			final float b = (color & 0xFF) / 255.0f;
			final Tessellator tessellator = Tessellator.getInstance();
			final WorldRenderer worldrenderer = tessellator.getWorldRenderer();
			GlStateManager.enableBlend();
			GlStateManager.disableTexture2D();
			GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
			GlStateManager.color(r, g, b, a);
			worldrenderer.begin(6, DefaultVertexFormats.POSITION);
			for (int i = 0; i < sides; ++i) {
				final double angle = 6.283185307179586 * i / sides + Math.toRadians(180.0);
				worldrenderer.pos(x + Math.sin(angle) * radius, y + Math.cos(angle) * radius, 0.0).endVertex();
			}
			tessellator.draw();
			GlStateManager.enableTexture2D();
			GlStateManager.disableBlend();
		}

		public static void d3p(final double x, final double y, final double z, final double radius, final int sides,
				final float lineWidth, final int color, final boolean chroma) {
			final float a = (color >> 24 & 0xFF) / 255.0f;
			final float r = (color >> 16 & 0xFF) / 255.0f;
			final float g = (color >> 8 & 0xFF) / 255.0f;
			final float b = (color & 0xFF) / 255.0f;
			mc.entityRenderer.disableLightmap();
			GL11.glDisable(3553);
			GL11.glEnable(3042);
			GL11.glBlendFunc(770, 771);
			GL11.glDisable(2929);
			GL11.glEnable(2848);
			GL11.glDepthMask(false);
			GL11.glLineWidth(lineWidth);
			if (!chroma) {
				GL11.glColor4f(r, g, b, a);
			}
			GL11.glBegin(1);
			long d = 0L;
			final long ed = 15000L / sides;
			final long hed = ed / 2L;
			for (int i = 0; i < sides * 2; ++i) {
				if (chroma) {
					if (i % 2 != 0) {
						if (i == 47) {
							d = hed;
						}
						d += ed;
					}
					final int c = Client.rainbowDraw(2L, d);
					final float r2 = (c >> 16 & 0xFF) / 255.0f;
					final float g2 = (c >> 8 & 0xFF) / 255.0f;
					final float b2 = (c & 0xFF) / 255.0f;
					GL11.glColor3f(r2, g2, b2);
				}
				final double angle = 6.283185307179586 * i / sides + Math.toRadians(180.0);
				GL11.glVertex3d(x + Math.cos(angle) * radius, y, z + Math.sin(angle) * radius);
			}
			GL11.glEnd();
			GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			GL11.glDepthMask(true);
			GL11.glDisable(2848);
			GL11.glEnable(2929);
			GL11.glDisable(3042);
			GL11.glEnable(3553);
			mc.entityRenderer.enableLightmap();
		}
	}

	public static class Modes {
		public enum ClickEvents {
			RENDER, TICK;
		}

		public enum BridgeMode {
			GODBRIDGE, MOONWALK, BREEZILY, NORMAL;
		}

		public enum ClickTimings {
			RAVEN, SKID;
		}

		public enum SprintResetTimings {
			PRE, POST;
		}
	}
}
