package io.github.ionianmc.ionian.impl.extensions;

import java.util.function.Function;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ItemBuilder extends Item.Settings {
	public ItemBuilder(Identifier id) {
		this.id = id;
	}

	private final Identifier id;

	public <T extends Item> T build(Function<ItemBuilder, T> constructor) {
		return Registry.register(Registry.ITEM, this.id, constructor.apply(this));
	}
}
