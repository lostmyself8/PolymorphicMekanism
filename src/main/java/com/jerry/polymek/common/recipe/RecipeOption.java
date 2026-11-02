package com.jerry.polymek.common.recipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** A currently matching recipe and its actual assembled output, used for server validation and previews. */
public record RecipeOption(ResourceLocation id, ItemStack output) {

    public RecipeOption {
        if (id == null || output == null || output.isEmpty()) {
            throw new IllegalArgumentException("A recipe option requires an id and a non-empty output");
        }
        output = output.copy();
    }
}