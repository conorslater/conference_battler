package dev.conferencebattler.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dev.conferencebattler.battle.BattleEngine
import dev.conferencebattler.battle.BattleState
import dev.conferencebattler.gen.CharacterGenerator
import dev.conferencebattler.gen.ItemGenerator
import dev.conferencebattler.model.Character
import dev.conferencebattler.model.Item
import dev.conferencebattler.rng.DiabloRandom
import dev.conferencebattler.scan.BadgeReading

/** Where the game is. Setup screens carry the player index (0 or 1) and their character so far. */
sealed interface Screen {
    data object Title : Screen
    data object Debug : Screen
    data class ScanBadge(val player: Int) : Screen
    data class CharacterCard(val player: Int, val character: Character) : Screen
    data class ScanItem(val player: Int, val character: Character) : Screen
    data class ItemCard(val player: Int, val character: Character, val item: Item) : Screen
    data class Battle(val state: BattleState) : Screen
}

/**
 * Game flow: Title → for each player, ScanBadge → CharacterCard → (ScanItem → ItemCard) → Battle.
 * Player 1 fights on the left, player 2 on the right.
 */
class GameViewModel : ViewModel() {

    var screen by mutableStateOf<Screen>(Screen.Title)
        private set

    private val players = arrayOfNulls<Character>(2)

    // Seeded from the clock so rematches play out differently. Characters and items stay
    // deterministic because they have their own seeds.
    private val engine = BattleEngine(DiabloRandom(System.nanoTime().toUInt()))

    fun newGame() {
        players.fill(null)
        screen = Screen.ScanBadge(0)
    }

    fun toTitle() {
        screen = Screen.Title
    }

    fun openDebug() {
        screen = Screen.Debug
    }

    fun scanBadge(player: Int) {
        screen = Screen.ScanBadge(player)
    }

    fun badgeScanned(player: Int, reading: BadgeReading) {
        screen = Screen.CharacterCard(player, CharacterGenerator.generate(reading.text, reading.name))
    }

    fun scanItem(player: Int, character: Character) {
        screen = Screen.ScanItem(player, character)
    }

    fun itemScanned(player: Int, character: Character, payload: String) {
        screen = Screen.ItemCard(player, character, ItemGenerator.generate(payload))
    }

    /** Back from an item screen to the character card, without the item. */
    fun backToCharacter(player: Int, character: Character) {
        screen = Screen.CharacterCard(player, character)
    }

    /** [character] is final (with or without an item). Moves on to player 2 or the battle. */
    fun ready(player: Int, character: Character) {
        players[player] = character
        if (player == 0) screen = Screen.ScanBadge(1) else startBattle()
    }

    /** Same two characters, fresh HP and a new coin flip for who goes first. */
    fun startBattle() {
        val (left, right) = players
        screen = Screen.Battle(engine.start(left!!, right!!))
    }

    fun attack() {
        val battle = screen as? Screen.Battle ?: return
        if (battle.state.winner != null) return
        screen = battle.copy(state = engine.attack(battle.state))
    }
}
