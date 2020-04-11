package io.github.ionianmc.loader.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.systems.RenderSystem;

import io.github.ionianmc.loader.IonianLaunch;

@Mixin(remap = false, value = RenderSystem.class)
public class MixinInitModsClient {
	@Inject(at = @At("HEAD"), method = "setupDefaultState", cancellable = true)
	private static void loadModsClient(int x, int y, int width, int height, CallbackInfo info) {
		IonianLaunch.loadMods();
	}
}
