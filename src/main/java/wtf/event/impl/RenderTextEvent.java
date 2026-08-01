package wtf.event.impl;

import wtf.event.Event;

public class RenderTextEvent extends Event {
    private String text;

    public RenderTextEvent(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
