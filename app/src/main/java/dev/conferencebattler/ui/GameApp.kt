package dev.conferencebattler.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.conferencebattler.scan.BadgeScanScreen
import dev.conferencebattler.scan.QrScanScreen

@Composable
fun GameApp(vm: GameViewModel = viewModel()) {
    when (val s = vm.screen) {
        Screen.Title -> TitleScreen(onStart = vm::newGame, onDebug = vm::openDebug)
        Screen.Debug -> DebugScreen(onBack = vm::toTitle)
        is Screen.ScanBadge -> BadgeScanScreen(
            title = "${playerLabel(s.player)}: scan your badge",
            onResult = { vm.badgeScanned(s.player, it) },
            onCancel = vm::toTitle,
        )
        is Screen.CharacterCard -> CharacterCardScreen(
            player = s.player,
            character = s.character,
            onScanItem = { vm.scanItem(s.player, s.character) },
            onSkip = { vm.ready(s.player, s.character) },
            onRescan = { vm.scanBadge(s.player) },
        )
        is Screen.ScanItem -> QrScanScreen(
            title = "${playerLabel(s.player)}: scan a QR code",
            onResult = { vm.itemScanned(s.player, s.character, it) },
            onSkip = { vm.ready(s.player, s.character) },
            onCancel = { vm.backToCharacter(s.player, s.character) },
        )
        is Screen.ItemCard -> ItemCardScreen(
            player = s.player,
            character = s.character,
            item = s.item,
            onEquip = { vm.ready(s.player, s.character.equip(s.item)) },
            onScanAnother = { vm.scanItem(s.player, s.character) },
            onSkip = { vm.ready(s.player, s.character) },
            onBack = { vm.backToCharacter(s.player, s.character) },
        )
        is Screen.Battle -> BattleScreen(
            state = s.state,
            onAttack = vm::attack,
            onRematch = vm::startBattle,
            onNewGame = vm::toTitle,
        )
    }
}
