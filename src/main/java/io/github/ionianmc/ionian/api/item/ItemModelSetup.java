package io.github.ionianmc.ionian.api.item;

import io.github.ionianmc.loader.api.Logger;

public interface ItemModelSetup extends Logger<ItemModelSetup> {
	ItemModelSetup type(ModelType type);
	ItemModelSetup overrideTexture(String newTexture);

	static enum ModelType {
		GENERATED,
		HANDHELD
	}
}
