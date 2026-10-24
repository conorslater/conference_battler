package dev.conferencebattler.gen

import dev.conferencebattler.gen.data.AttackBases
import dev.conferencebattler.gen.data.DefenceBases
import dev.conferencebattler.gen.data.ItemAffixes
import dev.conferencebattler.gen.data.UNIQUE_CHANCE
import dev.conferencebattler.gen.data.uniques
import dev.conferencebattler.model.AffixPower
import dev.conferencebattler.model.Item
import dev.conferencebattler.model.ItemKind
import dev.conferencebattler.model.Rarity
import dev.conferencebattler.model.magicName
import dev.conferencebattler.model.totalOf
import dev.conferencebattler.rng.DiabloRandom

/**
 * QR payload -> [Item], following the structure of Diablo's `SetupAllItems`.
 *
 * Roll order (changing it rerolls every QR code):
 *  1. kind: generateRnd(2), where 0 is Attack and 1 is Defence
 *  2. unique: flipCoin(UNIQUE_CHANCE). If it succeeds, pickRandomlyAmong the uniques and stop
 *  3. ilvl: randomIntBetween(1, 30)
 *  4. base: randomIntLessThan over the bases with minLevel <= ilvl
 *  5. base bonus: randomIntBetween, in hundreds
 *  6. magic: GetItemBLevel port with lvl = 2 * ilvl (like monster drops)
 *  7. if magic: GetItemPowerPrefixAndSuffix port with levels lvl / 2 .. lvl
 */
object ItemGenerator {

    private const val MAX_ITEM_LEVEL = 30

    private val bases = mapOf(ItemKind.ATTACK to AttackBases, ItemKind.DEFENCE to DefenceBases)

    fun generate(payload: String): Item {
        val seed = Seeds.itemSeed(payload)
        val rng = DiabloRandom(seed)
        val kind = if (rng.generateRnd(2) == 0) ItemKind.ATTACK else ItemKind.DEFENCE

        if (rng.flipCoin(UNIQUE_CHANCE)) return rng.pickRandomlyAmong(uniques(kind, seed))

        val ilvl = rng.randomIntBetween(1, MAX_ITEM_LEVEL)
        val base = rng.pickRandomlyAmong(bases.getValue(kind).filter { it.minLevel <= ilvl })
        val baseBonus = rng.randomIntBetween(base.minBonus / 100, base.maxBonus / 100) * 100

        val lvl = 2 * ilvl
        val (prefix, suffix) = if (AffixRoller.isMagic(rng, lvl)) {
            AffixRoller.rollPrefixAndSuffix(
                rng, ItemAffixes.prefixes, ItemAffixes.suffixes, minLvl = lvl / 2, maxLvl = lvl,
            )
        } else {
            null to null
        }

        val pct = totalOf(AffixPower.BONUS_PCT, prefix, suffix)
        val bonus = baseBonus * (100 + pct) / 100 + totalOf(AffixPower.BONUS_FLAT, prefix, suffix)
        return Item(
            kind = kind,
            name = magicName(base.name, prefix, suffix),
            seed = seed,
            level = ilvl,
            rarity = if (prefix != null || suffix != null) Rarity.MAGIC else Rarity.NORMAL,
            bonus = maxOf(0, roundDown100(bonus)),
            prefix = prefix,
            suffix = suffix,
        )
    }
}
