var METALS_DATA = JsonIO.read("kubejs/data/metals.json");

// Регистрация металлических порошков
StartupEvents.registry("item", (event) => {
    for (const METAL of Object.values(METALS_DATA)) {
        if (!METAL.can_be_powder) continue;
        event.create(`tfc_aeronautics:${METAL.name}_powder`)
            .texture(`tfc_aeronautics:item/${METAL.name}_powder`)
            .maxStackSize(64)
            .translationKey(`item.tfc_aeronautics.${METAL.name}_powder`);
    }
});
