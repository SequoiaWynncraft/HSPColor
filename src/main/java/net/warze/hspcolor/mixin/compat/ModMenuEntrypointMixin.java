package net.warze.hspcolor.mixin.compat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "com.terraformersmc.modmenu.ModMenu", remap = false)
public abstract class ModMenuEntrypointMixin {
    @Redirect(
            method = "onInitializeClient",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/terraformersmc/modmenu/ModMenu;checkForUpdates()V"
            )
    )
    private void hspcolor$skipBrokenUpdateCheck() {
        // Mod Menu 11.0.0 still invokes Minecraft's removed Util.method_18349.
        // Skipping the update checker avoids the crash without affecting gameplay.
    }
}
