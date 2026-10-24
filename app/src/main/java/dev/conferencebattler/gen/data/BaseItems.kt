package dev.conferencebattler.gen.data

/** An item base. Its bonus is rolled from [minBonus]..[maxBonus] (multiples of 100). */
data class ItemBase(val name: String, val minLevel: Int, val minBonus: Int, val maxBonus: Int)

// Things you pick up at a conference. Level requirements and attack ranges follow the old Diablo
// weapon table. Defence ranges are lower: damage never drops below 100, so a big DF bonus would
// make a player almost impossible to hurt. Same rule as the affix tables: append rows, don't
// reorder or edit existing ones.

val AttackBases = listOf(
    ItemBase("Free Pen", 1, 300, 600),
    ItemBase("Business Card", 1, 300, 700),
    ItemBase("Rolled-up Flyer", 1, 300, 800),
    ItemBase("Stress Ball", 2, 500, 900),
    ItemBase("Branded Lanyard", 2, 500, 1000),
    ItemBase("Hot Coffee", 2, 500, 1100),
    ItemBase("USB Stick", 4, 600, 1000),
    ItemBase("Energy Drink", 6, 800, 1300),
    ItemBase("Laser Pointer", 8, 900, 1500),
    ItemBase("Presentation Clicker", 10, 1000, 1600),
    ItemBase("Selfie Stick", 10, 1100, 1800),
    ItemBase("Branded Water Bottle", 12, 1200, 1900),
    ItemBase("Swag Bag", 14, 1300, 2100),
    ItemBase("Wireless Microphone", 15, 1300, 2000),
    ItemBase("Espresso Machine", 17, 1700, 2700),
    ItemBase("Prize Draw Bowl", 19, 1600, 2500),
    ItemBase("Keynote Podium", 21, 1500, 2600),
)

val DefenceBases = listOf(
    ItemBase("Badge Holder", 1, 300, 600),
    ItemBase("Promotional T-shirt", 1, 300, 700),
    ItemBase("Tote Bag", 1, 400, 600),
    ItemBase("Sticker-covered Laptop", 3, 500, 800),
    ItemBase("Branded Hoodie", 3, 500, 800),
    ItemBase("Brochure Stand", 8, 700, 1100),
    ItemBase("Branded Umbrella", 9, 800, 1100),
    ItemBase("Noise-cancelling Headphones", 10, 700, 1000),
    ItemBase("Pull-up Banner", 13, 1000, 1400),
    ItemBase("Exhibitor Table", 14, 1000, 1400),
    ItemBase("Speaker Lounge Pass", 16, 1100, 1600),
    ItemBase("Booth Backdrop", 19, 1400, 1900),
)
