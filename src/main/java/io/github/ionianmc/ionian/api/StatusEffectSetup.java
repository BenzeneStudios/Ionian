package io.github.ionianmc.ionian.api;

import io.github.ionianmc.loader.api.Logger;
import net.minecraft.entity.effect.StatusEffect;

public interface StatusEffectSetup extends Logger<StatusEffectSetup> {
	StatusEffectSetup effect(StatusEffect effect);
	StatusEffectSetup duration(int duration);
	StatusEffectSetup strength(int strength);
	StatusEffectSetup hideParticles();
	StatusEffectSetup ambient();
}
