package com.jerry.polymek.compat.robit;

import com.jerry.polymek.PolymorphicMekanism;
import com.jerry.polymek.common.recipe.RecipeTargets;
import mekanism.api.security.IEntitySecurityUtils;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.entity.robit.CraftingRobitContainer;
import mekanism.common.inventory.container.entity.robit.RobitContainer;
import mekanism.common.registries.MekanismContainerTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/** Registers the Robit's distinct menu workbench and entity furnace selection contexts. */
public final class RobitIntegration {

    public static final ResourceLocation CRAFTING = PolymorphicMekanism.rl("robit_crafting");
    public static final ResourceLocation SMELTING = PolymorphicMekanism.rl("robit_smelting");

    private RobitIntegration() {
    }

    /** Installs server-authoritative factories for the Robit's two recipe contexts. */
    public static void registerCommon() {
        RecipeTargets.register(CRAFTING, (player, menu, index) -> {
            if (index == 0 && menu instanceof CraftingRobitContainer crafting && canAccess(player, crafting.getEntity())) {
                return (RobitCraftingTarget) crafting;
            }
            return null;
        });
        RecipeTargets.register(SMELTING, (player, menu, index) -> {
            if (index == 0 && menu instanceof RobitContainer furnace
                  && furnace.getType() == MekanismContainerTypes.SMELTING_ROBIT.get()
                  && canAccess(player, furnace.getEntity())) {
                return (RobitSmeltingTarget) furnace.getEntity();
            }
            return null;
        });
    }

    private static boolean canAccess(ServerPlayer player, EntityRobit robit) {
        return robit.isAlive() && player.level() == robit.level()
              && robit.distanceToSqr(player) <= 64
              && IEntitySecurityUtils.INSTANCE.canAccess(player, robit);
    }
}
