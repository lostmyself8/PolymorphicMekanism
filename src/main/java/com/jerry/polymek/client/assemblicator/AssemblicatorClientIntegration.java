package com.jerry.polymek.client.assemblicator;

import com.illusivesoulworks.polymorph.api.client.PolymorphWidgets;
import mekanism.common.inventory.container.tile.FormulaicAssemblicatorContainer;

/** Registers the formulaic assemblicator's contextual Polymorph widget from the client setup path. */
public final class AssemblicatorClientIntegration {

    private AssemblicatorClientIntegration() {
    }

    /** Binds the widget to the existing native formulaic assemblicator menu. */
    public static void register() {
        PolymorphWidgets.getInstance().registerWidget(screen -> screen.getMenu() instanceof FormulaicAssemblicatorContainer menu
                ? new AssemblicatorRecipesWidget(screen, menu) : null);
    }
}
