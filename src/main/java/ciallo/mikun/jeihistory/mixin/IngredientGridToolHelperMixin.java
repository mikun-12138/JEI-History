package ciallo.mikun.jeihistory.mixin;

import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.common.config.IClientToggleState;
import mezz.jei.common.config.IIngredientFilterConfig;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.gui.overlay.IngredientGridTooltipHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = IngredientGridTooltipHelper.class, remap = false)
public interface IngredientGridToolHelperMixin {
    @Accessor
    IIngredientFilterConfig getIngredientFilterConfig();

    @Accessor
    IClientToggleState getToggleState();

    @Accessor
    IInternalKeyMappings getKeyBindings();

    @Accessor
    IColorHelper getColorHelper();
}
