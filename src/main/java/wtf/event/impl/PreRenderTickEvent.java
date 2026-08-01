package wtf.event.impl;

import wtf.event.Event;

public class PreRenderTickEvent extends Event {
	private final float partialTicks;
	
	public PreRenderTickEvent(float partialTicks) {
		this.partialTicks = partialTicks;
	}
	
	public float getPartialTicks() {
		return partialTicks;
	}
}
