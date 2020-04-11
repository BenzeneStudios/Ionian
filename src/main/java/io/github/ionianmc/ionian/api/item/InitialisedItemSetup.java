package io.github.ionianmc.ionian.api.item;

import java.util.function.Consumer;

import io.github.ionianmc.ionian.api.Localisation;
import net.minecraft.item.ItemGroup;

public interface InitialisedItemSetup extends ItemSetup<InitialisedItemSetup>, Localisation<InitialisedItemSetup> {
	InitialisedItemSetup food(Consumer<FoodSetup> foodSetup);
	InitialisedItemSetup creativeTab(ItemGroup group);
	InitialisedItemSetup durability(int durability);
	InitialisedItemSetup stackSize(int stackSize);
	InitialisedItemSetup model(Consumer<ItemModelSetup> modelSetup);
}
