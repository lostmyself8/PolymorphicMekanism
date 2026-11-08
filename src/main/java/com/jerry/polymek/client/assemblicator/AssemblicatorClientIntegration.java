package com.jerry.polymek.client.assemblicator;

import com.illusivesoulworks.polymorph.api.client.PolymorphWidgets;
import com.illusivesoulworks.polymorph.api.client.base.IRecipesWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.jetbrains.annotations.Nullable;
import mekanism.common.inventory.container.tile.FormulaicAssemblicatorContainer;

/**
 * Registers the formulaic assemblicator's contextual Polymorph widget from the client setup path.
 */
public final class AssemblicatorClientIntegration {

    private AssemblicatorClientIntegration() {
    }

    /**
     * Binds the widget to the existing native formulaic assemblicator menu.
     */
    public static void register() {
        PolymorphWidgets.getInstance().registerWidget(screen -> screen.getMenu() instanceof FormulaicAssemblicatorContainer menu
                ? new AssemblicatorRecipesWidget(screen, menu) : null);
    }

    /**
     * Resolves the selector created for the current formulaic assemblicator screen.
     */
    public static @Nullable AssemblicatorRecipesWidget recipesWidget(AbstractContainerScreen<?> screen) {
        IRecipesWidget current = PolymorphWidgets.getInstance().getCurrentWidget();
        return current instanceof AssemblicatorRecipesWidget widget && widget.isFor(screen) ? widget : null;
    }
}
