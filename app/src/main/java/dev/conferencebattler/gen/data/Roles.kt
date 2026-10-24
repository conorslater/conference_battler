package dev.conferencebattler.gen.data

/**
 * A conference job title. Everyone is the same class; the role only shifts the stat ranges.
 * Ranges are in hundreds. HP keeps the original Barcode Battler ceiling of 199.
 *
 * @param weight how often the role comes up, out of the total of all weights.
 */
data class Role(val name: String, val weight: Int, val hp: IntRange, val st: IntRange, val df: IntRange)

// Same rule as the affix tables: append rows, don't reorder or edit existing ones.
val Roles = listOf(
    Role("Developer", 20, hp = 50..150, st = 20..45, df = 10..25),
    Role("Marketeer", 15, hp = 70..180, st = 15..35, df = 10..30),
    Role("Sales Engineer", 15, hp = 60..170, st = 18..40, df = 12..30),
    Role("Booth Staffer", 12, hp = 80..199, st = 12..30, df = 15..35),
    Role("Product Manager", 10, hp = 60..160, st = 15..38, df = 12..30),
    Role("Recruiter", 8, hp = 60..160, st = 15..40, df = 10..28),
    Role("Developer Advocate", 8, hp = 60..170, st = 18..42, df = 10..28),
    Role("Intern", 7, hp = 40..120, st = 12..35, df = 5..20),
    Role("CTO", 4, hp = 80..199, st = 25..45, df = 15..35),
    Role("Keynote Speaker", 1, hp = 100..199, st = 30..50, df = 20..35),
)
