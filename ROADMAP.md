# Roadmap
---

## 📌 Status Legend
- ⬜ `To Do` – Planned
- 🟨 `In Progress` – Currently working on
- ✅ `Done` – Completed
- 🔄 `Review` – Under review
---

## Environment Setup
**Goal:** Prepare the repository and development tools

| Task | Status | Dependencies |
|------|--------|--------------|
| Initialize repository | ✅ | – |
| Create folder structure | ✅ | – |
| Set up build script (Node.js) for automatic compilation | ✅ | – |
| Configure `.gitignore` | ✅ | – |

---

## Metal Powders
**Goal:** Introduce basic materials for advanced metallurgy

| Task | Status | Dependencies |
|------|--------|--------------|
| Create 16x16 textures for powders: andesite alloy, bismuth, copper, gold, silver, tin, nickel, magmatite, cast iron, zinc | 🟨 | – |
| Define custom items | ✅ | – |
| Add localization (`ru_ru.lang`, `en_us.lang`) with proper names | ⬜ | Items |
| Create heating recipes: powder -> molten metal | ✅ | Items |
| Create crushing and quern recipes: ingot -> powder | ✅ | Items |

---

## Magmatite
**Goal:** Create a new intermediate metal for making andesite alloy

| Task | Status | Dependencies |
|------|--------|--------------|
| Create fluid of molten magmatite | ✅ | – |
| Create heating recipes: igneous extrusive rock -> molten magmatite | ✅ | Fluid |
| Add fluid name to `.lang` files | ⬜ | Fluid |

---

## Andesite Alloy
**Goal:** Create a new alloy as a core component for future mechanics

| Task | Status | Dependencies |
|------|--------|--------------|
| Create texture for andesite alloy ingot | ✅ | – |
| Create texture for molten andesite alloy | 🟨 | – |
| Create fluid of molten andesite alloy | ✅ | – |
| Add fluid to `.lang` files | ⬜ | Fluid |
| Make alloy recipe: magmatite + cast iron -> andesite alloy | ✅ | Items |

---

## Shaft
**Goal:** Add a mechanical component and refactor old recipes

| Task | Status | Dependencies |
|------|--------|--------------|
| Create forging recipe for shaft andesite alloy ingot -> shaft | ✅ | Andesite Alloy |
| Remove old shaft by 2 andesite alloy ingots | ⬜ | – |
| Create heating recipe: shaft -> molten andesite alloy | ✅ | – |

---

## Andesite casing
**Goal:** Make compatibility with TFC logs

| Task | Status | Dependencies |
|------|--------|--------------|
| Create andesite casing recipes | ⬜ |  |

---

## Brass casing
**Goal:** Make compatibility with TFC logs

| Task | Status | Dependencies |
|------|--------|--------------|
| Create brass casing recipes | ⬜ |  |

---

## Copper casing
**Goal:** Make compatibility with TFC logs

| Task | Status | Dependencies |
|------|--------|--------------|
| Create copper casing recipes | ⬜ |  |

---

## Wrench
**Goal:** Balance recipe of wrench

| Task | Status | Dependencies |
|------|--------|--------------|
| Create brass wrench head | ⬜ |  |
| Make recipe of wrench | ⬜ | Wrench head |

---

## Red quartz
**Goal:** Balance recipe of red quartz

| Task | Status | Dependencies |
|------|--------|--------------|
| Create red quartz recipe: metamorphic stone + redstone | ⬜ |  |

---

## Sandpaper
**Goal:** Make compatibility Create's and TFC's sandpapers

*idk how to do it*

---

## Sheet pressing
**Goal:** Rebalance sheet pressing recipes

| Task | Status | Dependencies |
|------|--------|--------------|
| Remove old recipe by 1 ingot | ⬜ |  |
| Add pressing recipe: double ingot -> sheet | ⬜ |  |

---

## Create Recipes with TFC Grains 

|Task| Status| Dependencies |
|------|--------|--------------|
| Create milling recipe: grain -> flour (barley, oat, rye, wheat, rice, maize) | ⬜ |  |
| Create mixing recipe: tfc flour + water -> tfc dough | ⬜ |  |
| Create bulk washing recipe: tfc flour -> tfc dough | ⬜ |  |
| Create basin press recipe: tfc grain -> oil | ⬜ |  |

---

## Create Mixing with Heating
**Goal:** Make mixing recipes with heating balanced

| Task | Status | Dependencies |
|------|--------|--------------|
| Make charcoal forge able to heat basin | ⬜ |  |

## Brass recipe

| Task | Status | Dependencies |
|------|--------|--------------|
| Remove create brass recipe | ⬜ |  |

---

*Roadmap will be updated later*
