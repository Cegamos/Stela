package keystrokesmod.client.event.impl;

import keystrokesmod.client.event.Event;

public class DrawEvent extends Event {
    private final float partialTicks;

    public DrawEvent(float partialTicks) {
        this.partialTicks = partialTicks;
    }

    public float getPartialTicks() {
        return partialTicks;
    }
}
