package keystrokesmod.client.event.impl;

import keystrokesmod.client.event.Event;

public class MouseEvent extends Event {
    private final int button;
    private final boolean buttonstate;
    
	public MouseEvent(int button, boolean buttonstate) {
		this.button = button;
		this.buttonstate = buttonstate;
	}

	public int getButton() {
		return button;
	}

	public boolean isButtonstate() {
		return buttonstate;
	}
}
