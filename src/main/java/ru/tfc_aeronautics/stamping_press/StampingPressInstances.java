package ru.tfc_aeronautics.stamping_press;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Tracks the loaded client-side instances of {@link StampingPressBlockEntity}
 * so that {@code StampingPressFrameTickHandler} can iterate them every client
 * tick to keep the always-visible filter frames in sync.
 *
 * <p>A {@link WeakHashMap} keyed by the block entity is used so that removed
 * entities are collected without manual cleanup. Registration is gated on
 * {@code level.isClientSide()} in the block entity, so the server never
 * touches this set.
 */
public final class StampingPressInstances {

    private static final Set<StampingPressBlockEntity> INSTANCES =
        Collections.newSetFromMap(new WeakHashMap<>());

    private StampingPressInstances() {}

    public static Set<StampingPressBlockEntity> getInstances() {
        return INSTANCES;
    }

    public static void add(StampingPressBlockEntity be) {
        INSTANCES.add(be);
    }

    public static void remove(StampingPressBlockEntity be) {
        INSTANCES.remove(be);
    }
}
