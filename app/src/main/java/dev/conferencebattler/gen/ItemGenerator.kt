package dev.conferencebattler.gen

import dev.conferencebattler.gen.data.ItemBase
import dev.conferencebattler.gen.data.SpellAffixes
import dev.conferencebattler.gen.data.SpellBases
import dev.conferencebattler.gen.data.UNIQUE_CHANCE
import dev.conferencebattler.gen.data.WeaponAffixes
import dev.conferencebattler.gen.data.WeaponBases
import dev.conferencebattler.gen.data.uniques
import dev.conferencebattler.model.Affix
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
 *  1. kind: generateRnd(2), where 0 is Weapon and 1 is Spell
 *  2. unique: flipCoin(UNIQUE_CHANCE). If it succeeds, pickRandomlyAmong the uniques and stop
 *  3. ilvl: randomIntBetween(1, 30)
 *  4. base: randomIntLessThan over the bases with minLevel <= ilvl
 *  5. base ST bonus: randomIntBetween, in hundreds
 *  6. magic: GetItemBLevel port with lvl = 2 * ilvl (like monster drops)
 *  7. if magic: GetItemPowerPrefixAndSuffix port with levels lvl / 2 .. lvl
 */
object ItemGenerator {

    private const val MAX_ITEM_LEVEL = 30

    private class KindData(val bases: List<ItemBase>, val prefixes: List<Affix>, val suffixes: List<Affix>)

    private val kinds = mapOf(
        ItemKind.WEAPON to KindData(WeaponBases, WeaponAffixes.prefixes, WeaponAffixes.suffixes),
        ItemKind.SPELL to KindData(SpellBases, SpellAffixes.prefixes, SpellAffixes.suffixes),
    )

    fun generate(payload: String): Item {
        val seed = Seeds.itemSeed(payload)
        val rng = DiabloRandom(seed)
        val kind = if (rng.generateRnd(2) == 0) ItemKind.WEAPON else ItemKind.SPELL

        if (rng.flipCoin(UNIQUE_CHANCE)) return rng.pickRandomlyAmong(uniques(kind, seed))

        val data = kinds.getValue(kind)
        val ilvl = rng.randomIntBetween(1, MAX_ITEM_LEVEL)
        val base = rng.pickRandomlyAmong(data.bases.filter { it.minLevel <= ilvl })
        val baseSt = rng.randomIntBetween(base.minSt / 100, base.maxSt / 100) * 100

        val lvl = 2 * ilvl
        val (prefix, suffix) = if (AffixRoller.isMagic(rng, lvl)) {
            AffixRoller.rollPrefixAndSuffix(rng, data.prefixes, data.suffixes, minLvl = lvl / 2, maxLvl = lvl)
        } else {
            null to null
        }

        val pct = totalOf(AffixPower.DAMAGE_PCT, prefix, suffix)
        val stBonus = baseSt * (100 + pct) / 100 + totalOf(AffixPower.ST_FLAT, prefix, suffix)
        return Item(
            kind = kind,
            name = magicName(base.name, prefix, suffix),
            seed = seed,
            level = ilvl,
            rarity = if (prefix != null || suffix != null) Rarity.MAGIC else Rarity.NORMAL,
            stBonus = maxOf(0, roundDown100(stBonus)),
            prefix = prefix,
            suffix = suffix,
        )
    }
}
