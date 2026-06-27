# TFC Aeronautics

**Bridging the Iron Age and the Industrial Revolution.**

TFC Aeronautics is a Minecraft addon that seamlessly integrates the hardcore survival mechanics of **TerraFirmaCraft (TFC)** with the mechanical engineering of **Create** and the sky-high ambitions of **Create Aeronautics**.

This mod allows you to craft and utilize all standard Create mechanisms using resources sourced directly from the TFC world. No more vanilla Minecraft materials—forge your gears, shafts, and chassis from TFC's metal alloys and wood types. Take your TFC survival experience to the skies by building functional airships using materials you painstakingly forged through the ages.

## 🌟 Features

- **TFC-Centric Crafting:** All Create recipes are reworked to use TFC materials (e.g., Wrought Iron, Bronze, Steel, and TFC Wood logs).
- **Native Compatibility:** Create Aeronautics' airship frames and engines are fully craftable with TFC components.
- **Balanced Progression:** Access to mechanical power and flight is aligned with TFC's technological timeline. You must progress through the TFC ages to unlock advanced Create machinery.
- **Fluid & Item Integration:** TFC fluids (like Molten Metals) can be used in Create's fluid-based recipes (such as polishing or filling).
- **Powered by KubeJS:** All recipe integrations are handled dynamically via KubeJS scripts, making the mod extremely flexible and easy to customize.

## 📦 Dependencies

This mod is designed for **Minecraft 1.21.1** and requires the following mods to function:

### Required Core Mods:
- **[NeoForge](https://neoforged.net/)** (v21.1.233 or later) – *The mod loader.*
- **[TerraFirmaCraft (TFC)](https://www.curseforge.com/minecraft/mc-mods/terrafirmacraft)** (v4.1.3+) – *The core survival overhaul.*
- **[Create](https://www.curseforge.com/minecraft/mc-mods/create)** (v6.0.10+) – *The mechanical engineering mod.*
- **[Create Aeronautics](https://www.curseforge.com/minecraft/mc-mods/create-aeronautics)** (v1.3.0+) – *The airship construction mod.*

### Required KubeJS Suite:
> *TFC Aeronautics relies entirely on KubeJS to apply its recipe changes. These mods are **mandatory** for the addon to work.*

- **[KubeJS](https://www.curseforge.com/minecraft/mc-mods/kubejs)** (v2101.7.2+) – *Core scripting mod.*
- **[KubeJS Create](https://www.curseforge.com/minecraft/mc-mods/kubejs-create)** (v2101.3.1+) – *Adds Create-specific recipe support for KubeJS.*
- **[KubeJS TFC](https://www.curseforge.com/minecraft/mc-mods/kubejs-tfc)** (v2.0.0+) – *Adds TFC-specific recipe support for KubeJS.*

## 🚀 Getting Started

1. **Install NeoForge** for Minecraft 1.21.1.
2. Place **all required mods** (TFC, Create, Create Aeronautics, KubeJS, KubeJS Create, KubeJS TFC) into your `mods` folder.
3. Place built scripts and resources (`client_scripts/`, `server_scripts/`, `startup_scripts/`, `assets/`, `data/`) into `kubejs/` folder.
4. Launch the game. The KubeJS scripts will automatically load and override the default Create recipes with TFC-compatible versions.
5. You will now see Create recipes in JEI/REI that utilize TFC ingots, sheets, rods, and planks.

## 🛠️ Customization

Because the mod is built on KubeJS, you have full control over the integration:

- Edit existing recipes in the `kubejs/server_scripts/` directory.
- Add new recipes to suit your modpack's balance.
- Disable specific integrations by simply removing or commenting out script lines.

## 🤝 Contributing

Issues and Pull Requests are welcome! If you encounter a bug or have a suggestion for a recipe adjustment, please open an issue on the GitHub Issues page.
