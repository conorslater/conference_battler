package dev.conferencebattler.gen

import dev.conferencebattler.model.Affix
import dev.conferencebattler.model.Alignment
import dev.conferencebattler.model.RolledAffix
import dev.conferencebattler.rng.DiabloRandom

/**
 * Ports of DevilutionX's affix selection (Source/items.cpp). The RNG call order is kept exactly,
 * because reordering any call changes every result generated from an existing seed.
 */
internal object AffixRoller {

    /**
     * Port of `GetItemBLevel` without the item-type exceptions: an item is magic if
     * `GenerateRnd(100) <= 10 || GenerateRnd(100) <= lvl`. The second roll only happens when the
     * first one fails.
     */
    fun isMagic(rng: DiabloRandom, lvl: Int): Boolean =
        rng.generateRnd(100) <= 10 || rng.generateRnd(100) <= lvl

    /**
     * Port of `SelectAffix`. Each eligible affix goes into the pool `chance` times. To be eligible
     * its `minLevel` must be within [minLvl, maxLvl], it must not be cursed when [onlyGood] is
     * set, and its alignment must not oppose [goe].
     *
     * Uses [DiabloRandom.randomIntLessThan] instead of `generateRnd` so the INT_MIN quirk can't
     * produce a negative index. For every other value the result is the same.
     */
    fun selectAffix(
        rng: DiabloRandom,
        affixes: List<Affix>,
        minLvl: Int,
        maxLvl: Int,
        onlyGood: Boolean,
        goe: Alignment,
    ): Affix? {
        val pool = buildList {
            for (affix in affixes) {
                if (affix.minLevel < minLvl || affix.minLevel > maxLvl) continue
                if (onlyGood && !affix.useful) continue
                if (goe == Alignment.GOOD && affix.alignment == Alignment.EVIL) continue
                if (goe == Alignment.EVIL && affix.alignment == Alignment.GOOD) continue
                repeat(affix.chance) { add(affix) }
            }
        }
        if (pool.isEmpty()) return null
        return pool[rng.randomIntLessThan(pool.size)]
    }

    /**
     * Port of `GetItemPowerPrefixAndSuffix` together with the `SaveItemPower` value roll (`RndPL`).
     * As in Diablo, the prefix's value is rolled before the suffix is selected.
     */
    fun rollPrefixAndSuffix(
        rng: DiabloRandom,
        prefixes: List<Affix>,
        suffixes: List<Affix>,
        minLvl: Int,
        maxLvl: Int,
        onlyGood: Boolean = false,
    ): Pair<RolledAffix?, RolledAffix?> {
        // Same clamp as GetItemBonus.
        val clampedMin = minOf(minLvl, 25)

        var allocatePrefix = rng.flipCoin(4)
        var allocateSuffix = !rng.flipCoin(3)
        if (!allocatePrefix && !allocateSuffix) {
            // At least try and give each item a prefix or suffix
            if (rng.flipCoin()) allocatePrefix = true else allocateSuffix = true
        }
        var goe = Alignment.ANY
        var good = onlyGood
        if (!good && !rng.flipCoin(3)) good = true

        var prefix: RolledAffix? = null
        if (allocatePrefix) {
            selectAffix(rng, prefixes, clampedMin, maxLvl, good, goe)?.let {
                goe = it.alignment
                prefix = RolledAffix(it, rng.rndPL(it.param1, it.param2))
            }
        }

        var suffix: RolledAffix? = null
        if (allocateSuffix) {
            selectAffix(rng, suffixes, clampedMin, maxLvl, good, goe)?.let {
                suffix = RolledAffix(it, rng.rndPL(it.param1, it.param2))
            }
        }
        return prefix to suffix
    }

    /** Weighted pick in the style of `SelectAffix`: each entry goes into the pool `weight` times. */
    fun <T> weightedPick(rng: DiabloRandom, entries: List<T>, weight: (T) -> Int): T {
        val total = entries.sumOf(weight)
        var roll = rng.randomIntLessThan(total)
        for (entry in entries) {
            roll -= weight(entry)
            if (roll < 0) return entry
        }
        return entries.last()
    }
}
