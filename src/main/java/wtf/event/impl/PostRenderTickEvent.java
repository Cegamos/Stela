package wtf.event.impl;

import wtf.event.Event;

public class PostRenderTickEvent extends Event {
	private final float partialTicks;
	
	public PostRenderTickEvent(float partialTicks) {
		this.partialTicks = partialTicks;
	}
	
	public float getPartialTicks() {
		return partialTicks;
	}
}
