package keystrokesmod.client.event.impl;

import keystrokesmod.client.event.Event;

public class PostRenderTickEvent extends Event {
	private final float partialTicks;
	
	public PostRenderTickEvent(float partialTicks) {
		this.partialTicks = partialTicks;
	}
	
	public float getPartialTicks() {
		return partialTicks;
	}
}
