package com.jerry.polymek.compat.qio;

import com.jerry.polymek.PolymorphicMekanism;
import com.jerry.polymek.common.recipe.RecipeTargets;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import net.minecraft.resources.ResourceLocation;

/** Registers QIO's three independently persisted targets with the shared selection protocol. */
public final class QIOIntegration {

    public static final ResourceLocation KIND = PolymorphicMekanism.rl("qio");

    private QIOIntegration() {
    }

    /** Registers authoritative menu resolution; packets cannot target another user's inactive workbench. */
    public static void registerCommon() {
        RecipeTargets.register(KIND, (player, menu, index) -> {
            if (!(menu instanceof QIOItemViewerContainer qio) || index < 0 || index >= 3
                  || qio.getSelectedCraftingGrid(player.getUUID()) != index) {
                return null;
            }
            return (QIOCraftingTarget) qio.getCraftingWindow(index);
        });
    }
}
