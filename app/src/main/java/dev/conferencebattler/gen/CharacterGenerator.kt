package dev.conferencebattler.gen

import dev.conferencebattler.gen.data.CharacterAffixes
import dev.conferencebattler.gen.data.Roles
import dev.conferencebattler.model.AffixPower
import dev.conferencebattler.model.Character
import dev.conferencebattler.model.totalOf
import dev.conferencebattler.rng.DiabloRandom

/**
 * Badge text -> [Character], in the same way as Diablo's `SetupAllItems`: seed the engine, then
 * make a fixed sequence of rolls.
 *
 * Roll order (changing it rerolls every badge):
 *  1. role: weighted pick over [Roles]
 *  2. level: weighted pick over 1..MAX_LEVEL, where level n has weight MAX_LEVEL + 1 - n
 *  3. HP, ST, DF: randomIntBetween over the role's ranges, in hundreds
 *  4. epithet: level 5 and up always rolls one; below that, one if flipCoin(4).
 *     Uses the GetItemPowerPrefixAndSuffix port with levels level / 2 .. level.
 */
object CharacterGenerator {

    const val MAX_LEVEL = 20

    /** Each level above 1 adds this percentage to HP, ST and DF. */
    private const val LEVEL_BONUS_PERCENT = 2

    private val levels = (1..MAX_LEVEL).toList()

    /**
     * @param badgeText all the text on the badge (OCR output or typed). Every word is hashed; see
     *   [Seeds.badgeKey].
     * @param name the part of the badge to show as the character's name. It doesn't affect stats.
     */
    fun generate(badgeText: String, name: String = badgeText): Character {
        val seed = Seeds.badgeSeed(Seeds.badgeKey(badgeText))
        val rng = DiabloRandom(seed)

        val role = AffixRoller.weightedPick(rng, Roles) { it.weight }
        val level = AffixRoller.weightedPick(rng, levels) { MAX_LEVEL + 1 - it }
        val baseHp = rng.randomIntBetween(role.hp.first, role.hp.last) * 100
        val baseSt = rng.randomIntBetween(role.st.first, role.st.last) * 100
        val baseDf = rng.randomIntBetween(role.df.first, role.df.last) * 100

        val (prefix, suffix) = if (level >= 5 || rng.flipCoin(4)) {
            AffixRoller.rollPrefixAndSuffix(
                rng, CharacterAffixes.prefixes, CharacterAffixes.suffixes,
                minLvl = level / 2, maxLvl = level,
            )
        } else {
            null to null
        }

        val all = (level - 1) * LEVEL_BONUS_PERCENT + totalOf(AffixPower.ALL_PCT, prefix, suffix)
        fun scaled(base: Int, power: AffixPower, floor: Int) =
            maxOf(floor, roundDown100(base * (100 + all + totalOf(power, prefix, suffix)) / 100))

        return Character(
            name = Seeds.normalizeBadge(name),
            seed = seed,
            role = role.name,
            level = level,
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
