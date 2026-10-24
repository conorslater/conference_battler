package dev.conferencebattler.gen.data

import dev.conferencebattler.model.Item
import dev.conferencebattler.model.ItemKind
import dev.conferencebattler.model.Rarity

/** Fixed-stat uniques, rolled 1 time in [UNIQUE_CHANCE] before anything else about the item. */
const val UNIQUE_CHANCE = 200

private const val UNIQUE_LEVEL = 30

private val uniqueStats = mapOf(
    ItemKind.WEAPON to listOf(
        "The Butcher's Cleaver" to 3000,
        "The Grandfather" to 3500,
        "Messerschmidt's Reaver" to 3200,
        "Black Razor" to 2800,
    ),
    ItemKind.SPELL to listOf(
        "Horazon's Inferno" to 3800,
        "Zhar's Storm" to 3400,
        "Gharbad's Folly" to 4200,
    ),
)

fun uniques(kind: ItemKind, seed: UInt): List<Item> =
    uniqueStats.getValue(kind).map { (name, st) ->
        Item(kind, name, seed, UNIQUE_LEVEL, Rarity.UNIQUE, stBonus = st)
    }
