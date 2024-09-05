package ciallo.mikun.jeihistory.helper;

import ciallo.mikun.jeihistory.jei.JeiHistoryPlugin;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.library.ingredients.TypedIngredient;

/**
 * @author mikun_12138
 * @date 2024/9/5 下午7:36
 */
public class IngredientHelper {
    public static <T> ITypedIngredient<?> createTypedIngredient(T ingredient) {
        return TypedIngredient.createAndFilterInvalid(JeiHistoryPlugin.params.ingredientManager, ingredient, true).orElse(null);
    }
}
