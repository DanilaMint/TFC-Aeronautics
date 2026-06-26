var STONE_DATA = JsonIO.read("kubejs/data/stones.json");
var METALS_DATA = JsonIO.read("kubejs/data/metals.json");

ServerEvents.recipes(event => {
    for (const STONE of Object.values(STONE_DATA)) {
        if (STONE.type !== 'igneous extrusive') continue;
        event.recipes.tfc.heating(`tfc:rock/loose/${STONE.name}`, METALS_DATA.magmatite.melt_temperature)
            .fluidOutput(Fluid.of("tfc_aeronautics:magmatite", 250));

        event.recipes.tfc.quern(
            Item.of('tfc_aeronautics:magmatite_powder', 50),
            `tfc:rock/loose/${STONE.name}`);
    }
});

TFCEvents.data(event => {
    event.fluidHeat({
        fluid: "tfc_aeronautics:magmatite",
        meltTemperature: METALS_DATA.magmatite.melt_temperature,
        specificHeatCapacity: 0.7
    });

    // Нагрев магматических пород
    for (const STONE of Object.values(STONE_DATA)) {
        if (STONE.type !== 'igneous extrusive') continue;
        event.heat({
            ingredient: `tfc:rock/loose/${STONE.name}`,
            heatCapacity: 0.5
        });
    }
});
