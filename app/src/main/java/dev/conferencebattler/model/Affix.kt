package dev.conferencebattler.model

/** Port of Diablo's `goodorevil`: a prefix's alignment limits which suffixes can follow it. */
enum class Alignment { ANY, GOOD, EVIL }

/**
 * What an affix modifies. This plays the role of Diablo's `item_effect_type` (IPL_*), cut down to
 * the stats this game has. Cursed affixes use negative params rather than separate *_CURSE
 * entries.
 */
enum class AffixPower {
    // Characters: percentage of HP, ST, DF, or all three.
    HP_PCT,
    ST_PCT,
    DF_PCT,
    ALL_PCT,

    // Items: percentage of the base bonus, or a flat amount added to it.
    BONUS_PCT,
    BONUS_FLAT,
}

/**
 * Port of Diablo's `PLStruct` (one row of item_prefixes.tsv / item_suffixes.tsv).
 *
 * @param useful same as `PLOk`: false for cursed affixes, which are skipped when only good
 *   affixes are allowed.
 */
data class Affix(
    val name: String,
    val power: AffixPower,
    val param1: Int,
    val param2: Int,
    val minLevel: Int,
    val alignment: Alignment = Alignment.ANY,
    val chance: Int = 1,
    val useful: Boolean = param1 >= 0,
)

/** An affix together with the value rolled for it by `RndPL(param1, param2)`. */
data class RolledAffix(val affix: Affix, val value: Int)

/** Sums the rolled values of [power] across the given affixes. */
fun totalOf(power: AffixPower, vararg affixes: RolledAffix?): Int =
    affixes.filterNotNull().filter { it.affix.power == power }.sumOf { it.value }

/** Port of `GenerateMagicItemName`: "{prefix} {base} of {suffix}". */
fun magicName(base: String, prefix: RolledAffix?, suffix: RolledAffix?): String = buildString {
    prefix?.let { append(it.affix.name).append(' ') }
    append(base)
    suffix?.let { append(" of ").append(it.affix.name) }
}
