package io.github.ionianmc.ionian;

import net.fabricmc.api.ModInitializer;

public class IonianLaunch implements ModInitializer {
	static IonianLoader loader;

	@Override
	public void onInitialize() {
		loader = new IonianLoader();
		loader.discoverMods();
	}
}
