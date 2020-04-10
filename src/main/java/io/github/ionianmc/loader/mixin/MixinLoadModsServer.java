package io.github.ionianmc.loader.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import io.github.ionianmc.loader.IonianLaunch;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;

@Mixin(MinecraftDedicatedServer.class)
public class MixinLoadModsServer {
	@Inject(at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;info(Ljava/lang/String;)V", ordinal = 0), method = "setupServer")
	private void loadModsServer() {
		IonianLaunch.loadMods();
	}
}
