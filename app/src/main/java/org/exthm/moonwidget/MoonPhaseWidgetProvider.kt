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
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import kotlin.math.cos

class MoonPhaseWidgetProvider : AppWidgetProvider() {
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
        private const val PREF_SHOW_DETAILS = "moon_show_details"

        fun savePrefs(context: Context, appWidgetId: Int, showDetails: Boolean) {
            WidgetPrefs.forWidget(context, appWidgetId)
                .edit()
                .putBoolean(PREF_SHOW_DETAILS, showDetails)
                .apply()
        }

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val info = MoonPhaseUtil.current(context)
            val showDetails = WidgetPrefs.forWidget(context, appWidgetId)
                .getBoolean(PREF_SHOW_DETAILS, true)
            val views = RemoteViews(context.packageName, R.layout.widget_moon_phase)
            views.setImageViewBitmap(R.id.image_moon, createMoonBitmap(context, info.age, 180))
            views.setTextViewText(R.id.text_moon_phase, info.phaseName)
            views.setTextViewText(
                R.id.text_moon_illumination,
                context.getString(R.string.moon_phase_illumination, info.illumination)
            )
            views.setTextViewText(
                R.id.text_moon_next,
                context.getString(R.string.moon_phase_next, info.nextPhaseName, info.daysToNextPhase)
            )
            views.setViewVisibility(R.id.text_moon_illumination, if (showDetails) android.view.View.VISIBLE else android.view.View.GONE)
            views.setViewVisibility(R.id.text_moon_next, if (showDetails) android.view.View.VISIBLE else android.view.View.GONE)
            views.setOnClickPendingIntent(R.id.widget_root, configIntent(context, appWidgetId))
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun configIntent(context: Context, appWidgetId: Int): PendingIntent {
            val intent = Intent(context, MoonPhaseConfigActivity::class.java)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            return PendingIntent.getActivity(
                context,
                appWidgetId + 3000,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        private fun createMoonBitmap(context: Context, age: Double, size: Int): Bitmap {
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val moonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = ContextCompat.getColor(context, R.color.widget_moon_light)
            }
            val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = ContextCompat.getColor(context, R.color.widget_moon_dark)
            }
            val rect = RectF(8f, 8f, size - 8f, size - 8f)
            canvas.drawOval(rect, moonPaint)

            val phase = age / 29.53058867
            val widthScale = cos(2.0 * Math.PI * phase).toFloat()
            val shadowWidth = (rect.width() * kotlin.math.abs(widthScale)).coerceAtLeast(2f)
            val shadowRect = RectF(
                rect.centerX() - shadowWidth / 2f,
                rect.top,
                rect.centerX() + shadowWidth / 2f,
                rect.bottom
            )
            if (phase < 0.5) {
                canvas.drawArc(rect, 90f, 180f, false, shadowPaint)
            } else {
                canvas.drawArc(rect, 270f, 180f, false, shadowPaint)
            }
            val overlayPaint = if (widthScale >= 0f) shadowPaint else moonPaint
            canvas.drawOval(shadowRect, overlayPaint)
            return bitmap
        }
    }
}
