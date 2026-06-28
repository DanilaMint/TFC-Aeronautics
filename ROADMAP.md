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

**Acceptance Criteria:** All dusts can be crafted and smelted into ingots

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

*Roadmap will be updated later*
