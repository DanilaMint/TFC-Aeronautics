from __future__ import annotations
from typing import Literal, List
from dataclasses import dataclass

from _common import TFC_SIZE_VALUES, TFC_WEIGHT_VALUES, DYE_COLORS

Weight = Literal["very_light", "light", "medium", "heavy", "very_heavy"]
Size = Literal["tiny", "very_small", "small", "normal", "large", "very_large", "huge"]

Namespaces = Literal["create", "tfc", "tfc_aeronautics", "simulated", "offroad", "aeronautics"]

@dataclass
class SizeGroup:
    weight: Weight
    size: Size
    namespace: Namespaces
    ids: list[str]

    def __post_init__(self) -> None:
        # Fail at import time, not as a silently broken datapack: TFC's item_size
        # loader rejects unknown enum names with a raw parse error in-game.
        if self.size not in TFC_SIZE_VALUES:
            raise ValueError(
                f"{self.namespace}: invalid size {self.size!r}; "
                f"expected one of {TFC_SIZE_VALUES}"
            )
        if self.weight not in TFC_WEIGHT_VALUES:
            raise ValueError(
                f"{self.namespace}: invalid weight {self.weight!r}; "
                f"expected one of {TFC_WEIGHT_VALUES}"
            )

GROUPS: list[SizeGroup] = [
    # Tools
    SizeGroup("very_heavy", "very_large", "create", [
        "wrench", "potato_cannon", "extendo_grip", "wand_of_symmetry",
        "cardboard_sword",
    ]),
    # Armor
    SizeGroup("very_heavy", "large", "create", [
        "copper_diving_helmet", "copper_diving_boots", "copper_backtank"
    ]),
    # Light Armor
    SizeGroup("medium", "normal", "create", [
        "cardboard_helmet", "cardboard_chestplate", "cardboard_leggings",
        "cardboard_boots", "goggles"
    ]),
    SizeGroup("medium", "normal", "aeronautics", [
        "aviators_goggles"
    ]),

    # Kinetic relays
    SizeGroup("light", "large", "create", [
        "shaft", "cogwheel", "large_cogwheel", "gearbox", "vertical_gearbox",
        "clutch", "gearshift", "encased_chain_drive", "stressometer",
        "adjustable_chain_gearshift", "belt_connector", "speedometer",
    ]),

    # Fluid pipes
    SizeGroup("light", "large", "create", [
        "fluid_pipe", "mechanical_pump", "smart_fluid_pipe", "fluid_valve"
    ]),

    # Other light blocks
    SizeGroup("light", "large", "create", [
        "schematic_table", "turntable", "hand_crank", "nozzle", "cuckoo_clock",
        "wooden_bracket", "metal_bracket", "piston_extension_pole",
        "gantry_shaft", "sail_frame", "white_sail", "andesite_funnel",
        "brass_funnel", "andesite_tunnel", "brass_tunnel", "item_hatch",
        "cardboard_package_12x10", "cardboard_package_10x8",
        "cardboard_package_10x12", "cardboard_package_12x12",
        "andesite_table_cloth", "brass_table_cloth", "copper_table_cloth",
        "peculiar_bell", "haunted_bell", "desk_bell", "clipboard", "chute",
        "smart_chute", "linear_chassis", "secondary_linear_chassis",
        "radial_chassis", "portable_storage_interface", "portable_fluid_interface",
        "andesite_casing", "brass_casing", "copper_casing", "sequenced_gearshift",
        "flywheel", "railway_casing", "item_vault", "fluid_valve",

    ]),
    SizeGroup("light", "large", "create", [
        f"{color}_postbox" for color in DYE_COLORS
    ]), # colored postboxes
    SizeGroup("light", "large", "create", [
        f"{color}_table_cloth" for color in DYE_COLORS
    ]), # colored table clothes
    SizeGroup("light", "large", "create", [
        f"{ "copper" if color == "orange" else color }_valve_handle" for color in DYE_COLORS
    ]), # colored valve handles

    # Redstone things
    SizeGroup("light", "small", "create", [
        "stock_link", "stock_ticker", "factory_gauge", "display_link",
        "nixie_tube", "display_board", "redstone_link", "analog_lever",
        "placard", "pulse_repeater", "pulse_extender", "pulse_timer",
        "powered_latch", "powered_toggle_latch",
    ]),
    SizeGroup("light", "small", "simulated", [
        "directional_linked_receiver", "modulating_linked_receiver", "redstone_accumulator", "redstone_inductor", "throttle_lever", "linked_typewriter"
    ]),

    # Rail tracks
    SizeGroup("very_light", "large", "create", [
        "track", "controller_rail"
    ]),

    # Machinery
    SizeGroup("medium", "large", "create", [
        "water_wheel", "large_water_wheel", "encased_fan", "millstone", "depot",
        "weighted_ejector", "hose_pulley", "spout", "mechanical_piston",
        "sticky_mechanical_piston", "gantry_carriage", "windmill_bearing",
        "mechanical_bearing", "clockwork_bearing", "rope_pulley",
        "elevator_pulley", "cart_assembler", "sticker", "contraption_controls",
        "redstone_contact", "mechanical_drill", "mechanical_saw", "deployer",
        "mechanical_harvester", "mechanical_plough", "mechanical_roller",
        "mechanical_crafter", "rotation_speed_controller", "mechanical_arm",
        "track_station", "track_signal", "track_observer", "controls",
        "content_observer", "stockpile_switch", "packager", "repackager",
        "package_frogport", "redstone_requester", "fluid_tank", "item_drain",
    ]),

    # Heavy machinery
    SizeGroup("heavy", "very_large", "create", [
        "crushing_wheel", "mechanical_press", "mechanical_mixer", "steam_engine",
        "steam_whistle", "basin", "schematicannon"
    ]),

    # Light items-ingredients
    SizeGroup("light", "small", "create", [
        "propeller", "whisk", "brass_hand", "crafter_slot_cover",
        "electron_tube", "transmitter", "pulp", "cardboard", "tree_fertilizer",
        "filter", "attribute_filter", "package_filter", "precision_mechanism",
        "empty_schematic", "schematic_and_quill", "crafting_blueprint",
        "superglue", "minecart_coupling"
    ]),

    # Custom anvils
    SizeGroup("very_heavy", "huge","tfc_aeronautics", [f"metal/anvil/{metal}" for metal in [
        "bismuth", "brass", "gold", "nickel", "rose_gold", "silver", "tin", "zinc",
        "sterling_silver", "cast_iron", "pig_iron", "weak_steel", "weak_blue_steel",
        "weak_red_steel", "high_carbon_steel", "high_carbon_black_steel",
        "high_carbon_blue_steel", "high_carbon_red_steel", "unknown"
    ]]),
]

if __name__ == '__main__':
    namespaces = {}
    total = 0

    for group in GROUPS:
        previous = namespaces.get(group.namespace) or 0
        count = len(group.ids)
        namespaces[group.namespace] = previous + count
        total += count

    print("----- Number of items -----")
