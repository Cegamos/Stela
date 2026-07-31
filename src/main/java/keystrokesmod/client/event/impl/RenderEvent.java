package keystrokesmod.client.event.impl;

import keystrokesmod.client.event.Event;

public class RenderEvent extends Event {
    private final float partialTicks;

    public RenderEvent(float partialTicks) {
        this.partialTicks = partialTicks;
    }

    public float getPartialTicks() {
        return partialTicks;
    }
}
