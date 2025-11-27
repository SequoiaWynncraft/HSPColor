package net.warze.hspcolor.mixin.compat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "com.terraformersmc.modmenu.util.UpdateCheckerUtil", remap = false)
public abstract class ModMenuUpdateCheckerMixin {
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

    @Redirect(
            method = "checkForUpdates",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/Util;method_18349()Ljava/util/concurrent/ExecutorService;",
                    remap = true
            )
    )
    private static ExecutorService hspcolor$provideMissingExecutor() {
        return hspcolor$legacyUpdateExecutor;
    }
}
