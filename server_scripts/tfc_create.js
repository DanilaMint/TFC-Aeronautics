ServerEvents.recipes((event) => {
    // deleting copper ingot to copper sheet
    event.remove({ input: "tfc:metal/ingot/copper", type: "create:pressing" });

    event.recipes.create.pressing(
        "tfc:metal/sheet/copper",
        "tfc:metal/double_ingot/copper",
    );
});
