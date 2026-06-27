const METALS_DATA = JsonIO.read('kubejs/data/metals.json');

ServerEvents.recipes(event => {
    for (const [KEY, METAL] of Object.entries(METALS_DATA)) {
        if (!METAL.can_be_powder) continue;

        let powder_id = `tfc_aeronautics:${KEY}_powder`;

        event.recipes.tfc.heating(powder_id, METAL.melt_temperature)
            .fluidOutput(Fluid.of(METAL.id, 5));

        if (METAL.ingot_id !== null) {
            // Дробление слитка в жернове
            event.recipes.tfc.quern(
                Item.of(powder_id, 20),
                METAL.ingot_id
            );

            event.recipes.create.crushing(
                Item.of(powder_id, 20),
                METAL.ingot_id
            );
        }

        if (METAL.is_tfc_metal) {
            // Дробление двойного слитка в дробилке Create
            event.recipes.create.crushing(
                Item.of(powder_id, 40),
                `tfc:metal/double_ingot/${KEY}`
            );
            // Дробление листа в дробилке Create
            event.recipes.create.crushing(
                Item.of(powder_id, 40),
                `tfc:metal/sheet/${KEY}`
            );
            // Дробление двойного листа в дробилке Create
            event.recipes.create.crushing(
                Item.of(powder_id, 80),
                `tfc:metal/double_sheet/${KEY}`
            );
            // Дробление стержня в дробилке Create
            event.recipes.create.crushing(
                Item.of(powder_id, 10),
                `tfc:metal/rod/${KEY}`
            );

            if (METAL.can_be_tool) {
                // Наконечники
                event.recipes.create.crushing(
                    Item.of(powder_id, 20),
                    `tfc:metal/pickaxe_head/${KEY}`
                );
                event.recipes.create.crushing(
                    Item.of(powder_id, 20),
                    `tfc:metal/shovel_head/${KEY}`
                );
                event.recipes.create.crushing(
                    Item.of(powder_id, 20),
                    `tfc:metal/axe_head/${KEY}`
                );
                event.recipes.create.crushing(
                    Item.of(powder_id, 20),
                    `tfc:metal/propick_head/${KEY}`
                );
                event.recipes.create.crushing(
                    Item.of(powder_id, 20),
                    `tfc:metal/hoe_head/${KEY}`
                );
                event.recipes.create.crushing(
                    Item.of(powder_id, 20),
                    `tfc:metal/chisel_head/${KEY}`
                );
                event.recipes.create.crushing(
                    Item.of(powder_id, 20),
                    `tfc:metal/hammer_head/${KEY}`
                );
                event.recipes.create.crushing(
                    Item.of(powder_id, 20),
                    `tfc:metal/saw_blade/${KEY}`
                );
                event.recipes.create.crushing(
                    Item.of(powder_id, 20),
                    `tfc:metal/knife_blade/${KEY}`
                );
                event.recipes.create.crushing(
                    Item.of(powder_id, 20),
                    `tfc:metal/scythe_blade/${KEY}`
                );
                event.recipes.create.crushing(
                    Item.of(powder_id, 20),
                    `tfc:metal/javelin_head/${KEY}`
                );
                event.recipes.create.crushing(
                    Item.of(powder_id, 40),
                    `tfc:metal/sword_blade/${KEY}`
                );
                event.recipes.create.crushing(
                    Item.of(powder_id, 40),
                    `tfc:metal/mace_head/${KEY}`
                );
                event.recipes.create.crushing(
                    Item.of(powder_id, 40),
                    `tfc:metal/fish_hook/${KEY}`
                );
            }
        }
    }
});

TFCEvents.data(event => {
    for (const [KEY, METAL] of Object.entries(METALS_DATA)) {
        if (!METAL.can_be_powder) continue;
        event.heat({
            ingredient: `tfc_aeronautics:${KEY}_powder`,
            heatCapacity: METAL.solid_heat_capacity / 20,
            forgingTemperature: METAL.melt_temperature * 0.6,
            weldingTemperature: METAL.melt_temperature * 0.8,
        });
    }
});
