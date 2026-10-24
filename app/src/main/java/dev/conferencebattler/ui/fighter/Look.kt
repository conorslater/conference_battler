package dev.conferencebattler.ui.fighter

import androidx.compose.ui.graphics.Color
import dev.conferencebattler.model.Character
import dev.conferencebattler.rng.DiabloRandom

enum class Outfit { TEE, HOODIE, POLO, SHIRT, SHIRT_AND_TIE, BLAZER, TURTLENECK }

enum class Hair { SHORT, LONG, BUN, CURLY, BALD }

enum class Extra { NONE, CAP, BACKPACK, HEADSET }

/** How a fighter is drawn. Everyone wears a lanyard and badge. */
data class Look(
    val outfit: Outfit,
    val extra: Extra,
    val hair: Hair,
    val glasses: Boolean,
    val skin: Color,
    val hairColor: Color,
    val top: Color,
    /** Shirt under a blazer, or the tie. */
    val accent: Color,
    val trousers: Color,
    val shoes: Color,
    val badgeStripe: Color,
    /** The equipped item, worn or held. */
    val gear: Gear? = null,
)

private val skins = listOf(0xFFF6D7C3, 0xFFEAC09A, 0xFFD39B6A, 0xFFB07548, 0xFF8A5530, 0xFF5E3A22)
private val hairColors = listOf(0xFF1E1A18, 0xFF3B2A20, 0xFF6B4423, 0xFFA0522D, 0xFFD8B26E, 0xFF9E9E9E)
internal val brights = listOf(0xFFE53935, 0xFF1E88E5, 0xFF43A047, 0xFFFB8C00, 0xFF8E24AA, 0xFF00ACC1, 0xFFFDD835)
private val darks = listOf(0xFF263238, 0xFF37474F, 0xFF212121, 0xFF3E2723, 0xFF1A237E, 0xFF4E342E)
private val shirts = listOf(0xFFFFFFFF, 0xFFBBDEFB, 0xFFE1F5FE, 0xFFF3E5F5, 0xFFFFF8E1)
private val trousers = listOf(0xFF283593, 0xFF263238, 0xFF5D4037, 0xFF424242, 0xFF8D6E63, 0xFF1565C0)
private val badgeStripes = listOf(0xFFE53935, 0xFF1E88E5, 0xFF43A047, 0xFFFB8C00, 0xFF8E24AA)

/** Keeps the look's rolls apart from the stat rolls made with the same badge seed. */
private const val LOOK_SALT = 0x10C5EEDu

/**
 * The outfit comes from the role; colours, hair and glasses come from the badge seed, so a badge
 * always looks the same. Purely cosmetic: none of this changes stats.
 */
fun lookFor(c: Character): Look {
    val rng = DiabloRandom(c.seed xor LOOK_SALT)
    fun pick(colors: List<Long>) = Color(rng.pickRandomlyAmong(colors))

    val (outfit, extra) = when (c.role) {
        "Developer" -> Outfit.HOODIE to Extra.NONE
        "Marketeer" -> Outfit.BLAZER to Extra.NONE
        "Sales Engineer" -> Outfit.POLO to Extra.NONE
        "Booth Staffer" -> Outfit.TEE to Extra.CAP
        "Product Manager" -> Outfit.SHIRT to Extra.NONE
        "Recruiter" -> Outfit.SHIRT_AND_TIE to Extra.NONE
        "Developer Advocate" -> Outfit.TEE to Extra.NONE
        "Intern" -> Outfit.TEE to Extra.BACKPACK
        "CTO" -> Outfit.BLAZER to Extra.NONE
        "Keynote Speaker" -> Outfit.TURTLENECK to Extra.HEADSET
        else -> Outfit.TEE to Extra.NONE
    }
    val skin = pick(skins)
    val hair = if (extra == Extra.CAP) Hair.SHORT else rng.pickRandomlyAmong(Hair.entries)
    val hairColor = pick(hairColors)
    val glasses = rng.flipCoin(if (c.role == "Developer") 2 else 4)
    val top = when (outfit) {
        Outfit.TEE, Outfit.HOODIE, Outfit.POLO -> pick(brights)
        Outfit.SHIRT, Outfit.SHIRT_AND_TIE -> pick(shirts)
        Outfit.BLAZER -> pick(darks)
        Outfit.TURTLENECK -> Color(0xFF111111)
    }
    val accent = when (outfit) {
        Outfit.SHIRT_AND_TIE -> pick(brights)
        Outfit.BLAZER -> if (c.role == "CTO") pick(brights) else pick(shirts)
        else -> pick(darks)
    }
    return Look(
        outfit = outfit,
        extra = extra,
        hair = hair,
        glasses = glasses,
        skin = skin,
        hairColor = hairColor,
        top = top,
        accent = accent,
        trousers = pick(trousers),
        shoes = if (rng.flipCoin(3)) Color.White else Color(0xFF1B1B1B),
        badgeStripe = pick(badgeStripes),
        gear = gearFor(c.item),
    )
}
