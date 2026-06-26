var METALS_DATA = JsonIO.read("kubejs/data/metals.json");

const ANDESITE_ALLOY_MELTING_TEMP = METALS_DATA.andesite_alloy.melt_temperature;
const ANDESITE_ALLOY_FORGING_TEMP = METALS_DATA.andesite_alloy.forging_temperature;
const ANDESITE_ALLOY_WELDING_TEMP = METALS_DATA.andesite_alloy.welding_temperature;
const ANDESITE_ALLOY_HEAT_CAPACITY = METALS_DATA.andesite_alloy.heat_capacity;

ServerEvents.recipes(event => {
    // Расплавление
    event.recipes.tfc.heating("create:andesite_alloy", ANDESITE_ALLOY_MELTING_TEMP)
        .fluidOutput(Fluid.of("tfc_aeronautics:andesite_alloy", 100));

    event.recipes.tfc.heating("create:shaft", ANDESITE_ALLOY_MELTING_TEMP)
        .fluidOutput(Fluid.of("tfc_aeronautics:andesite_alloy", 25));
    // Сплав
    event.recipes.tfc.alloy(
        "tfc_aeronautics:andesite_alloy",
        [
            { fluid: "tfc_aeronautics:magmatite", min: 0.95, max: 0.98 },
            { fluid: "tfc:metal/cast_iron", min: 0.02, max: 0.05 }
        ]
    );
    // Литье слитка
    event.recipes.tfc.casting(
        'create:andesite_alloy',
        'tfc:ceramic/ingot_mold',
        Fluid.of('tfc_aeronautics:andesite_alloy', 100),
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

    // Дробление слитка в жернове
    event.recipes.tfc.quern(
        Item.of('tfc_aeronautics:andesite_alloy_powder', 20),
        'create:andesite_alloy'
    );

    // Дробление слитка в дробилке Create
    event.recipes.create.crushing(
        Item.of('tfc_aeronautics:andesite_alloy_powder', 20),
        'create:andesite_alloy'
    );
});

TFCEvents.data(event => {
    // Нагрев жидкого андезитового сплава
    event.fluidHeat({
        fluid: "tfc_aeronautics:andesite_alloy",
        meltTemperature: ANDESITE_ALLOY_MELTING_TEMP,
        specificHeatCapacity: 2.7
    });

    // Нагрев слитка андезитового сплава
    event.heat({
        ingredient: 'create:andesite_alloy',
        heatCapacity: ANDESITE_ALLOY_HEAT_CAPACITY,
        forgingTemperature: ANDESITE_ALLOY_FORGING_TEMP,
        weldingTemperature: ANDESITE_ALLOY_WELDING_TEMP,
    });
    // Нагрев вала
    event.heat({
        ingredient: 'create:shaft',
        heatCapacity: ANDESITE_ALLOY_HEAT_CAPACITY
    })
});

ServerEvents.tags('fluid', event => {
    // Добавляем ваш андезитовый сплав в системный тег TFC для изложниц слитков
    event.add('tfc:usable_in_ingot_mold', 'tfc_aeronautics:andesite_alloy');

    // На всякий случай добавим и магматит, если вы захотите разливать его напрямую
    //event.add('tfc:usable_in_ingot_mold', 'tfc_aeronautics:magmatite');
})
