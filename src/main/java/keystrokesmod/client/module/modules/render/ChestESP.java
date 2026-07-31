package keystrokesmod.client.module.modules.render;

import java.awt.Color;

import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.value.impl.NumberValue;
import keystrokesmod.client.module.value.impl.BooleanValue;
import keystrokesmod.client.util.Utils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@ModuleInfo(name = "ChestESP", category = Category.Render)
public class ChestESP extends Mod {
    private final NumberValue a = new NumberValue("Red", this, 0.0, 0.0, 255.0, 1.0);
    private final NumberValue b = new NumberValue("Green", this, 0.0, 0.0, 255.0, 1.0);
    private final NumberValue c = new NumberValue("Blue", this, 255.0, 0.0, 255.0, 1.0);
    private final BooleanValue d = new BooleanValue("Rainbow", this, false);

    @SubscribeEvent
    public void onRenderWorldLast(final RenderWorldLastEvent ev) {
        if (Utils.Player.isPlayerInGame()) {
            final int rgb = d.isToggled() ? Utils.Client.rainbowDraw(2L, 0L) : new Color((int)a.getInput(), (int)b.getInput(), (int)c.getInput()).getRGB();
            for (final TileEntity te : mc.theWorld.loadedTileEntityList) {
                if (te instanceof TileEntityChest || te instanceof TileEntityEnderChest) {
                    Utils.HUD.re(te.getPos(), rgb, true);
                }
            }
        }
    }
}
