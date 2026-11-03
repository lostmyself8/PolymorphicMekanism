package com.jerry.polymek.client.robit;

import com.illusivesoulworks.polymorph.api.client.PolymorphWidgets;
import mekanism.client.gui.robit.GuiRobitCrafting;
import mekanism.client.gui.robit.GuiRobitSmelting;

/** Selects Robit-specific widgets before Polymorph's generic ResultContainer detection. */
public final class RobitClientIntegration {

    private RobitClientIntegration() {
    }

    /** Registers only the two Robit screens whose native recipe paths are adapted by this module. */
    public static void register() {
        PolymorphWidgets.getInstance().registerWidget(screen -> {
            if (screen instanceof GuiRobitCrafting) {
                return new RobitRecipesWidget(screen, true);
            }
            if (screen instanceof GuiRobitSmelting) {
                return new RobitRecipesWidget(screen, false);
            }
            return null;
        });
    }
}
