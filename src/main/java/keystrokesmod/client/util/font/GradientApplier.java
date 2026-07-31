package keystrokesmod.client.util.font;

import java.awt.Color;

@FunctionalInterface
public interface GradientApplier {
    Color colour(int i);
}