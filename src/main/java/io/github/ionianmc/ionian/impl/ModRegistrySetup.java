package io.github.ionianmc.ionian.impl;

import java.util.function.Function;

import org.apache.logging.log4j.Logger;

import io.github.ionianmc.ionian.api.ItemSetup;
import net.minecraft.util.Identifier;

public class ModRegistrySetup implements ItemSetup {
	public ModRegistrySetup(String modid, Logger logger) {
		this.identifiers = name -> new Identifier(modid, name);
		this.logger = logger;
	}

	private final Function<String, Identifier> identifiers;
	private final Logger logger;

	@Override
	public ModRegistrySetup logInfo(String msg) {
		logger.info(msg);
		return this;
	}

	@Override
	public ModRegistrySetup logWarn(String msg) {
		logger.warn(msg);
		return this;
	}

	@Override
	public ModRegistrySetup logError(String msg) {
		logger.error(msg);
		return this;
	}
}
