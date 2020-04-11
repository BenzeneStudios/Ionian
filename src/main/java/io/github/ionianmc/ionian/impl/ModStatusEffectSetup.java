package io.github.ionianmc.ionian.impl;

import org.apache.logging.log4j.Logger;

import io.github.ionianmc.ionian.api.StatusEffectSetup;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;

public class ModStatusEffectSetup implements StatusEffectSetup {
	public ModStatusEffectSetup(Logger logger) {
		this.logger = logger;
	}

	private final Logger logger;
	private StatusEffect effect;
	private int duration = 30;
	private int strength = 1;
	private boolean hideParticles = false;
	private boolean ambient = false;

	@Override
	public StatusEffectSetup logInfo(String msg) {
		logger.info(msg);
		return this;
	}

	@Override
	public StatusEffectSetup logWarn(String msg) {
		logger.warn(msg);
		return this;
	}

	@Override
	public StatusEffectSetup logError(String msg) {
		logger.error(msg);
		return this;
	}

	@Override
	public StatusEffectSetup effect(StatusEffect effect) {
		this.effect = effect;
		return this;
	}

	@Override
	public StatusEffectSetup duration(int duration) {
		this.duration = duration;
		return this;
	}

	@Override
	public StatusEffectSetup strength(int strength) {
		this.strength = strength;
		return this;
	}

	@Override
	public StatusEffectSetup hideParticles() {
		this.hideParticles = true;
		return this;
	}

	@Override
	public StatusEffectSetup ambient() {
		this.ambient  = true;
		return this;
	}

	public StatusEffectInstance build() {
		return new StatusEffectInstance(
				this.effect,
				this.duration,
				this.strength,
				this.ambient,
				!this.hideParticles);
	}
}
