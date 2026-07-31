package keystrokesmod.client.event.impl;

import keystrokesmod.client.event.Event;

public class DragEvent extends Event {
	public int mouseX, mouseY;
	public float partialTicks;
	
	public DragEvent(int mouseX, int mouseY, float partialTicks) {
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		this.partialTicks = partialTicks;
	}	
}
