package com.jerry.polymek.client.qio;

import com.illusivesoulworks.polymorph.api.client.PolymorphWidgets;
import com.illusivesoulworks.polymorph.api.client.base.IRecipesWidget;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.qio.GuiQIOItemViewer;
import org.jetbrains.annotations.Nullable;

/**
 * Registers one context-aware Polymorph widget for both kinds of QIO dashboard.
 */
public final class QIOClientIntegration {

    private QIOClientIntegration() {
    }

    public static void register() {
        PolymorphWidgets.getInstance().registerWidget(screen -> screen instanceof GuiQIOItemViewer<?> qio
                ? new QIORecipesWidgetImpl(qio) : null);
    }

    /**
     * Resolves the current screen's coordinator after native window transfer; it is absent before widget initialization.
     */
    public static @Nullable QIORecipesWidget recipesWidget(IGuiWrapper gui) {
        IRecipesWidget current = PolymorphWidgets.getInstance().getCurrentWidget();
        return current instanceof QIORecipesWidget view && view.isFor(gui) ? view : null;
    }
}