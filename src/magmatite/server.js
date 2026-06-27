var STONE_DATA = JsonIO.read("kubejs/data/stones.json");
var METALS_DATA = JsonIO.read("kubejs/data/metals.json");

var MAGMATITE_DATA = METALS_DATA.magmatite;

// dtn
if (METALS_DATA === undefined || METALS_DATA === null) {
    console.error("METALS_DATA is undefined or null");
}
if (MAGMATITE_DATA === undefined || MAGMATITE_DATA === null) {
    console.error("MAGMATITE_DATA is undefined or null");
}
if (STONE_DATA === undefined || STONE_DATA === null) {
    console.error("STONE_DATA is undefined or null");
}
console.log("[BAKA] magmatite data: " + MAGMATITE_DATA);


ServerEvents.recipes(event => {
    for (const STONE of Object.values(STONE_DATA)) {
        if (STONE.type !== 'igneous extrusive') continue;
        event.recipes.tfc.heating(`tfc:rock/loose/${STONE.name}`, MAGMATITE_DATA.melt_temperature)
            .fluidOutput(Fluid.of("tfc_aeronautics:magmatite", 250));

        event.recipes.tfc.quern(
            Item.of('tfc_aeronautics:magmatite_powder', 50),
            `tfc:rock/loose/${STONE.name}`);
    }
});

TFCEvents.data(event => {
    event.fluidHeat({
        fluid: "tfc_aeronautics:magmatite",
        meltTemperature: MAGMATITE_DATA.melt_temperature,
        specificHeatCapacity: MAGMATITE_DATA.liquid_heat_capacity
    });

    // Нагрев магматических пород
    for (const STONE of Object.values(STONE_DATA)) {
        if (STONE.type !== 'igneous extrusive') continue;
        event.heat({
            ingredient: `tfc:rock/loose/${STONE.name}`,
            heatCapacity: MAGMATITE_DATA.solid_heat_capacity
        });
    }
});
