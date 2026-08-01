package keystrokesmod.client.util.math;

import java.lang.reflect.Field;
import net.minecraft.util.MathHelper;

/**
 * A replacement for the sine angle lookup table used in {@link MathHelper}, both reducing the size of LUT and improving
 * the access patterns for common paired sin/cos operations.
 * Reduced from 64K entries (256 KB) to 16K entries (64 KB) to fit CPU L1/L2 caches while returning bit-for-bit identical results to Vanilla.
 */
public class CompactSineLUT {
    private static final int[] SINE_TABLE_INT = new int[16384 + 1];
    private static final float SINE_TABLE_MIDPOINT;

    static {
        float[] SINE_TABLE;
        try {
            Field field;
            try {
                field = MathHelper.class.getDeclaredField("SIN_TABLE");
            } catch (NoSuchFieldException e) {
                field = MathHelper.class.getDeclaredField("field_76144_a");
            }
            field.setAccessible(true);
            SINE_TABLE = (float[]) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            SINE_TABLE = new float[65536];
            for (int i = 0; i < 65536; ++i) {
                SINE_TABLE[i] = (float) Math.sin((double) i * Math.PI * 2.0 / 65536.0);
            }
        }
        for (int i = 0; i < SINE_TABLE_INT.length; i++) {
            SINE_TABLE_INT[i] = Float.floatToRawIntBits(SINE_TABLE[i]);
        }

        SINE_TABLE_MIDPOINT = SINE_TABLE[SINE_TABLE.length / 2];
    }

    public static float sin(float f) {
        return lookup((int) (f * 10430.378f) & 0xFFFF);
    }

    public static float cos(float f) {
        return lookup((int) (f * 10430.378f + 16384.0f) & 0xFFFF);
    }

    private static float lookup(int index) {
        if (index == 32768) {
            return SINE_TABLE_MIDPOINT;
        }

        int neg = (index & 0x8000) << 16;
        int mask = (index << 17) >> 31;
        int pos = (0x8001 & mask) + (index ^ mask);
        pos &= 0x7fff;

        return Float.intBitsToFloat(SINE_TABLE_INT[pos] ^ neg);
    }
}
