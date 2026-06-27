export function regirtryTFCHeatingRecipe(ingredient, temperature, result_item, item_count = 1, result_fluid, fluid_count = 1000) {
    let recipe = {
        type: "tfc:heating",
        ingredient: ingredient,
        temperature: temperature
    };

    if (result_item) recipe.result_item = { id: result_item, count: item_count };
    if (result_fluid) recipe.result_fluid = result_fluid;

    return recipe;
}

export function registryTFCQuernRecipe(ingredient, result, count = 1) {
    return {
        type: "tfc:quern",
        ingredient: { item: ingredient },
        result: { id: result, count: count }
    };
}

export function registryTFCAlloyRecipe() { }
