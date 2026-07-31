package keystrokesmod.client.event.impl;

import keystrokesmod.client.event.Event;

public class MouseEvent extends Event {
    private final int button;

    public MouseEvent(int button) {
        this.button = button;
    }

    public int getButton() {
        return button;
    }
}
