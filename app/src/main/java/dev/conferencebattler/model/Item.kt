package dev.conferencebattler.model

enum class Rarity(val label: String) { NORMAL("Swag"), MAGIC("Premium swag"), UNIQUE("Exclusive") }

/** Attack items add to ST, defence items add to DF. */
enum class ItemKind(val label: String, val stat: String) {
    ATTACK("Attack", "ST"),
    DEFENCE("Defence", "DF"),
}

/**
 * An item generated from a QR code. Its only stat is [bonus], which is added to the holder's ST
 * or DF depending on [kind]. As with Diablo's `_iPLDam`, the affix effects are already included.
 */
data class Item(
    val kind: ItemKind,
    val name: String,
    val seed: UInt,
    val level: Int,
    val rarity: Rarity,
    val bonus: Int,
    val prefix: RolledAffix? = null,
    val suffix: RolledAffix? = null,
)
