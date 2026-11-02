package com.jerry.polymek.compat.assemblicator;

import com.jerry.polymek.PolymorphicMekanism;
import com.jerry.polymek.common.recipe.RecipeTarget;
import com.jerry.polymek.common.recipe.RecipeTargets;
import mekanism.common.inventory.container.tile.FormulaicAssemblicatorContainer;
import net.minecraft.resources.ResourceLocation;

/** Binds formula selections to the exact authorized machine behind the currently open native menu. */
public final class AssemblicatorIntegration {

    public static final ResourceLocation KIND = PolymorphicMekanism.rl("assemblicator");

    private AssemblicatorIntegration() {
    }

    /** Registers the machine adapter; no position supplied by a client can select a different block. */
    public static void registerCommon() {
        RecipeTargets.register(KIND, (player, menu, index) -> {
            if (index != 0 || !(menu instanceof FormulaicAssemblicatorContainer container)) {
                return null;
            }
            var tile = container.getTileEntity();
            return tile.getLevel() == player.level() && !tile.isRemoved() && player.distanceToSqr(tile.getBlockPos().getCenter()) <= 64
                    ? (RecipeTarget) tile : null;
        });
    }
}
