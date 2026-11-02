package com.jerry.polymek.common;

import com.jerry.polymek.PolymorphicMekanism;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.text.ILangEntry;
import net.minecraft.Util;

@NothingNullByDefault
public enum PolymekLang implements ILangEntry {
    RECIPE_CHOOSE("recipe", "choose"),
    RECIPE_SOURCE("recipe", "source"),
    RECIPE_ENCODED("recipe", "encoded"),
    RECIPE_LOCKED("recipe", "locked"),
    RECIPE_MISSING_CHOOSE("recipe", "missing_choose"),
    RECIPE_MISSING_NO_MATCH("recipe", "missing_no_match");

    private final String key;

    PolymekLang(String type, String path) {
        this(Util.makeDescriptionId(type, PolymorphicMekanism.rl(path)));
    }

    PolymekLang(String key) {
        this.key = key;
    }

    @Override
    public String getTranslationKey() {
        return key;
    }
}