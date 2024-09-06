package ciallo.mikun.jeihistory.mixin;

import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.overlay.IngredientGrid;
import mezz.jei.gui.overlay.IngredientGridTooltipHelper;
import mezz.jei.gui.overlay.IngredientListRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;


@Mixin(value = IngredientGrid.class, remap = false)
public interface IngredientGridAccessor {
    @Accessor
    IngredientListRenderer getIngredientListRenderer();

    @Accessor
    IIngredientManager getIngredientManager();

    @Accessor
    boolean getSearchable();

    @Accessor
    IngredientGridTooltipHelper getTooltipHelper();

    @Accessor
    IIngredientGridConfig getGridConfig();

    @Accessor
    void setArea(ImmutableRect2i area);

}
