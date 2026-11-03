package com.jerry.polymek;

import com.jerry.polymek.common.network.RecipeNetwork;
import com.jerry.polymek.common.registries.PolymekDataComponents;
import com.jerry.polymek.compat.assemblicator.AssemblicatorIntegration;
import com.jerry.polymek.compat.qio.QIOIntegration;
import com.jerry.polymek.compat.robit.RobitIntegration;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(PolymorphicMekanism.MOD_ID)
public class PolymorphicMekanism {

    public static final String MOD_ID = "polymek";
    public static final Logger LOGGER = LogUtils.getLogger();

    public PolymorphicMekanism(IEventBus modEventBus) {
        PolymekDataComponents.DATA_COMPONENTS.register(modEventBus);
        modEventBus.addListener(RecipeNetwork::register);
        AssemblicatorIntegration.registerCommon();
        QIOIntegration.registerCommon();
        RobitIntegration.registerCommon();
        LOGGER.info("Registered Polymorph recipe selection for Mekanism assemblicators, QIO, and Robit");
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}