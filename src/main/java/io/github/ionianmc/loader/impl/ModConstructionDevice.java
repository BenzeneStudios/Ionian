package io.github.ionianmc.loader.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.ionianmc.ionian.api.item.ItemSetup;
import io.github.ionianmc.ionian.impl.ModRegistrySetup;
import io.github.ionianmc.loader.api.IonianModSetup;

public class ModConstructionDevice implements IonianModSetup {
	public ModConstructionDevice(String modid) {
		this.logger = LogManager.getLogger(modid.replace('_', ' '));
		this.registrySetup = new ModRegistrySetup(modid, this.logger);
	}

	private final Logger logger;
	private final ModRegistrySetup registrySetup;
	private final List<Consumer<ItemSetup<?>>> itemSetups = new ArrayList<>();

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
	public IonianModSetup itemSetup(Consumer<ItemSetup<?>> setup) {
		this.itemSetups.add(setup);
		return this;
	}

	public void setupItems() {
		this.itemSetups.forEach(setup -> setup.accept(this.registrySetup.item()));
		this.registrySetup.item().flush();
		this.registrySetup.item().addLangKeys();
	}
}
