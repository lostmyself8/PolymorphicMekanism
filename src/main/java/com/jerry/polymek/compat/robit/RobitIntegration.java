package com.jerry.polymek.compat.robit;

import com.jerry.polymek.PolymorphicMekanism;
import com.jerry.polymek.common.recipe.RecipeTargets;
import mekanism.api.security.IEntitySecurityUtils;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.entity.robit.RobitContainer;
import mekanism.common.registries.MekanismContainerTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/** Registers the Robit's entity furnace selection context. */
public final class RobitIntegration {

    public static final ResourceLocation SMELTING = PolymorphicMekanism.rl("robit_smelting");

    private RobitIntegration() {
    }

    /** Installs the server-authoritative factory for the Robit's furnace recipe context. */
    public static void registerCommon() {
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