package ru.tfc_aeronautics.recipe;

import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;

/**
 * Public-factory subclass of {@link ProcessingRecipeParams} so that
 * {@link QuernMillingRecipe} can construct a stub params instance for the
 * parent {@code MillingRecipe} constructor without going through the
 * standard {@code ProcessingRecipe.codec} pipeline. Inherits all
 * package-private fields from the parent class.
 */
public final class QuernMillingRecipeParams extends ProcessingRecipeParams
{
    public QuernMillingRecipeParams() {}
}
