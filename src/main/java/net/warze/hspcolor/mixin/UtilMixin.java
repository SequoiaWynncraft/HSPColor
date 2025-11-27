package net.warze.hspcolor.mixin;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Util.class)
public abstract class UtilMixin {
    @Unique
    private static final ExecutorService hspcolor$legacyUpdateExecutor = Executors.newCachedThreadPool(new ThreadFactory() {
        private int hspcolor$threadId = 1;

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "LegacyModMenuUpdate-" + hspcolor$threadId++);
            thread.setDaemon(true);
            return thread;
        }
    });

    public static ExecutorService method_18349() {
        return hspcolor$legacyUpdateExecutor;
    }
}
