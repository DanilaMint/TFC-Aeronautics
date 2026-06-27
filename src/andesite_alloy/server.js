import { regirtryTFCHeatingRecipe } from '../lib/tfc.js'

const METAL_DATA = JsonIO.read("kubejs/data/metals.json");

const ANDESITE_ALLOY = METAL_DATA.andesite_alloy;
const MAGMATITE = METAL_DATA.magmatite;
const CAST_IRON = METAL_DATA.cast_iron;

ServerEvents.recipes(event => {
    // Расплавление
    event.recipes.tfc.heating(ANDESITE_ALLOY.ingot_id, ANDESITE_ALLOY.melt_temperature)
        .fluidOutput(Fluid.of(ANDESITE_ALLOY.id, 100));

    // Расплавление вала
    event.recipes.tfc.heating("create:shaft", ANDESITE_ALLOY.melt_temperature)
        .fluidOutput(Fluid.of(ANDESITE_ALLOY.id, 25));

    // Сплав
    event.recipes.tfc.alloy(
        ANDESITE_ALLOY.id,
        [
            { fluid: MAGMATITE.id, min: 0.95, max: 0.98 },
            { fluid: CAST_IRON.id, min: 0.02, max: 0.05 }
        ]
    );

    // Литье слитка
    event.recipes.tfc.casting(
        ANDESITE_ALLOY.ingot_id,
        'tfc:ceramic/ingot_mold',
        Fluid.of(ANDESITE_ALLOY.id, 100),
        0.95
    );

    // Ковка в вал
    event.recipes.tfc.anvil(
        Item.of("create:shaft", 4),
        "create:andesite_alloy",
        [
            "upset_not_last",
            "draw_not_last",
            "hit_last"
        ]
    ).tier(2);
});

TFCEvents.data(event => {
    // Нагрев жидкого андезитового сплава
    event.fluidHeat({
        fluid: "tfc_aeronautics:andesite_alloy",
        meltTemperature: ANDESITE_ALLOY.melt_temperature,
        specificHeatCapacity: ANDESITE_ALLOY.liquid_heat_capacity
    });

    // Нагрев слитка андезитового сплава
    event.heat({
        ingredient: 'create:andesite_alloy',
        heatCapacity: ANDESITE_ALLOY.solid_heat_capacity,
        forgingTemperature: ANDESITE_ALLOY.melt_temperature * 0.6,
        weldingTemperature: ANDESITE_ALLOY.melt_temperature * 0.8,
    });
    // Нагрев вала
    event.heat({
        ingredient: 'create:shaft',
        heatCapacity: ANDESITE_ALLOY.solid_heat_capacity
    })
});

ServerEvents.tags('fluid', event => {
    // Добавляем ваш андезитовый сплав в системный тег TFC для изложниц слитков
    event.add('tfc:usable_in_ingot_mold', 'tfc_aeronautics:andesite_alloy');

    // На всякий случай добавим и магматит, если вы захотите разливать его напрямую
    //event.add('tfc:usable_in_ingot_mold', 'tfc_aeronautics:magmatite');
});
