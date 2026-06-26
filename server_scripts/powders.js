var METALS_DATA = JsonIO.read("kubejs/data/metals.json");

ServerEvents.recipes((event) => {
    for (const METAL of Object.values(METALS_DATA)) {
        if (!METAL.can_be_powder) continue;

        let METAL_POWDER_ID = `tfc_aeronautics:${METAL.name}_powder`;

        if (METAL.is_tfc_metal) {
            // Плавка
            event.recipes.tfc
                .heating(METAL_POWDER_ID, METAL.melting_temp)
                .fluidOutput(Fluid.of(`tfc:metal/${METAL.name}`, 5));

            // Дробление слитка в жернове
            event.recipes.tfc.quern(
                Item.of(METAL_POWDER_ID, 20),
                `tfc:metal/ingot/${METAL.name}`
            );

            // Дробление слитка в дробилке Create
            event.recipes.create.crushing(
                Item.of(METAL_POWDER_ID, 20),
                `tfc:metal/ingot/${METAL.name}`
            );

            // Дробление двойного слитка в дробилке Create
            event.recipes.create.crushing(
                Item.of(METAL_POWDER_ID, 40),
                `tfc:metal/double_ingot/${METAL.name}`
            );

            // Дробление листа в дробилке Create
            event.recipes.create.crushing(
                Item.of(METAL_POWDER_ID, 40),
                `tfc:metal/sheet/${METAL.name}`
            );

            // Дробление двойного листа в дробилке Create
            event.recipes.create.crushing(
                Item.of(METAL_POWDER_ID, 80),
                `tfc:metal/double_sheet/${METAL.name}`
            );

            // Дробление стержня в дробилке Create
            event.recipes.create.crushing(
                Item.of(METAL_POWDER_ID, 10),
                `tfc:metal/rod/${METAL.name}`
            );

            if (METAL.can_be_tool) {
                // Наконечники
                event.recipes.create.crushing(
                    Item.of(METAL_POWDER_ID, 20),
                    `tfc:metal/pickaxe_head/${METAL.name}`
                );
                event.recipes.create.crushing(
                    Item.of(METAL_POWDER_ID, 20),
                    `tfc:metal/shovel_head/${METAL.name}`
                );
                event.recipes.create.crushing(
                    Item.of(METAL_POWDER_ID, 20),
                    `tfc:metal/axe_head/${METAL.name}`
                );
                event.recipes.create.crushing(
                    Item.of(METAL_POWDER_ID, 20),
                    `tfc:metal/propick_head/${METAL.name}`
                );
                event.recipes.create.crushing(
                    Item.of(METAL_POWDER_ID, 20),
                    `tfc:metal/hoe_head/${METAL.name}`
                );
                event.recipes.create.crushing(
                    Item.of(METAL_POWDER_ID, 20),
                    `tfc:metal/chisel_head/${METAL.name}`
                );
                event.recipes.create.crushing(
                    Item.of(METAL_POWDER_ID, 20),
                    `tfc:metal/hammer_head/${METAL.name}`
                );
                event.recipes.create.crushing(
                    Item.of(METAL_POWDER_ID, 20),
                    `tfc:metal/saw_blade/${METAL.name}`
                );
                event.recipes.create.crushing(
                    Item.of(METAL_POWDER_ID, 20),
                    `tfc:metal/knife_blade/${METAL.name}`
                );
                event.recipes.create.crushing(
                    Item.of(METAL_POWDER_ID, 20),
                    `tfc:metal/scythe_blade/${METAL.name}`
                );
                event.recipes.create.crushing(
                    Item.of(METAL_POWDER_ID, 20),
                    `tfc:metal/javelin_head/${METAL.name}`
                );
                event.recipes.create.crushing(
                    Item.of(METAL_POWDER_ID, 40),
                    `tfc:metal/sword_blade/${METAL.name}`
                );
                event.recipes.create.crushing(
                    Item.of(METAL_POWDER_ID, 40),
                    `tfc:metal/mace_head/${METAL.name}`
                );
                event.recipes.create.crushing(
                    Item.of(METAL_POWDER_ID, 40),
                    `tfc:metal/fish_hook/${METAL.name}`
                );
            }
        }
        else {
            event.recipes.tfc.heating(METAL_POWDER_ID, METAL.melting_temp)
                .fluidOutput(Fluid.of(`tfc_aeronautics:${METAL.name}`, 5));
        }
    }
});

TFCEvents.data((event) => {
    // Register heat data
    for (const METAL of Object.values(METALS_DATA)) {
        if (!METAL.can_be_powder) continue;
        event.heat({
            ingredient: `tfc_aeronautics:${METAL.name}_powder`,
            heatCapacity: METAL.heat_capacity / 20,
            forgingTemperature: METAL.forging_temp,
            weldingTemperature: METAL.welding_temp,
        });
    }
});
