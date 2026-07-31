package keystrokesmod.client.utils.render;

public class AnimationUtil {

    public static float animate(float target, float current, float speed) {
        boolean larger = target > current;
        if (speed < 0.0F) {
            speed = 0.0F;
        } else if (speed > 1.0F) {
            speed = 1.0F;
        }

        float dif = Math.abs(target - current);
        float factor = dif * speed;
        if (factor < 0.1F) {
            factor = 0.1F;
        }

        if (larger) {
            current += factor;
            if (current > target) {
                current = target;
            }
        } else {
            current -= factor;
            if (current < target) {
                current = target;
            }
        }

        return current;
    }

    public static double easeOutQuad(double t) {
        return t * (2 - t);
    }

    public static double easeInOutQuad(double t) {
        return t < 0.5 ? 2 * t * t : -1 + (4 - 2 * t) * t;
    }

    public static double easeOutCubic(double t) {
        return (--t) * t * t + 1;
    }
}
