package com.jerry.polymek.compat.qio;

import com.jerry.polymek.PolymorphicMekanism;
import com.jerry.polymek.common.recipe.RecipeTargets;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import net.minecraft.resources.ResourceLocation;

/** Registers QIO's three independently persisted targets with the shared selection protocol. */
public final class QioIntegration {

    public static final ResourceLocation KIND = PolymorphicMekanism.rl("qio");

    private QioIntegration() {
    }

    /** Registers authoritative menu resolution; packets cannot target another user's inactive workbench. */
    public static void registerCommon() {
        RecipeTargets.register(KIND, (player, menu, index) -> {
            if (!(menu instanceof QIOItemViewerContainer qio) || index < 0 || index >= 3
                  || qio.getSelectedCraftingGrid(player.getUUID()) != index) {
                return null;
            }
            return (QioCraftingTarget) qio.getCraftingWindow(index);
        });
    }

    /** Loads client-only classes only from the client setup path. */
    public static void registerClient() {
        com.jerry.polymek.client.qio.QioClientIntegration.register();
    }
}
