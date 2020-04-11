package io.github.ionianmc.testmod;

import io.github.ionianmc.ionian.api.item.ItemModelSetup.ModelType;
import io.github.ionianmc.loader.api.IonianModSetup;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;

public class IonianTestMod {
	public static void setup(IonianModSetup setup) {
		setup.logInfo("Hello, Ionian World!")
		.itemSetup(itemSetup -> itemSetup
				.newItem("test")
				.creativeTab(ItemGroup.BREWING)
				.model(modelSetup -> modelSetup
						.type(ModelType.GENERATED)
						.overrideTexture(new Identifier("stick"))));
	}
}
