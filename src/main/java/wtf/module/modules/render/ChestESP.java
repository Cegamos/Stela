package wtf.module.modules.render;

import java.awt.Color;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import wtf.module.Category;
import wtf.module.ModuleInfo;
import wtf.module.modules.Mod;
import wtf.module.value.impl.BooleanValue;
import wtf.module.value.impl.NumberValue;
import wtf.util.Utils;

@ModuleInfo(name = "ChestESP", category = Category.Render)
public class ChestESP extends Mod {
    private final NumberValue a = new NumberValue("Red", this, 0.0, 0.0, 255.0, 1.0);
    private final NumberValue b = new NumberValue("Green", this, 0.0, 0.0, 255.0, 1.0);
    private final NumberValue c = new NumberValue("Blue", this, 255.0, 0.0, 255.0, 1.0);
    private final BooleanValue d = new BooleanValue("Rainbow", this, false);

    @SubscribeEvent
    public void onRenderWorldLast(final RenderWorldLastEvent ev) {
        if (Utils.Player.isPlayerInGame()) {
            final int rgb = d.getValue() ? Utils.Client.rainbowDraw(2L, 0L) : new Color((int)a.getValue(), (int)b.getValue(), (int)c.getValue()).getRGB();
            for (final TileEntity te : mc.theWorld.loadedTileEntityList) {
                if (te instanceof TileEntityChest || te instanceof TileEntityEnderChest) {
                    Utils.HUD.re(te.getPos(), rgb, true);
                }
            }
        }
    }
}
