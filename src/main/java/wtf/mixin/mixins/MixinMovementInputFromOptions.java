package wtf.mixin.mixins;

import net.minecraft.util.MovementInput;
import net.minecraft.util.MovementInputFromOptions;
import wtf.event.EventBus;
import wtf.event.impl.PostInputEvent;
import wtf.event.impl.PreInputEvent;
import wtf.stela.annotations.Inject;
import wtf.stela.annotations.Mixin;
import wtf.stela.annotations.Shadow;
import wtf.stela.annotations.Target;

@Mixin(MovementInputFromOptions.class)
public class MixinMovementInputFromOptions extends MovementInput {
    @Shadow
    public float moveStrafe;

    @Shadow
    public float moveForward;

    @Shadow
    public boolean jump;

    @Shadow
    public boolean sneak;

    @Inject(method = "updatePlayerMoveState", desc = "()V", target = @Target(value = "PUTFIELD", target = "net/minecraft/util/MovementInputFromOptions.sneak Z", shift = Target.Shift.AFTER))
    public void updatePlayerMoveState() {
        PreInputEvent event = new PreInputEvent(this.moveForward, this.moveStrafe, this.jump, this.sneak);
        EventBus.INSTANCE.post(event);
        this.moveForward = event.getForward();
        this.moveStrafe = event.getStrafe();
        this.jump = event.isJump();
        this.sneak = event.isSneak();
    }

    @Inject(method = "updatePlayerMoveState", desc = "()V", target = @Target("TAIL"))
    private void onUpdatePlayerMoveState() {
    	EventBus.INSTANCE.post(new PostInputEvent());
    }
}