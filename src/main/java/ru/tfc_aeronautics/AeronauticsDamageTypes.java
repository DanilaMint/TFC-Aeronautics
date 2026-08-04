package ru.tfc_aeronautics;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;

/**
 * Damage types added by this mod. The matching entries live in
 * {@code data/tfc_aeronautics/damage_type/}.
 */
public final class AeronauticsDamageTypes {
    /** Dealt by touching a bare, spinning Create shaft or cogwheel. */
    public static final ResourceKey<DamageType> SHAFT = key("shaft");

    private AeronauticsDamageTypes() {}

    private static ResourceKey<DamageType> key(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(Aeronautics.MOD_ID, name));
    }

    public static DamageSource shaft(Level level) {
        return new DamageSource(level.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(SHAFT));
    }
}
