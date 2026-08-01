package wtf.event.impl;

import wtf.event.Event;

public class RenderWorldLastEvent extends Event {
    private final float partialTicks;

    public RenderWorldLastEvent(float partialTicks) {
        this.partialTicks = partialTicks;
    }

    public float getPartialTicks() {
        return partialTicks;
    }
}
