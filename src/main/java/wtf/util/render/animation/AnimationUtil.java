package wtf.util.render.animation;

public class AnimationUtil {

    public static float animatedEyeHeight = 0f;

    public static float animate(float target, float current, float speed) {
        if (current == target) return current;
        boolean larger = target > current;
        if (speed < 0.0f) {
            speed = 0.0f;
        } else if (speed > 1.0f) {
            speed = 1.0f;
        }
        float dif = Math.max(target, current) - Math.min(target, current);
        float factor = dif * speed;
        if (factor < 0.01f) {
            factor = 0.01f;
        }
        if (larger) {
            current += factor;
            if (current >= target) current = target;
        } else {
            current -= factor;
            if (current <= target) current = target;
        }
        return current;
    }

    public static void setAnimatedEyeHeight(float eyeHeight) {
        animatedEyeHeight = eyeHeight;
    }
}
