package dev.conferencebattler.gen.data

/** A weapon or spell base. Its ST bonus is rolled from [minSt]..[maxSt] (multiples of 100). */
data class ItemBase(val name: String, val minLevel: Int, val minSt: Int, val maxSt: Int)

// Names and level requirements are loosely based on Diablo's items and spells. Spells roll
// higher than weapons because wizards have lower base ST. Same rule as the affix tables: append
// rows, don't reorder or edit existing ones.

val WeaponBases = listOf(
    ItemBase("Dagger", 1, 300, 600),
    ItemBase("Short Sword", 1, 400, 700),
    ItemBase("Club", 1, 300, 800),
    ItemBase("Falchion", 2, 500, 900),
    ItemBase("Mace", 2, 500, 1000),
    ItemBase("Axe", 2, 500, 1100),
    ItemBase("Scimitar", 4, 600, 1000),
    ItemBase("Long Sword", 6, 800, 1300),
    ItemBase("Broad Sword", 8, 900, 1500),
    ItemBase("Morning Star", 10, 1000, 1600),
    ItemBase("Bastard Sword", 10, 1100, 1800),
    ItemBase("Large Axe", 12, 1200, 1900),
    ItemBase("Claymore", 14, 1300, 2100),
    ItemBase("War Hammer", 15, 1300, 2000),
    ItemBase("Great Sword", 17, 1700, 2700),
    ItemBase("Great Axe", 19, 1600, 2500),
    ItemBase("Maul", 21, 1500, 2600),
)

val SpellBases = listOf(
    ItemBase("Firebolt", 1, 600, 1000),
    ItemBase("Charged Bolt", 1, 500, 1100),
    ItemBase("Holy Bolt", 1, 700, 900),
    ItemBase("Lightning", 3, 900, 1400),
    ItemBase("Inferno", 3, 800, 1300),
    ItemBase("Fireball", 8, 1200, 1800),
    ItemBase("Flame Wave", 9, 1300, 1900),
    ItemBase("Flash", 10, 1200, 1700),
    ItemBase("Chain Lightning", 13, 1600, 2300),
    ItemBase("Blood Star", 14, 1700, 2400),
    ItemBase("Elemental", 16, 1800, 2600),
    ItemBase("Apocalypse", 19, 2400, 3200),
)
