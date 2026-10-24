package dev.conferencebattler.ui.fighter

import androidx.compose.ui.graphics.Color
import dev.conferencebattler.model.Item
import dev.conferencebattler.model.Rarity
import dev.conferencebattler.rng.DiabloRandom
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** When a piece of gear is drawn, relative to the body. */
enum class Layer {
    /** Over the torso, under the lanyard. */
    BODY,

    /** Over the name badge. */
    CHEST,

    /** Over the head. */
    HEAD,

    /** In the front hand, over the arm: swung with every punch. */
    HAND,
}

/** What an item looks like on a fighter. */
enum class Prop(val layer: Layer) {
    PEN(Layer.HAND),
    CARD(Layer.HAND),
    FLYER(Layer.HAND),
    STRESS_BALL(Layer.HAND),
    LANYARD(Layer.HAND),
    COFFEE(Layer.HAND),
    USB_STICK(Layer.HAND),
    ENERGY_DRINK(Layer.HAND),
    LASER_POINTER(Layer.HAND),
    CLICKER(Layer.HAND),
    SELFIE_STICK(Layer.HAND),
    WATER_BOTTLE(Layer.HAND),
    SWAG_BAG(Layer.HAND),
    MICROPHONE(Layer.HAND),
    ESPRESSO_MACHINE(Layer.HAND),
    PRIZE_BOWL(Layer.HAND),
    PODIUM(Layer.HAND),
    DONUTS(Layer.HAND),

    BADGE_HOLDER(Layer.CHEST),
    T_SHIRT(Layer.BODY),
    TOTE_BAG(Layer.BODY),
    LAPTOP(Layer.HAND),
    HOODIE(Layer.BODY),
    BROCHURE_STAND(Layer.HAND),
    UMBRELLA(Layer.HAND),
    HEADPHONES(Layer.HEAD),
    PULL_UP_BANNER(Layer.HAND),
    TABLE(Layer.HAND),
    VIP_PASS(Layer.CHEST),
    BACKDROP(Layer.HAND),
    GILET(Layer.BODY),
    POWER_BANK(Layer.HAND),
}

/** An item as drawn: its [prop] in its brand [color]. */
data class Gear(val prop: Prop, val color: Color)

/** Base and unique item names. A magic item's name contains its base name between the affixes. */
private val propsByName = mapOf(
    "Free Pen" to Prop.PEN,
    "Business Card" to Prop.CARD,
    "Rolled-up Flyer" to Prop.FLYER,
    "Stress Ball" to Prop.STRESS_BALL,
    "Branded Lanyard" to Prop.LANYARD,
    "Hot Coffee" to Prop.COFFEE,
    "USB Stick" to Prop.USB_STICK,
    "Energy Drink" to Prop.ENERGY_DRINK,
    "Laser Pointer" to Prop.LASER_POINTER,
    "Presentation Clicker" to Prop.CLICKER,
    "Selfie Stick" to Prop.SELFIE_STICK,
    "Branded Water Bottle" to Prop.WATER_BOTTLE,
    "Swag Bag" to Prop.SWAG_BAG,
    "Wireless Microphone" to Prop.MICROPHONE,
    "Espresso Machine" to Prop.ESPRESSO_MACHINE,
    "Prize Draw Bowl" to Prop.PRIZE_BOWL,
    "Keynote Podium" to Prop.PODIUM,
    "Badge Holder" to Prop.BADGE_HOLDER,
    "Promotional T-shirt" to Prop.T_SHIRT,
    "Tote Bag" to Prop.TOTE_BAG,
    "Sticker-covered Laptop" to Prop.LAPTOP,
    "Branded Hoodie" to Prop.HOODIE,
    "Brochure Stand" to Prop.BROCHURE_STAND,
    "Branded Umbrella" to Prop.UMBRELLA,
    "Noise-cancelling Headphones" to Prop.HEADPHONES,
    "Pull-up Banner" to Prop.PULL_UP_BANNER,
    "Exhibitor Table" to Prop.TABLE,
    "Speaker Lounge Pass" to Prop.VIP_PASS,
    "Booth Backdrop" to Prop.BACKDROP,
    // Uniques.
    "The CEO's Fountain Pen" to Prop.PEN,
    "Triple-shot Espresso" to Prop.COFFEE,
    "The Golden Clicker" to Prop.CLICKER,
    "Last Box of Conference Donuts" to Prop.DONUTS,
    "All-access Backstage Pass" to Prop.VIP_PASS,
    "The Sponsor's Gilet" to Prop.GILET,
    "Fully Charged Power Bank" to Prop.POWER_BANK,
)

