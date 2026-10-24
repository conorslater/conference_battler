package dev.conferencebattler.model

import java.util.Locale

/**
 * A character generated from a badge. [hp], [st] and [df] already include the level bonus and the
 * epithet affixes. The equipped [item] adds to ST or DF; see [totalSt] and [totalDf].
 *
 * @param name the normalized display name (the badge's largest line). The seed comes from all of
 *   the badge's text, not just this.
 * @param role the conference job title, e.g. "Sales Engineer".
 * @param level 1..20. Low levels are the most common.
 */
data class Character(
    val name: String,
    val seed: UInt,
    val role: String,
    val level: Int,
    val prefix: RolledAffix?,
    val suffix: RolledAffix?,
    val hp: Int,
    val st: Int,
    val df: Int,
    val item: Item? = null,
) {
    /** "Caffeinated Ada Lovelace of Hot Takes". */
    val title: String
        get() = magicName(displayName(name), prefix, suffix)

    /** "Level 10 Marketeer". */
    val rank: String
        get() = "Level $level $role"

    /** ST including an equipped attack item. */
    val totalSt: Int
        get() = st + bonusFor(ItemKind.ATTACK)

    /** DF including an equipped defence item. */
    val totalDf: Int
        get() = df + bonusFor(ItemKind.DEFENCE)

    fun equip(item: Item): Character = copy(item = item)

    private fun bonusFor(kind: ItemKind): Int = item?.takeIf { it.kind == kind }?.bonus ?: 0
}

/** "ADA LOVELACE" -> "Ada Lovelace". */
fun displayName(normalized: String): String =
    normalized.lowercase(Locale.ROOT).split(' ').joinToString(" ") { word ->
        word.replaceFirstChar { it.titlecase(Locale.ROOT) }
    }
