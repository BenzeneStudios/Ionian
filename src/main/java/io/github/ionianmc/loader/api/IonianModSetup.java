package io.github.ionianmc.loader.api;

import java.util.function.Consumer;

import io.github.ionianmc.ionian.api.item.ItemSetup;

public interface IonianModSetup extends Logger<IonianModSetup> {
	IonianModSetup itemSetup(Consumer<ItemSetup<?>> setup);
}
