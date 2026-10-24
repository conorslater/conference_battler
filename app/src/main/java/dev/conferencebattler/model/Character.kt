package dev.conferencebattler.model

import java.util.Locale

enum class CharClass(val label: String) {
    WARRIOR("Warrior"),
    WIZARD("Wizard"),
}

/**
 * Character tier, picked from a weighted pool in the same way Diablo's `SelectAffix` does.
 *
 * @param level the affix level used when rolling the character's epithet.
 * @param bonusPercent added to HP, ST and DF.
 */
enum class Tier(val label: String, val weight: Int, val level: Int, val bonusPercent: Int) {
    COMMON("Common", 60, 1, 0),
    VETERAN("Veteran", 30, 5, 10),
    CHAMPION("Champion", 9, 10, 20),
    LEGEND("Legend", 1, 15, 35),
}

/**
 * A character generated from a badge. [hp], [st] and [df] already include the tier bonus and the
 * epithet affixes. The equipped [item] adds to ST; see [totalSt].
 *
 * @param name the normalized display name (the badge's largest line). The seed comes from all of
 *   the badge's text, not just this.
 */
data class Character(
    val name: String,
    val seed: UInt,
    val charClass: CharClass,
    val tier: Tier,
    val prefix: RolledAffix?,
    val suffix: RolledAffix?,
    val hp: Int,
    val st: Int,
    val df: Int,
    val item: Item? = null,
) {
    /** "Stout Ada Lovelace of the Bear". */
    val title: String
        get() = magicName(displayName(name), prefix, suffix)

    /** ST including the equipped item. */
    val totalSt: Int
        get() = st + (item?.stBonus ?: 0)

    fun canEquip(item: Item): Boolean = item.kind.usableBy == charClass

    fun equip(item: Item): Character {
        require(canEquip(item)) { "${charClass.label} can't use ${item.name}" }
        return copy(item = item)
    }
}

/** "ADA LOVELACE" -> "Ada Lovelace". */
fun displayName(normalized: String): String =
    normalized.lowercase(Locale.ROOT).split(' ').joinToString(" ") { word ->
        word.replaceFirstChar { it.titlecase(Locale.ROOT) }
    }
