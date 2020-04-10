package io.github.ionianmc.loader.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.ionianmc.loader.api.IonianModSetup;

public class ModConstructionDevice implements IonianModSetup {
	public ModConstructionDevice(String modid) {
		this.logger = LogManager.getLogger(modid.replace('_', ' '));
	}

	private final Logger logger;

	@Override
	public ModConstructionDevice logInfo(String msg) {
		logger.info(msg);
		return this;
	}

	@Override
	public ModConstructionDevice logWarn(String msg) {
		logger.warn(msg);
		return this;
	}

	@Override
	public ModConstructionDevice logError(String msg) {
		logger.error(msg);
		return this;
	}
}
