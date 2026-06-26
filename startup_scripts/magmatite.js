// Жидкость "Магматит"

StartupEvents.registry('fluid', event => {
    event.create('tfc_aeronautics:magmatite')
        //.translationKey('fluid.tfc_aeronautics.magmatite')
        .stillTexture('minecraft:block/lava_still')   // Ванильная стоячая лава
        .flowingTexture('minecraft:block/lava_flow')
        .tint(0xFF9F40)
        .type(type => {
            type.temperature(1500) // Высокая температура плавления
            type.lightLevel(15)    // Максимальное свечение в темноте
            type.viscosity(6000)   // Течет медленно, как лава
            type.density(3000)     // Тяжелая жидкость
        });
});
