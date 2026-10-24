package dev.conferencebattler.model

enum class Rarity(val label: String) { NORMAL("Normal"), MAGIC("Magic"), UNIQUE("Unique") }

/** Weapons can be used by warriors, spells by wizards. */
enum class ItemKind(val label: String, val usableBy: CharClass) {
    WEAPON("Weapon", CharClass.WARRIOR),
    SPELL("Spell", CharClass.WIZARD),
}

/**
 * An item generated from a QR code. Its only stat is [stBonus], which is added to the holder's
 * ST. As with Diablo's `_iPLDam`, the affix effects are already included.
 */
data class Item(
    val kind: ItemKind,
    val name: String,
    val seed: UInt,
    val level: Int,
    val rarity: Rarity,
    val stBonus: Int,
    val prefix: RolledAffix? = null,
    val suffix: RolledAffix? = null,
)
