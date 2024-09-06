package ciallo.mikun.jeihistory.helper;

import ciallo.mikun.jeihistory.jei.JeiHistoryPlugin;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.library.ingredients.TypedIngredient;


public class IngredientHelper {
    public static <T> ITypedIngredient<?> createTypedIngredient(T ingredient) {
        return TypedIngredient.createAndFilterInvalid(JeiHistoryPlugin.params.ingredientManager, ingredient, true).orElse(null);
    }
}
