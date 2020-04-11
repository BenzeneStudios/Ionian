package io.github.ionianmc.ionian.impl;

import io.github.ionianmc.ionian.impl.ModRegistrySetup.ItemModel;

public final class JsonModels {
	public static String constructItemModel(ItemModel currentModel) {
		if (currentModel.modelType != null) {
			switch (currentModel.modelType) {
			case GENERATED:
				return new StringBuilder("{")
						.append("\"parent\": \"item/generated\",")
						.append("\"textures\": {")
						.append("\"layer0\": \"").append(currentModel.texture).append("\"}}")
						.toString();
			case HANDHELD:
				return new StringBuilder("{")
						.append("\"parent\": \"item/handheld\",")
						.append("\"textures\": {")
						.append("\"layer0\": \"").append(currentModel.texture).append("\"}}")
						.toString();
			default:
				throw new RuntimeException("Invalid Enum Model Type! Who's ASMing one in without adding impl? : " + currentModel.modelType.toString());
			}
		} else {
			return null;
		}
	}
}
