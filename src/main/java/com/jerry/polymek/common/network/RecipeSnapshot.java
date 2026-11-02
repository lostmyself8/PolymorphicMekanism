package com.jerry.polymek.common.network;

import com.jerry.polymek.common.recipe.RecipeOption;
import com.jerry.polymek.common.recipe.RecipeTargetKey;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;

/** An authoritative response scoped to the exact open menu and independent recipe target. */
public record RecipeSnapshot(int containerId, RecipeTargetKey target, List<RecipeOption> options,
                             Optional<ResourceLocation> selected, boolean editable) {

    public RecipeSnapshot {
        options = List.copyOf(options);
    }
}