private const val GEAR_SALT = 0x5EA6u
private val GOLD = Color(0xFFFFC107)

/** The prop for [item], or null for an unknown name. The colour comes from the QR seed; uniques are gold. */
fun gearFor(item: Item?): Gear? {
    item ?: return null
    // Longest first, so "Espresso Machine" can't be mistaken for anything shorter.
    val prop = propsByName.entries.sortedByDescending { it.key.length }
        .firstOrNull { item.name.contains(it.key) }?.value ?: return null
    val color = if (item.rarity == Rarity.UNIQUE) GOLD else Color(DiabloRandom(item.seed xor GEAR_SALT).pickRandomlyAmong(brights))
    return Gear(prop, color)
}

/** Clothing items replace the outfit. */
internal fun wearing(look: Look, gear: Gear): Look = when (gear.prop) {
    Prop.T_SHIRT -> look.copy(outfit = Outfit.TEE, top = gear.color)
    Prop.HOODIE -> look.copy(outfit = Outfit.HOODIE, top = gear.color)
    else -> look
}

private val DARK = Color(0xFF263238)
private val SILVER = Color(0xFFCFD8DC)
private val WHITE = Color(0xFFF5F5F5)

/**
 * Draws the part of [gear] that belongs on [layer]. Held things are drawn relative to the front
 * hand: swung props point along the forearm, tilted up like a raised sword, and the rest stay
 * upright with the torso, held up like a shield.
 */
