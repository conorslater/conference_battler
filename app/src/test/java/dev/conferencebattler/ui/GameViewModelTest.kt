package dev.conferencebattler.ui

import dev.conferencebattler.battle.Side
import dev.conferencebattler.scan.BadgeReading
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameViewModelTest {

    private fun badge(name: String) = BadgeReading(text = "DevConf\n$name", name = name)

    @Test
    fun setupFlowLeadsToBattleWithPlayerOneOnTheLeft() {
        val vm = GameViewModel()
        vm.newGame()
        assertEquals(Screen.ScanBadge(0), vm.screen)

        vm.badgeScanned(0, badge("Ada Lovelace"))
        val p1 = (vm.screen as Screen.CharacterCard).character
        vm.ready(0, p1)
        assertEquals(Screen.ScanBadge(1), vm.screen)

        vm.badgeScanned(1, badge("Alan Turing"))
        val p2 = (vm.screen as Screen.CharacterCard).character
        vm.scanItem(1, p2)
        vm.itemScanned(1, p2, "https://example.com")
        val card = vm.screen as Screen.ItemCard
        vm.ready(1, if (p2.canEquip(card.item)) p2.equip(card.item) else p2)

        val battle = (vm.screen as Screen.Battle).state
        assertEquals("ADA LOVELACE", battle.left.character.name)
        assertEquals("ALAN TURING", battle.right.character.name)
    }

    @Test
    fun eitherPlayerCanGoFirst() {
        val vm = GameViewModel()
        vm.newGame()
        vm.badgeScanned(0, badge("Ada Lovelace"))
        vm.ready(0, (vm.screen as Screen.CharacterCard).character)
        vm.badgeScanned(1, badge("Alan Turing"))
        vm.ready(1, (vm.screen as Screen.CharacterCard).character)
        val first = (1..50).map { vm.startBattle(); (vm.screen as Screen.Battle).state.turn }.toSet()
        assertTrue(first.containsAll(Side.entries))
    }
}
