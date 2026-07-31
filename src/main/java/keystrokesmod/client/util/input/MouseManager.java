package keystrokesmod.client.util.input;

import java.util.ArrayList;
import java.util.List;

import keystrokesmod.client.event.EventLink;
import keystrokesmod.client.event.Listener;
import keystrokesmod.client.event.impl.MouseEvent;
import keystrokesmod.client.event.impl.RenderTextEvent;
import keystrokesmod.client.util.IMinecraft;
import keystrokesmod.client.util.Utils;

public class MouseManager implements IMinecraft {
    private static final List<Long> leftClicks = new ArrayList<>();
    private static final List<Long> rightClicks = new ArrayList<>();
    public static long leftClickTimer = 0;
    public static long rightClickTimer = 0;
    
    @EventLink
    public final Listener<MouseEvent> onMouseUpdate = mouse -> {
        if (mouse.getButton() == 0) {
            addLeftClick();
        } else if (mouse.getButton() == 1) {
            addRightClick();
        }
    };
    
    @EventLink
    public final Listener<RenderTextEvent> renderTextEvent = event -> {
        if (event.getText() != null) {
            event.setText(event.getText().replace("§k", ""));
        }
    };
    
    public static void addLeftClick() {
        leftClicks.add(leftClickTimer = System.currentTimeMillis());
    }
    
    public static void addRightClick() {
        rightClicks.add(rightClickTimer = System.currentTimeMillis());
    }
    
    public static int getLeftClickCounter() {
        if (!Utils.Player.isPlayerInGame()) return leftClicks.size();
        leftClicks.removeIf(lon -> lon < System.currentTimeMillis() - 1000L);
        return leftClicks.size();
    }
    
    public static int getRightClickCounter() {
        if (!Utils.Player.isPlayerInGame()) return rightClicks.size();
        rightClicks.removeIf(lon -> lon < System.currentTimeMillis() - 1000L);
        return rightClicks.size();
    }
}
