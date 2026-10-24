package dev.conferencebattler.rng

/**
 * Port of DevilutionX's `SplitMix32` (Source/engine/random.hpp), which is the MurmurHash3 fmix32
 * finalizer stepped by the golden ratio.
 *
 * Used to spread the bits of a hash prefix before it becomes a [DiabloRandom] seed.
 */
class SplitMix32(private var state: UInt) {
    fun next(): UInt {
        state += 0x9e3779b9u
        var z = state
        z = (z xor (z shr 16)) * 0x85ebca6bu
        z = (z xor (z shr 13)) * 0xc2b2ae35u
        return z xor (z shr 16)
    }
}
