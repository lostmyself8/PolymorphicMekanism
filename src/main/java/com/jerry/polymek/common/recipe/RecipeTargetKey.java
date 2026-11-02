package com.jerry.polymek.common.recipe;

import net.minecraft.resources.ResourceLocation;

/** Identifies one independent recipe context within the currently open menu. */
public record RecipeTargetKey(ResourceLocation kind, int index) {

    public RecipeTargetKey {
        if (kind == null || index < 0) {
            throw new IllegalArgumentException("A recipe target requires a kind and a non-negative index");
        }
    }
}