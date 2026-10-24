package dev.conferencebattler.gen

import dev.conferencebattler.gen.data.CharacterAffixes
import dev.conferencebattler.model.AffixPower
import dev.conferencebattler.model.CharClass
import dev.conferencebattler.model.Character
import dev.conferencebattler.model.Tier
import dev.conferencebattler.model.totalOf
import dev.conferencebattler.rng.DiabloRandom

/**
 * Badge text -> [Character], in the same way as Diablo's `SetupAllItems`: seed the engine, then
 * make a fixed sequence of rolls.
 *
 * Roll order (changing it rerolls every badge):
 *  1. class: generateRnd(2), where 0 is Warrior and 1 is Wizard
 *  2. tier: weighted pick (Common 60 / Veteran 30 / Champion 9 / Legend 1)
 *  3. HP, ST, DF: randomIntBetween, in hundreds
 *  4. epithet: Legend, Champion and Veteran always roll one; Common rolls one if flipCoin(4).
 *     Uses the GetItemPowerPrefixAndSuffix port with levels tier.level / 2 .. tier.level.
 */
object CharacterGenerator {

    private data class ClassStats(val hp: IntRange, val st: IntRange, val df: IntRange)

    // In hundreds. HP keeps the original Barcode Battler ceiling of 199. ST and DF are narrower
    // than the original 0..99 so that fights last a few turns. Wizards have less ST but
    // spells give bigger bonuses than weapons.
    private val stats = mapOf(
        CharClass.WARRIOR to ClassStats(hp = 60..199, st = 15..45, df = 10..30),
        CharClass.WIZARD to ClassStats(hp = 50..170, st = 10..35, df = 10..30),
    )

    /**
     * @param badgeText all the text on the badge (OCR output or typed). Every word is hashed; see
     *   [Seeds.badgeKey].
     * @param name the part of the badge to show as the character's name. It doesn't affect stats.
     */
    fun generate(badgeText: String, name: String = badgeText): Character {
        val seed = Seeds.badgeSeed(Seeds.badgeKey(badgeText))
        val rng = DiabloRandom(seed)

        val charClass = if (rng.generateRnd(2) == 0) CharClass.WARRIOR else CharClass.WIZARD
        val tier = AffixRoller.weightedPick(rng, Tier.entries) { it.weight }
        val s = stats.getValue(charClass)
        val baseHp = rng.randomIntBetween(s.hp.first, s.hp.last) * 100
        val baseSt = rng.randomIntBetween(s.st.first, s.st.last) * 100
        val baseDf = rng.randomIntBetween(s.df.first, s.df.last) * 100

        val (prefix, suffix) = if (tier != Tier.COMMON || rng.flipCoin(4)) {
            AffixRoller.rollPrefixAndSuffix(
                rng, CharacterAffixes.prefixes, CharacterAffixes.suffixes,
                minLvl = tier.level / 2, maxLvl = tier.level,
            )
        } else {
            null to null
        }

        val all = tier.bonusPercent + totalOf(AffixPower.ALL_PCT, prefix, suffix)
        fun scaled(base: Int, power: AffixPower, floor: Int) =
            maxOf(floor, roundDown100(base * (100 + all + totalOf(power, prefix, suffix)) / 100))

        return Character(
            name = Seeds.normalizeBadge(name),
            seed = seed,
            charClass = charClass,
            tier = tier,
            prefix = prefix,
            suffix = suffix,
            hp = scaled(baseHp, AffixPower.HP_PCT, floor = 100),
            st = scaled(baseSt, AffixPower.ST_PCT, floor = 100),
            df = scaled(baseDf, AffixPower.DF_PCT, floor = 0),
        )
    }
}

/** Barcode Battler stats are always multiples of 100. */
internal fun roundDown100(value: Int): Int = Math.floorDiv(value, 100) * 100
