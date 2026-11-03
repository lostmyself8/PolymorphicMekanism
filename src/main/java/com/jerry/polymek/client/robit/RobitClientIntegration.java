package com.jerry.polymek.client.robit;

import com.illusivesoulworks.polymorph.api.client.PolymorphWidgets;
import mekanism.client.gui.robit.GuiRobitSmelting;

/** Selects a Robit-specific furnace widget before Polymorph's generic result-container detection. */
public final class RobitClientIntegration {

    private RobitClientIntegration() {
    }

    /** Registers the Robit furnace screen whose recipe path is adapted by this module. */
    public static void register() {
        PolymorphWidgets.getInstance().registerWidget(screen -> {
            if (screen instanceof GuiRobitSmelting) {
                return new RobitRecipesWidget(screen);
            }
            return null;
        });
    }
}