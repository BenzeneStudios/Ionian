package io.github.ionianmc.ionian.api.item;

import io.github.ionianmc.loader.api.Logger;
import net.minecraft.util.Identifier;

public interface ItemModelSetup extends Logger<ItemModelSetup> {
	ItemModelSetup type(ModelType type);
	ItemModelSetup overrideTexture(Identifier newTexture);

	static enum ModelType {
		GENERATED,
		HANDHELD
	}
}
