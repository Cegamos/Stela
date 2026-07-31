package keystrokesmod.client.event.impl;

import keystrokesmod.client.event.Event;

public class PreRenderTickEvent extends Event {
	private final float partialTicks;
	
	public PreRenderTickEvent(float partialTicks) {
		this.partialTicks = partialTicks;
	}
	
	public float getPartialTicks() {
		return partialTicks;
	}
}
