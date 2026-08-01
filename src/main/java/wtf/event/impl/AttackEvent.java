package wtf.event.impl;

import net.minecraft.entity.Entity;
import wtf.event.Event;

public class AttackEvent extends Event {
    private final Entity target;

    public AttackEvent(Entity target) {
        this.target = target;
    }

    public Entity getTarget() {
        return target;
    }
}
