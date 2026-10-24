package dev.conferencebattler.gen.data

import dev.conferencebattler.model.Item
import dev.conferencebattler.model.ItemKind
import dev.conferencebattler.model.Rarity

/** Fixed-stat uniques, rolled 1 time in [UNIQUE_CHANCE] before anything else about the item. */
const val UNIQUE_CHANCE = 200

private const val UNIQUE_LEVEL = 30

private val uniqueStats = mapOf(
    ItemKind.ATTACK to listOf(
        "The CEO's Fountain Pen" to 3000,
        "Triple-shot Espresso" to 3500,
        "The Golden Clicker" to 3200,
        "Last Box of Conference Donuts" to 2800,
    ),
    ItemKind.DEFENCE to listOf(
        "All-access Backstage Pass" to 2400,
        "The Sponsor's Gilet" to 2000,
        "Fully Charged Power Bank" to 2800,
    ),
)

fun uniques(kind: ItemKind, seed: UInt): List<Item> =
    uniqueStats.getValue(kind).map { (name, bonus) ->
        Item(kind, name, seed, UNIQUE_LEVEL, Rarity.UNIQUE, bonus = bonus)
    }
