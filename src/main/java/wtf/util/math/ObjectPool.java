package wtf.util.math;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;
import net.minecraft.util.BlockPos;

public final class ObjectPool<T> {

    public static final ObjectPool<BlockPos.MutableBlockPos> MUTABLE_BLOCK_POS = new ObjectPool<>(BlockPos.MutableBlockPos::new);

    private final ConcurrentLinkedQueue<T> queue = new ConcurrentLinkedQueue<>();
    private final Supplier<T> supplier;

    public ObjectPool(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public T get() {
        T value = queue.poll();
        return value != null ? value : supplier.get();
    }

    public void offer(T value) {
        queue.offer(value);
    }
}
