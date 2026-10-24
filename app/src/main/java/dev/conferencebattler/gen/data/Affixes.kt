package dev.conferencebattler.gen.data

import dev.conferencebattler.model.Affix
import dev.conferencebattler.model.AffixPower.ALL_PCT
import dev.conferencebattler.model.AffixPower.DAMAGE_PCT
import dev.conferencebattler.model.AffixPower.DF_PCT
import dev.conferencebattler.model.AffixPower.HP_PCT
import dev.conferencebattler.model.AffixPower.ST_FLAT
import dev.conferencebattler.model.AffixPower.ST_PCT
import dev.conferencebattler.model.Alignment.EVIL
import dev.conferencebattler.model.Alignment.GOOD

// Affix tables in the format of DevilutionX's assets/txtdata/items/item_{prefixes,suffixes}.tsv.
// Weapon names, levels and DAMAGE_PCT values come from those files. ST_FLAT is Diablo's
// DAMMOD × 100, to match Barcode Battler's stats in hundreds.
// Changing any row changes the stats generated from existing seeds. Append new rows; don't
// reorder or edit the existing ones.

/** Character epithets. Rolled against the tier's level (1/5/10/15). */
object CharacterAffixes {
    val prefixes = listOf(
        Affix("Sickly", HP_PCT, -20, -10, 1),
        Affix("Stout", HP_PCT, 10, 20, 1),
        Affix("Hardy", HP_PCT, 21, 30, 5, GOOD),
        Affix("Feeble", ST_PCT, -20, -10, 1),
        Affix("Strong", ST_PCT, 10, 20, 1),
        Affix("Fierce", ST_PCT, 21, 30, 10, EVIL),
        Affix("Guarded", DF_PCT, 10, 20, 1),
        Affix("Valiant", DF_PCT, 21, 30, 10, GOOD),
        Affix("Godly", ALL_PCT, 11, 15, 15, GOOD),
    )

    val suffixes = listOf(
        Affix("the Mouse", ALL_PCT, -10, -5, 1),
        Affix("the Fox", DF_PCT, 5, 10, 1),
        Affix("the Wolf", ST_PCT, 5, 10, 1),
        Affix("the Bear", HP_PCT, 15, 25, 5),
        Affix("the Bull", ST_PCT, 15, 25, 5),
        Affix("the Turtle", DF_PCT, 15, 25, 5),
        Affix("the Heavens", ALL_PCT, 5, 10, 10),
        Affix("Titans", ST_PCT, 30, 40, 15),
    )
}

/** Weapon affixes. Item level is 2 * ilvl (2..60), as for monster drops (`SpawnItem`). */
object WeaponAffixes {
    val prefixes = listOf(
        Affix("Useless", DAMAGE_PCT, -100, -100, 5, chance = 2),
        Affix("Bent", DAMAGE_PCT, -75, -50, 3, chance = 2),
        Affix("Weak", DAMAGE_PCT, -45, -25, 1, chance = 2),
        Affix("Jagged", DAMAGE_PCT, 20, 35, 4, chance = 2),
        Affix("Deadly", DAMAGE_PCT, 36, 50, 6, chance = 2),
        Affix("Heavy", DAMAGE_PCT, 51, 65, 9, chance = 2),
        Affix("Vicious", DAMAGE_PCT, 66, 80, 12, EVIL, chance = 2),
        Affix("Brutal", DAMAGE_PCT, 81, 95, 16, chance = 2),
        Affix("Massive", DAMAGE_PCT, 96, 110, 20, chance = 2),
        Affix("Savage", DAMAGE_PCT, 111, 125, 23, chance = 2),
        Affix("Ruthless", DAMAGE_PCT, 126, 150, 35, chance = 2),
        Affix("Merciless", DAMAGE_PCT, 151, 175, 60, chance = 2),
        Affix("Dull", DAMAGE_PCT, -45, -25, 1, chance = 2),
        Affix("Clumsy", DAMAGE_PCT, -75, -50, 5, chance = 2),
        Affix("Sharp", DAMAGE_PCT, 20, 35, 1, chance = 2, useful = false),
        Affix("Fine", DAMAGE_PCT, 36, 50, 6, chance = 2),
        Affix("Warrior's", DAMAGE_PCT, 51, 65, 10, chance = 2),
        Affix("Soldier's", DAMAGE_PCT, 66, 80, 15, chance = 2),
        Affix("Lord's", DAMAGE_PCT, 81, 95, 19, chance = 2),
        Affix("Knight's", DAMAGE_PCT, 96, 110, 23, chance = 2),
        Affix("Master's", DAMAGE_PCT, 111, 125, 28, chance = 2),
        Affix("Champion's", DAMAGE_PCT, 126, 150, 40, chance = 2),
        Affix("King's", DAMAGE_PCT, 151, 175, 28, chance = 2),
    )

    val suffixes = listOf(
        Affix("quality", ST_FLAT, 100, 200, 2),
        Affix("maiming", ST_FLAT, 300, 500, 7),
        Affix("slaying", ST_FLAT, 600, 800, 15),
        Affix("gore", ST_FLAT, 900, 1200, 25),
        Affix("carnage", ST_FLAT, 1300, 1600, 35),
        Affix("slaughter", ST_FLAT, 1700, 2000, 60),
        Affix("weakness", ST_FLAT, -500, -100, 1, EVIL),
        Affix("frailty", ST_FLAT, -1000, -600, 3, EVIL),
    )
}

/** Spell affixes. These are based on Diablo's staff and magic affixes. */
object SpellAffixes {
    val prefixes = listOf(
        Affix("Dull", DAMAGE_PCT, -45, -25, 1),
        Affix("Clumsy", DAMAGE_PCT, -75, -50, 5),
        Affix("Sharp", DAMAGE_PCT, 20, 35, 1),
        Affix("Fine", DAMAGE_PCT, 36, 50, 6),
        Affix("Angel's", DAMAGE_PCT, 51, 65, 15, GOOD),
        Affix("Arch-Angel's", DAMAGE_PCT, 66, 90, 25, GOOD),
    )

    val suffixes = listOf(
        Affix("magic", ST_FLAT, 100, 300, 1),
        Affix("the mind", ST_FLAT, 400, 600, 5),
        Affix("brilliance", ST_FLAT, 700, 1000, 11),
        Affix("sorcery", ST_FLAT, 1100, 1500, 17),
        Affix("wizardry", ST_FLAT, 1600, 2000, 25),
        Affix("dyslexia", ST_FLAT, -500, -100, 1, EVIL),
        Affix("the fool", ST_FLAT, -1000, -600, 3, EVIL),
    )
}
