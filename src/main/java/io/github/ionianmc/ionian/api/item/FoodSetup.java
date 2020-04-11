package io.github.ionianmc.ionian.api.item;

import java.util.function.Consumer;

import io.github.ionianmc.ionian.api.StatusEffectSetup;
import io.github.ionianmc.loader.api.Logger;

public interface FoodSetup extends Logger<FoodSetup> {
	FoodSetup hunger(int hunger);
	FoodSetup saturation(float saturation);
	FoodSetup wolfFood();
	FoodSetup alwaysEdible();
	FoodSetup eatFast();
	FoodSetup addStatusEffect(Consumer<StatusEffectSetup> statusEffectSetup);
}
