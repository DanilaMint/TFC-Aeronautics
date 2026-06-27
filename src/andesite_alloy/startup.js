StartupEvents.registry('fluid', event => {
    event.create('tfc_aeronautics:andesite_alloy')
        //.translationKey()
        .stillTexture('minecraft:block/lava_still')
        .flowingTexture('minecraft:block/lava_flow')

        // В KubeJS 1.21 этот метод отвечает сразу и за блок, и за ведро
        .tint(0x6D778B)

        // Физика лавы и металлов
        .type(type => {
            type.temperature(1500) // Высокая температура плавления
            type.lightLevel(15)    // Максимальное свечение в темноте
            type.viscosity(6000)   // Течет медленно, как лава
            type.density(3000)     // Тяжелая жидкость
        })
});
