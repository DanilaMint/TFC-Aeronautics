package ru.tfc_aeronautics.bracket;

import com.simibubi.create.content.decoration.bracket.BracketBlock;

/**
 * TFC-themed wooden bracket block. One concrete subclass per TFC wood species
 * exists; the geometry, block state properties, and ПКМ behaviour all come from
 * the inherited {@link BracketBlock} (which already declares
 * {@code AXIS_ALONG_FIRST_COORDINATE} and {@code TYPE}).
 *
 * <p>Texture and item model are bound to the per-wood PNGs in
 * {@code assets/tfc_aeronautics/textures/block/wood/bracket/} via datagen.
 */
public class WoodenBracket extends BracketBlock {
    public WoodenBracket(Properties properties) {
        super(properties);
    }
}
