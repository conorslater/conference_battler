package dev.conferencebattler.gen.data

import dev.conferencebattler.model.Affix
import dev.conferencebattler.model.AffixPower.ALL_PCT
import dev.conferencebattler.model.AffixPower.BONUS_FLAT
import dev.conferencebattler.model.AffixPower.BONUS_PCT
import dev.conferencebattler.model.AffixPower.DF_PCT
import dev.conferencebattler.model.AffixPower.HP_PCT
import dev.conferencebattler.model.AffixPower.ST_PCT
import dev.conferencebattler.model.Alignment.EVIL
import dev.conferencebattler.model.Alignment.GOOD

// Affix tables in the format of DevilutionX's assets/txtdata/items/item_{prefixes,suffixes}.tsv.
// The item rows keep the levels and values of Diablo's weapon affixes (BONUS_PCT is its
// "+% damage", BONUS_FLAT is DAMMOD × 100) under conference names.
// Changing any row changes the stats generated from existing seeds. Append new rows; don't
// reorder or edit the existing ones.

/** Character epithets. Rolled against the character's level (1..20). */
object CharacterAffixes {
    val prefixes = listOf(
        Affix("Jet-lagged", HP_PCT, -20, -10, 1),
        Affix("Well-rested", HP_PCT, 10, 20, 1),
        Affix("Tireless", HP_PCT, 21, 30, 5, GOOD),
        Affix("Hungover", ST_PCT, -20, -10, 1),
        Affix("Caffeinated", ST_PCT, 10, 20, 1),
        Affix("Disruptive", ST_PCT, 21, 30, 10, EVIL),
        Affix("Diplomatic", DF_PCT, 10, 20, 1),
        Affix("Unflappable", DF_PCT, 21, 30, 10, GOOD),
        Affix("Visionary", ALL_PCT, 11, 15, 15, GOOD),
    )

    val suffixes = listOf(
        Affix("Missed Sessions", ALL_PCT, -10, -5, 1),
        Affix("Small Talk", DF_PCT, 5, 10, 1),
        Affix("Hot Takes", ST_PCT, 5, 10, 1),
        Affix("the Free Buffet", HP_PCT, 15, 25, 5),
        Affix("the Lightning Talk", ST_PCT, 15, 25, 5),
        Affix("the Firewall", DF_PCT, 15, 25, 5),
        Affix("the Main Stage", ALL_PCT, 5, 10, 10),
        Affix("Thought Leadership", ST_PCT, 30, 40, 15),
    )
}

/** Item affixes, shared by attack and defence items. Item level is 2 * ilvl (2..60). */
object ItemAffixes {
    val prefixes = listOf(
        Affix("Broken", BONUS_PCT, -100, -100, 5, chance = 2),
        Affix("Soggy", BONUS_PCT, -75, -50, 3, chance = 2),
        Affix("Second-hand", BONUS_PCT, -45, -25, 1, chance = 2),
        Affix("Branded", BONUS_PCT, 20, 35, 4, chance = 2),
        Affix("Premium", BONUS_PCT, 36, 50, 6, chance = 2),
        Affix("Oversized", BONUS_PCT, 51, 65, 9, chance = 2),
        Affix("Aggressively Marketed", BONUS_PCT, 66, 80, 12, EVIL, chance = 2),
        Affix("Limited Edition", BONUS_PCT, 81, 95, 16, chance = 2),
        Affix("Enterprise", BONUS_PCT, 96, 110, 20, chance = 2),
        Affix("Gold-plated", BONUS_PCT, 111, 125, 23, chance = 2),
        Affix("Venture-backed", BONUS_PCT, 126, 150, 35, chance = 2),
        Affix("Unicorn", BONUS_PCT, 151, 175, 60, chance = 2),
        Affix("Off-brand", BONUS_PCT, -45, -25, 1, chance = 2),
        Affix("Last Year's", BONUS_PCT, -75, -50, 5, chance = 2),
        Affix("Shiny", BONUS_PCT, 20, 35, 1, chance = 2, useful = false),
        Affix("Artisanal", BONUS_PCT, 36, 50, 6, chance = 2),
        Affix("Sponsor's", BONUS_PCT, 51, 65, 10, chance = 2),
        Affix("Exhibitor's", BONUS_PCT, 66, 80, 15, chance = 2),
        Affix("Speaker's", BONUS_PCT, 81, 95, 19, chance = 2),
        Affix("VIP", BONUS_PCT, 96, 110, 23, chance = 2),
        Affix("Organiser's", BONUS_PCT, 111, 125, 28, chance = 2),
        Affix("Platinum Sponsor's", BONUS_PCT, 126, 150, 40, chance = 2),
        Affix("Founder's", BONUS_PCT, 151, 175, 28, chance = 2),
    )

    val suffixes = listOf(
        Affix("Networking", BONUS_FLAT, 100, 200, 2),
        Affix("Synergy", BONUS_FLAT, 300, 500, 7),
        Affix("Disruption", BONUS_FLAT, 600, 800, 15),
        Affix("the Expo Hall", BONUS_FLAT, 900, 1200, 25),
        Affix("the Keynote", BONUS_FLAT, 1300, 1600, 35),
        Affix("Hypergrowth", BONUS_FLAT, 1700, 2000, 60),
        Affix("Bad Wi-Fi", BONUS_FLAT, -500, -100, 1, EVIL),
        Affix("the Dead Battery", BONUS_FLAT, -1000, -600, 3, EVIL),
    )
}
