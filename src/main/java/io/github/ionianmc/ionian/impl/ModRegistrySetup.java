package io.github.ionianmc.ionian.impl;

import java.util.ArrayList;
import java.util.List;
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
	}

	private final Function<String, Identifier> identifiers;
	private final Logger logger;
	private final Item item;

	public Item item() {
		return this.item;
	}

	public class Item implements InitialisedItemSetup {
		private Item() {
		}

		private ItemBuilder currentItem;
		private Identifier currentId;
		private ItemModel currentModel;
		private boolean dirty = false;

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
					}
					this.currentModel = null;
				}

				this.currentItem = null;
			}

			this.dirty = false;
		}

		@Override
		public InitialisedItemSetup newItem(String registryName) {
			if (this.dirty) {
				this.flush();
			}

			this.dirty = true;
			this.currentItem = new ItemBuilder(this.currentId = identifiers.apply(registryName));
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
			this.texture = buildResourceLocation(this.type, newTexture.toString().split(":"));
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

	static String buildResourceLocation(String type, String[] id) {
		String namespace = id[0];

		if (namespace.equals("minecraft")) {
			return type + "/" + id[1];
		} else {
			return namespace + ":" + type + "/" + id[1];
		}
	}
}
