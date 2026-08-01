package wtf.util.player;

import net.minecraft.entity.EntityLivingBase;
import wtf.handler.ClickHandler;
import wtf.module.modules.Mod;
import wtf.module.value.impl.BooleanValue;
import wtf.module.value.impl.ModeValue;
import wtf.module.value.impl.NumberValue;
import wtf.util.IMinecraft;

public class ClickManager implements IMinecraft {
    public ModeValue clickMode;
    public final NumberValue cps;
    public final NumberValue maxSkipsInARow;
    public BooleanValue rayTrace;
    public BooleanValue failSwing;
    public final BooleanValue respectHitDelay;
    public final NumberValue maxDelay;
    public final BooleanValue doubleClick;
    public final NumberValue maxClickAmount;
    public final NumberValue maxConcentrationDiff;
    public final NumberValue consistency;
    public final boolean isAutoClicker;
    public final NumberValue perlinRandAdd;

    public ClickManager(Mod module) {
        isAutoClicker = module.getName().equalsIgnoreCase("LeftClicker") || module.getName().equalsIgnoreCase("AutoClicker");

        cps = new NumberValue("CPS", module, 13.0, 1.0, 60.0, 0.5);

        if (!isAutoClicker) {
            clickMode = new ModeValue("Click mode", module, "Packet", "Legit", "Packet", "PlayerController");
        }

        doubleClick = new BooleanValue("Double click", module, false);
        maxClickAmount = new NumberValue("Max click amount", module, 6.0, 1.0, 10.0, 1.0, () -> doubleClick.getValue());

        perlinRandAdd = new NumberValue("Perlin rand add", module, 1.0, 0.0, 5.0, 0.1);

        maxSkipsInARow = new NumberValue("Max skips in a row", module, 2.0, 0.0, 5.0, 1.0);
        consistency = new NumberValue("Consistency", module, 0.5, 0.0, 1.0, 0.01);
        maxConcentrationDiff = new NumberValue("Max concentration diff", module, 2.0, 0.0, 20.0, 1.0);
        respectHitDelay = new BooleanValue("Respect hit delay", module, false);
        maxDelay = new NumberValue("Max delay", module, 10.0, 1.0, 10.0, 1.0, () -> respectHitDelay.getValue());

        if (!isAutoClicker) {
            rayTrace = new BooleanValue("Raytrace", module, false);
            failSwing = new BooleanValue("Fail swing", module, false);
        }
    }

    public void click(float attackRange, EntityLivingBase target) {
        ClickHandler.ClickMode mode = ClickHandler.ClickMode.valueOf(isAutoClicker ? "Legit" : clickMode.getMode());
        ClickHandler.initHandler(
                (float) cps.getValue(),
                perlinRandAdd.getValue(),
                !isAutoClicker && rayTrace != null && rayTrace.getValue(),
                !isAutoClicker && failSwing != null && failSwing.getValue(),
                respectHitDelay.getValue(),
                (int) maxDelay.getValue(),
                attackRange,
                doubleClick.getValue(),
                (float) maxClickAmount.getValue(),
                (int) maxSkipsInARow.getValue(),
                (int) maxConcentrationDiff.getValue(),
                (float) consistency.getValue(),
                mode,
                isAutoClicker ? mc.thePlayer : target
        );
    }
}
