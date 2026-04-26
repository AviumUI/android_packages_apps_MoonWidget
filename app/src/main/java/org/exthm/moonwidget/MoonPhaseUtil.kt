/*
 *
 * Copyright (C) 2025 The AviumUI Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 */

package org.exthm.moonwidget

import android.content.Context
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt

data class MoonPhaseInfo(
    val phaseIndex: Int,
    val phaseName: String,
    val age: Double,
    val illumination: Int,
    val nextPhaseName: String,
    val daysToNextPhase: Int
)

object MoonPhaseUtil {
    private const val SYNODIC_MONTH = 29.53058867
    private val KNOWN_NEW_MOON: LocalDate = LocalDate.of(2000, 1, 6)

    fun current(context: Context, date: LocalDate = LocalDate.now()): MoonPhaseInfo {
        val days = ChronoUnit.DAYS.between(KNOWN_NEW_MOON, date).toDouble()
        val age = ((days % SYNODIC_MONTH) + SYNODIC_MONTH) % SYNODIC_MONTH
        val fraction = age / SYNODIC_MONTH
        val phaseIndex = ((fraction * 8.0) + 0.5).toInt() and 7
        val illumination = ((1.0 - cos(2.0 * PI * fraction)) / 2.0 * 100.0).roundToInt()
        val nextBoundary = ((phaseIndex + 1) * SYNODIC_MONTH / 8.0)
        val daysToNext = if (age <= nextBoundary) {
            (nextBoundary - age).roundToInt().coerceAtLeast(1)
        } else {
            (SYNODIC_MONTH - age + nextBoundary).roundToInt().coerceAtLeast(1)
        }
        val nextIndex = (phaseIndex + 1) and 7
        return MoonPhaseInfo(
            phaseIndex = phaseIndex,
            phaseName = phaseName(context, phaseIndex),
            age = age,
            illumination = illumination.coerceIn(0, 100),
            nextPhaseName = phaseName(context, nextIndex),
            daysToNextPhase = daysToNext
        )
    }

    fun phaseName(context: Context, index: Int): String {
        val names = intArrayOf(
            R.string.moon_phase_new,
            R.string.moon_phase_waxing_crescent,
            R.string.moon_phase_first_quarter,
            R.string.moon_phase_waxing_gibbous,
            R.string.moon_phase_full,
            R.string.moon_phase_waning_gibbous,
            R.string.moon_phase_last_quarter,
            R.string.moon_phase_waning_crescent
        )
        return context.getString(names[index.coerceIn(0, names.lastIndex)])
    }
}
