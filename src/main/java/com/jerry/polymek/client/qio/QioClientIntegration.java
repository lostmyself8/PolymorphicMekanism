package com.jerry.polymek.client.qio;

import com.illusivesoulworks.polymorph.api.client.PolymorphWidgets;
import com.illusivesoulworks.polymorph.api.client.base.IRecipesWidget;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.qio.GuiQIOItemViewer;
import org.jetbrains.annotations.Nullable;

/** Registers one context-aware Polymorph widget for both kinds of QIO dashboard. */
public final class QioClientIntegration {

    private QioClientIntegration() {
    }

    public static void register() {
        PolymorphWidgets.getInstance().registerWidget(screen -> screen instanceof GuiQIOItemViewer<?> qio
              ? new QioRecipesWidgetImpl(qio) : null);
    }

    /** Resolves the current screen's coordinator after native window transfer; it is absent before widget initialization. */
    public static @Nullable QioNativeRecipeView nativeView(IGuiWrapper gui) {
        IRecipesWidget current = PolymorphWidgets.getInstance().getCurrentWidget();
        return current instanceof QioNativeRecipeView view && view.isFor(gui) ? view : null;
    }
}