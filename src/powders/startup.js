const METALS_DATA = JsonIO.read('kubejs/data/metals.json');

StartupEvents.registry('item', event => {
    for (const [KEY, METAL] of Object.entries(METALS_DATA)) {
        if (!METAL.can_be_powder) continue;

        event.create(`tfc_aeronautics:${KEY}_powder`)
            .texture(`tfc_aeronautics:item/${KEY}_powder`)
            .translationKey(`item.tfc_aeronautics.${KEY}_powder`)
            .maxStackSize(64);
    }
});
