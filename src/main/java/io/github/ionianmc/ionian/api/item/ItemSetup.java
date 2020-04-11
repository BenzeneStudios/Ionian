package io.github.ionianmc.ionian.api.item;

import io.github.ionianmc.loader.api.Logger;

public interface ItemSetup<T extends ItemSetup<T>> extends Logger<T> {
	InitialisedItemSetup newItem(String registryName);
}
