package io.github.ionianmc.ionian.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.logging.log4j.Logger;

import io.github.ionianmc.ionian.api.StatusEffectSetup;
import io.github.ionianmc.ionian.api.item.FoodSetup;
import io.github.ionianmc.ionian.api.item.InitialisedItemSetup;
import io.github.ionianmc.ionian.api.item.ItemModelSetup;
import io.github.ionianmc.ionian.impl.extensions.ItemBuilder;
import net.devtech.rrp.api.RuntimeResourcePack;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;

public class ModRegistrySetup {
	public ModRegistrySetup(String modid, Logger logger) {
		this.identifiers = name -> name.contains(":") ? new Identifier(name) : new Identifier(modid, name);
		this.logger = logger;
		this.item = new Item();
		this.modid = modid;
	}

	private final Function<String, Identifier> identifiers;
	private final Logger logger;
	private final Item item;
	private final String modid;

	public Item item() {
		return this.item;
	}

	public class Item implements InitialisedItemSetup {
		private Item() {
		}

		private ItemBuilder currentItem;
		private Identifier currentId;
		private ItemModel currentModel;
		private String localisedName;
		private String langKey;
		private boolean dirty = false;
		private final Map<String, Map<String, String>> language = new HashMap<>();

		@Override
		public InitialisedItemSetup logInfo(String msg) {
			logger.info(msg);
			return this;
		}

		@Override
		public InitialisedItemSetup logWarn(String msg) {
			logger.warn(msg);
			return this;
		}

		@Override
		public InitialisedItemSetup logError(String msg) {
			logger.error(msg);
			return this;
		}

		public void flush() {
			if (this.currentItem != null) {
				this.currentItem.build(net.minecraft.item.Item::new);

				if (this.currentModel != null) {
					String itemModelJson = JsonModels.constructItemModel(this.currentModel);

					if (itemModelJson != null) {
						RuntimeResourcePack.INSTANCE.addItemModel(this.currentId, itemModelJson);
						this.getLanguage("en_us").put(this.langKey, this.localisedName);
					}

					this.currentModel = null;
				}

				this.currentItem = null;
			}

			this.dirty = false;
		}

		public void addLangKeys() {
			this.language.forEach((language, map) -> RuntimeResourcePack.INSTANCE.addLangFile(modid, language, map));
		}

		@Override
		public InitialisedItemSetup newItem(String registryName) {
			if (this.dirty) {
				this.flush();
			}

			this.dirty = true;
			this.currentItem = new ItemBuilder(this.currentId = identifiers.apply(registryName));
			this.localisedName = generatedLangValue(this.currentId);
			this.langKey = langKey("item", this.currentId);
			return this;
		}

		@Override
		public InitialisedItemSetup food(Consumer<FoodSetup> foodSetup) {
			Food food = new Food();
			foodSetup.accept(food);
			FoodComponent.Builder builder = new FoodComponent.Builder()
					.hunger(food.hunger)
					.saturationModifier(food.saturation);

			if (food.alwaysEdible) {
				builder.alwaysEdible();
			}

			if (food.eatFast) {
				builder.snack();
			}

			if (food.wolfFood) {
				builder.meat();
			}

			this.currentItem.food(builder.build());
			return this;
		}

		@Override
		public InitialisedItemSetup creativeTab(ItemGroup tab) {
			this.currentItem.group(tab);
			return this;
		}

		@Override
		public InitialisedItemSetup durability(int durability) {
			this.currentItem.maxDamage(durability);
			return this;
		}

		@Override
		public InitialisedItemSetup stackSize(int stackSize) {
			this.currentItem.maxCount(stackSize);
			return this;
		}

		@Override
		public InitialisedItemSetup model(Consumer<ItemModelSetup> modelSetup) {
			ItemModel model = new ItemModel(this.currentId, "item");
			modelSetup.accept(model);
			this.currentModel = model;
			return this;
		}

		@Override
		public InitialisedItemSetup localisedName(String language, String name) {
			language = language.toLowerCase(Locale.ROOT);

			if (language.equals("en_us")) {
				this.localisedName = name;
			} else {
				this.getLanguage(language).put(this.langKey, name);
			}
			return this;
		}

		private Map<String, String> getLanguage(String language) {
			return this.language.computeIfAbsent(language, lang -> new HashMap<>());
		}
	}

	public class ItemModel implements ItemModelSetup {
		private ItemModel(Identifier id, String type) {
			this.type = type;
			this.overrideTexture(id);
		}

		ModelType modelType;
		String texture;
		final String type;

		@Override
		public ItemModelSetup logInfo(String msg) {
			logger.info(msg);
			return this;
		}

		@Override
		public ItemModelSetup logWarn(String msg) {
			logger.warn(msg);
			return this;
		}

		@Override
		public ItemModelSetup logError(String msg) {
			logger.error(msg);
			return this;
		}

		@Override
		public ItemModelSetup type(ModelType type) {
			this.modelType = type;
			return this;
		}

		@Override
		public ItemModelSetup overrideTexture(Identifier newTexture) {
			this.texture = buildResourceLocation(this.type, newTexture.getNamespace(), newTexture.getPath());
			return this;
		}
	}

	public class Food implements FoodSetup {
		private Food() {
		}

		private int hunger = 1;
		private float saturation = 0.1f;
		private boolean wolfFood = false;
		private boolean alwaysEdible = false;
		private boolean eatFast = false;
		private final List<StatusEffectInstance> statusEffects = new ArrayList<>();

		@Override
		public FoodSetup logInfo(String msg) {
			logger.info(msg);
			return this;
		}

		@Override
		public FoodSetup logWarn(String msg) {
			logger.warn(msg);
			return this;
		}

		@Override
		public FoodSetup logError(String msg) {
			logger.error(msg);
			return this;
		}

		@Override
		public FoodSetup hunger(int hunger) {
			this.hunger = hunger;
			return this;
		}

		@Override
		public FoodSetup saturation(float saturation) {
			this.saturation = saturation;
			return this;
		}

		@Override
		public FoodSetup wolfFood() {
			this.wolfFood = true;
			return this;
		}

		@Override
		public FoodSetup alwaysEdible() {
			this.alwaysEdible = true;
			return this;
		}

		@Override
		public FoodSetup eatFast() {
			this.eatFast = true;
			return this;
		}

		@Override
		public FoodSetup addStatusEffect(Consumer<StatusEffectSetup> statusEffectSetup) {
			ModStatusEffectSetup se = new ModStatusEffectSetup(logger);
			statusEffectSetup.accept(se);
			this.statusEffects.add(se.build());
			return this;
		}
	}

	static String buildResourceLocation(String type, String namespace, String path) {
		if (namespace.equals("minecraft")) {
			return type + "/" + path;
		} else {
			return namespace + ":" + type + "/" + path;
		}
	}

	static String langKey(String type, Identifier id) {
		return type + "." + id.getNamespace() + "." + id.getPath();
	}

	static String generatedLangValue(Identifier id) {
		String idName = id.getPath();

		StringBuilder sb = new StringBuilder();
		boolean capital = true;

		for (char c : idName.toCharArray()) {
			if (c == '_') {
				sb.append(" ");
				capital = true;
			} else if (capital) {
				sb.append(Character.toUpperCase(c));
				capital = false;
			} else {
				sb.append(c);
			}
		}

		return sb.toString();
	}
}
