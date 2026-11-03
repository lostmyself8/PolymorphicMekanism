package com.jerry.polymek.common.registries;

import com.jerry.polymek.PolymorphicMekanism;
import com.jerry.polymek.compat.robit.RobitSmeltingChoice;
import java.util.List;
import java.util.Optional;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.impl.DataComponentDeferredRegister;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

/** Registers every Polymek data component through Mekanism's shared registration system. */
public final class PolymekDataComponents {

    private PolymekDataComponents() {
    }

    public static final DataComponentDeferredRegister DATA_COMPONENTS = new DataComponentDeferredRegister(PolymorphicMekanism.MOD_ID);

    /** Stores the exact encoded recipe, or an empty value for an unencoded formula item. */
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Optional<ResourceLocation>>> FORMULA_RECIPE = DATA_COMPONENTS.simple("formula_recipe",
          builder -> builder.persistent(ExtraCodecs.optionalEmptyMap(ResourceLocation.CODEC))
                .networkSynchronized(ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC)));

    /** Stores the selected furnace recipe when a Robit is carried as an item. */
    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<RobitSmeltingChoice>> ROBIT_SMELTING_CHOICE = DATA_COMPONENTS.simple("robit_smelting_choice",
          builder -> builder.persistent(RobitSmeltingChoice.CODEC).networkSynchronized(RobitSmeltingChoice.STREAM_CODEC));

    /** Keeps each QIO crafting window's persistent recipe choice under its own component. */
    private static final List<MekanismDeferredHolder<DataComponentType<?>, DataComponentType<ResourceLocation>>> QIO_RECIPES = List.of(
          registerQIORecipe(0), registerQIORecipe(1), registerQIORecipe(2));

    /** Returns the persisted recipe component belonging to one of QIO's three crafting windows. */
    public static DataComponentType<ResourceLocation> qioRecipeComponent(int index) {
        return QIO_RECIPES.get(index).get();
    }

    private static MekanismDeferredHolder<DataComponentType<?>, DataComponentType<ResourceLocation>> registerQIORecipe(int index) {
        return DATA_COMPONENTS.simple("qio_recipe_" + index,
              builder -> builder.persistent(ResourceLocation.CODEC).networkSynchronized(ResourceLocation.STREAM_CODEC));
    }
}