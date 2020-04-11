package io.github.ionianmc.loader.impl;

import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.ionianmc.ionian.api.ItemSetup;
import io.github.ionianmc.ionian.impl.ModRegistrySetup;
import io.github.ionianmc.loader.api.IonianModSetup;

public class ModConstructionDevice implements IonianModSetup {
	public ModConstructionDevice(String modid) {
		this.logger = LogManager.getLogger(modid.replace('_', ' '));
		this.registrySetup = new ModRegistrySetup(modid, this.logger);
	}

	private final Logger logger;
	private final ModRegistrySetup registrySetup;

	@Override
	public IonianModSetup logInfo(String msg) {
		logger.info(msg);
		return this;
	}

	@Override
	public IonianModSetup logWarn(String msg) {
		logger.warn(msg);
		return this;
	}

	@Override
	public IonianModSetup logError(String msg) {
		logger.error(msg);
		return this;
	}

	@Override
	public IonianModSetup itemSetup(Consumer<ItemSetup> setup) {
		setup.accept(this.registrySetup);
		return this;
	}
}
