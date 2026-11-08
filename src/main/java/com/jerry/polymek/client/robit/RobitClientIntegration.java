package com.jerry.polymek.client.robit;

import com.illusivesoulworks.polymorph.api.client.PolymorphWidgets;
import com.illusivesoulworks.polymorph.api.client.base.IRecipesWidget;
import mekanism.client.gui.robit.GuiRobitSmelting;
import org.jetbrains.annotations.Nullable;

/**
 * Selects a Robit-specific furnace widget before Polymorph's generic result-container detection.
 */
public final class RobitClientIntegration {

    private RobitClientIntegration() {
    }

    /**
     * Registers the Robit furnace screen whose recipe path is adapted by this module.
     */
    public static void register() {
        PolymorphWidgets.getInstance().registerWidget(screen -> {
            if (screen instanceof GuiRobitSmelting) {
                return new RobitRecipesWidget(screen);
            }
            return null;
        });
    }

    /**
     * Resolves only the picker belonging to this native Robit furnace screen.
     */
    public static @Nullable RobitRecipesWidget recipesWidget(GuiRobitSmelting screen) {
        IRecipesWidget current = PolymorphWidgets.getInstance().getCurrentWidget();
        return current instanceof RobitRecipesWidget widget && widget.isFor(screen) ? widget : null;
    }
}
