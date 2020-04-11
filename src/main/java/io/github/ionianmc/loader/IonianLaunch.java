package io.github.ionianmc.loader;

import io.github.ionianmc.loader.impl.ModConstructionDevice;
import net.fabricmc.api.ModInitializer;

public class IonianLaunch implements ModInitializer {
	private static IonianLoader loader;

	@Override
	public void onInitialize() {
		IonianLoader.LOGGER.info("Boostrapping ionian loader!");
		loader = new IonianLoader();
		loader.discoverMods();
	}

	public static void loadMods() {
		IonianLoader.LOGGER.info("Loading discovered mods.");
		loader.setupMods(ModConstructionDevice::new);
		loader.registerModContent();
	}
}
