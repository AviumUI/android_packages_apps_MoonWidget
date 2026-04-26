/*
 *
 * Copyright (C) 2025 The AviumUI Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 */

package org.exthm.moonwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit

class CountdownWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { updateAppWidget(context, appWidgetManager, it) }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        appWidgetIds.forEach { WidgetPrefs.clear(context, it) }
    }

    companion object {
        private const val PREF_TITLE = "countdown_title"
        private const val PREF_TARGET_EPOCH_DAY = "countdown_target_epoch_day"

        fun savePrefs(context: Context, appWidgetId: Int, title: String, targetDate: LocalDate) {
            WidgetPrefs.forWidget(context, appWidgetId)
                .edit()
                .putString(PREF_TITLE, title.ifBlank { context.getString(R.string.countdown_default_title) })
                .putLong(PREF_TARGET_EPOCH_DAY, targetDate.toEpochDay())
                .apply()
        }

        fun loadTargetDate(context: Context, appWidgetId: Int): LocalDate {
            val defaultDate = LocalDate.now().plusDays(7)
            val epochDay = WidgetPrefs.forWidget(context, appWidgetId)
                .getLong(PREF_TARGET_EPOCH_DAY, defaultDate.toEpochDay())
            return LocalDate.ofEpochDay(epochDay)
        }

        fun loadTitle(context: Context, appWidgetId: Int): String {
            return WidgetPrefs.forWidget(context, appWidgetId)
                .getString(PREF_TITLE, context.getString(R.string.countdown_default_title))
                ?: context.getString(R.string.countdown_default_title)
        }

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val targetDate = loadTargetDate(context, appWidgetId)
            val title = loadTitle(context, appWidgetId)
            val days = ChronoUnit.DAYS.between(LocalDate.now(), targetDate).toInt()
            val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
            val daysText = when {
                days > 0 -> context.getString(R.string.countdown_days_left, days)
                days < 0 -> context.getString(R.string.countdown_days_since, -days)
                else -> context.getString(R.string.countdown_today)
            }

            val views = RemoteViews(context.packageName, R.layout.widget_countdown)
            views.setTextViewText(R.id.text_countdown_title, title)
            views.setTextViewText(R.id.text_countdown_days, daysText)
            views.setTextViewText(
                R.id.text_countdown_date,
                context.getString(R.string.countdown_target_date, targetDate.format(formatter))
            )
            views.setOnClickPendingIntent(R.id.widget_root, configIntent(context, appWidgetId))
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun configIntent(context: Context, appWidgetId: Int): PendingIntent {
            val intent = Intent(context, CountdownConfigActivity::class.java)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            return PendingIntent.getActivity(
                context,
                appWidgetId + 5000,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}