internal fun FigurePen.drawGear(gear: Gear, layer: Layer, sk: Skeleton, up: Vec, fwd: Vec) {
    if (gear.prop.layer != layer) return
    val color = gear.color
    val grip = sk.frontHand
    val a = (sk.frontHand - sk.frontElbow).unit().rotate(35f)
    val shield = grip + fwd * 0.03f

    when (gear.prop) {
        Prop.PEN -> {
            line(grip - a * 0.03f, grip + a * 0.12f, color, 0.018f)
            line(grip + a * 0.12f, grip + a * 0.145f, DARK, 0.01f)
            line(grip + a * 0.07f + a.across * 0.012f, grip + a * 0.11f + a.across * 0.012f, SILVER, 0.006f)
        }
        Prop.CARD -> {
            box(grip + a * 0.06f, a, 0.032f, 0.05f, WHITE)
            box(grip + a * 0.095f, a, 0.032f, 0.012f, color)
        }
        Prop.FLYER -> {
            line(grip - a * 0.05f, grip + a * 0.2f, WHITE, 0.04f)
            line(grip + a * 0.08f, grip + a * 0.12f, color, 0.042f)
        }
        Prop.STRESS_BALL -> dot(grip + a * 0.04f, color, 0.045f)
        Prop.LANYARD -> {
            val mid = grip + a * 0.12f + a.across * 0.03f
            val end = grip + a * 0.22f
            line(grip, mid, color, 0.014f)
            line(mid, end, color, 0.014f)
            dot(end, SILVER, 0.015f)
        }
        Prop.COFFEE -> {
            val cup = grip + up * 0.02f
            polygon(
                WHITE,
                cup - up * 0.05f - fwd * 0.026f, cup - up * 0.05f + fwd * 0.026f,
                cup + up * 0.05f + fwd * 0.034f, cup + up * 0.05f - fwd * 0.034f,
            )
            box(cup, up, 0.031f, 0.02f, color)
            line(cup + up * 0.055f - fwd * 0.034f, cup + up * 0.055f + fwd * 0.034f, DARK, 0.015f)
        }
        Prop.USB_STICK -> {
            box(grip + a * 0.04f, a, 0.016f, 0.04f, color)
            box(grip + a * 0.09f, a, 0.011f, 0.012f, SILVER)
        }
        Prop.ENERGY_DRINK -> {
            box(grip + up * 0.01f, up, 0.026f, 0.055f, color)
            line(grip + up * 0.065f - fwd * 0.022f, grip + up * 0.065f + fwd * 0.022f, SILVER, 0.012f)
        }
        Prop.LASER_POINTER -> {
            val tip = grip + a * 0.1f
            line(tip, tip + a * 0.6f, Color(0x99FF1744), 0.006f)
            line(grip - a * 0.03f, tip, DARK, 0.02f)
            dot(tip, Color(0xFFFF1744), 0.01f)
        }
        Prop.CLICKER -> {
            box(grip + a * 0.035f, a, 0.022f, 0.045f, if (color == GOLD) GOLD else DARK)
            dot(grip + a * 0.055f, Color(0xFFFF1744), 0.009f)
        }
        Prop.SELFIE_STICK -> {
            line(grip - a * 0.03f, grip + a * 0.38f, Color(0xFF9E9E9E), 0.012f)
            box(grip + a * 0.42f, a, 0.03f, 0.05f, DARK)
            box(grip + a * 0.42f, a, 0.022f, 0.04f, color)
        }
        Prop.WATER_BOTTLE -> {
            box(grip + a * 0.05f, a, 0.025f, 0.08f, color)
            box(grip + a * 0.14f, a, 0.018f, 0.012f, WHITE)
        }
        Prop.SWAG_BAG -> {
            val bag = grip - up * 0.11f
            line(grip, bag + up * 0.06f - fwd * 0.04f, DARK, 0.008f)
            line(grip, bag + up * 0.06f + fwd * 0.04f, DARK, 0.008f)
            box(bag, up, 0.07f, 0.065f, color)
            dot(bag, WHITE, 0.025f)
        }
        Prop.MICROPHONE -> {
            line(grip - a * 0.04f, grip + a * 0.08f, DARK, 0.028f)
            line(grip + a * 0.06f, grip + a * 0.075f, color, 0.03f)
            dot(grip + a * 0.11f, Color(0xFF757575), 0.03f)
        }
        Prop.ESPRESSO_MACHINE -> {
            val m = grip + up * 0.04f + fwd * 0.03f
            box(m, up, 0.075f, 0.09f, SILVER)
            box(m + up * 0.06f, up, 0.075f, 0.03f, color)
            line(m + fwd * 0.02f, m + fwd * 0.02f - up * 0.03f, DARK, 0.014f)
            box(m - up * 0.06f + fwd * 0.02f, up, 0.02f, 0.02f, WHITE)
        }
        Prop.PRIZE_BOWL -> {
            val rim = grip + up * 0.08f + fwd * 0.03f
            box(rim - up * 0.01f + fwd * 0.03f, up, 0.012f, 0.03f, WHITE)
            box(rim - up * 0.015f - fwd * 0.02f, up, 0.012f, 0.03f, color)
            polygon(Color(0x99B3E5FC), (0..12).map { i ->
                val t = PI * i / 12
                rim + fwd * (0.1f * cos(t).toFloat()) - up * (0.09f * sin(t).toFloat())
            })
        }
        Prop.PODIUM -> {
            val m = grip + fwd * 0.04f
            polygon(
                Color(0xFF6D4C41),
                m + up * 0.12f - fwd * 0.07f, m + up * 0.12f + fwd * 0.07f,
                m - up * 0.12f + fwd * 0.05f, m - up * 0.12f - fwd * 0.05f,
            )
            box(m + up * 0.13f, up, 0.08f, 0.015f, Color(0xFF4E342E))
            dot(m + up * 0.03f, color, 0.035f)
        }
        Prop.DONUTS -> {
            val b = grip + up * 0.03f
            for (i in -1..1) {
                val d = b + up * 0.045f + fwd * (0.06f * i)
                dot(d, Color(0xFF8D6E63), 0.024f)
                dot(d, Color(0xFFF48FB1), 0.009f)
            }
            box(b, up, 0.1f, 0.035f, if (color == GOLD) Color(0xFFF8BBD0) else color)
        }

        Prop.BADGE_HOLDER -> box(badgeCentre(sk, up, fwd), up, 0.045f, 0.062f, Color(0x66E1F5FE))
        Prop.VIP_PASS -> {
            val pass = sk.neck - up * 0.2f + fwd * 0.04f
            line(sk.neck - fwd * 0.04f, pass + up * 0.05f, DARK, 0.016f)
            box(pass, up, 0.036f, 0.05f, color)
            box(pass + up * 0.012f, up, 0.036f, 0.012f, DARK)
        }
        Prop.T_SHIRT, Prop.HOODIE -> dot(sk.neck - up * 0.22f + fwd * 0.02f, WHITE, 0.025f)
        Prop.TOTE_BAG -> {
            val bag = sk.hip - fwd * 0.11f - up * 0.02f
            line(sk.neck + fwd * 0.04f, bag + up * 0.07f, Color(0xFFC8B996), 0.014f)
            box(bag, up, 0.06f, 0.07f, Color(0xFFE8DCC0))
            dot(bag, color, 0.025f)
        }
        Prop.GILET -> {
            line(sk.hip + up * 0.06f, sk.neck - up * 0.02f, color, TORSO_WIDTH * 1.05f)
            line(sk.neck - up * 0.02f + fwd * 0.08f, sk.hip + up * 0.06f + fwd * 0.08f, color.shade(0.7f), 0.01f)
        }
        Prop.HEADPHONES -> {
            val ear = sk.head - fwd * 0.015f
            line(ear, sk.head + up * 0.095f, DARK, 0.018f)
            dot(ear, DARK, 0.042f)
            dot(ear, color, 0.028f)
        }

        Prop.LAPTOP -> {
            box(shield, up, 0.12f, 0.085f, Color(0xFF90A4AE))
            dot(shield + up * 0.03f - fwd * 0.05f, color, 0.022f)
            dot(shield - up * 0.03f + fwd * 0.04f, Color(0xFFFDD835), 0.018f)
            dot(shield + up * 0.04f + fwd * 0.06f, Color(0xFF43A047), 0.016f)
            box(shield - up * 0.04f - fwd * 0.03f, up, 0.025f, 0.015f, Color(0xFFE53935))
        }
        Prop.BROCHURE_STAND -> {
            box(shield, up, 0.07f, 0.17f, Color(0xFF424242))
            for ((i, c) in listOf(color, WHITE, color).withIndex()) {
                box(shield + up * (0.1f - 0.1f * i), up, 0.055f, 0.04f, c)
            }
        }
        Prop.UMBRELLA -> {
            val k = grip + fwd * 0.1f
            line(grip - fwd * 0.02f, k + fwd * 0.1f, DARK, 0.01f)
            polygon(color, (0..12).map { i ->
                val t = PI * i / 12 - PI / 2
                k + fwd * (0.09f * cos(t).toFloat()) + up * (0.22f * sin(t).toFloat())
            })
            line(k + up * 0.22f, k - up * 0.22f, color.shade(0.7f), 0.012f)
        }
        Prop.PULL_UP_BANNER -> {
            box(shield + up * 0.08f, up, 0.08f, 0.24f, color)
            box(shield + up * 0.22f, up, 0.08f, 0.03f, WHITE)
            box(shield - up * 0.16f, up, 0.09f, 0.015f, Color(0xFF9E9E9E))
        }
        Prop.TABLE -> {
            box(shield, up, 0.16f, 0.1f, color)
            line(shield + up * 0.1f - fwd * 0.16f, shield + up * 0.1f + fwd * 0.16f, WHITE, 0.012f)
            dot(shield, WHITE, 0.035f)
        }
        Prop.BACKDROP -> {
            box(shield + up * 0.05f, up, 0.17f, 0.22f, color)
            ring(shield + up * 0.05f, WHITE, 0.06f, 0.015f)
        }
        Prop.POWER_BANK -> {
            box(grip + a * 0.04f, a, 0.03f, 0.055f, DARK)
            for (i in 0..2) box(grip + a * (0.015f + 0.025f * i), a, 0.018f, 0.008f, Color(0xFF76FF03))
        }
    }
}
