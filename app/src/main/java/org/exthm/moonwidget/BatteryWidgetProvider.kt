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
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.os.BatteryManager
import android.os.Bundle
import android.provider.Settings
import android.widget.RemoteViews

class BatteryWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { updateAppWidget(context, appWidgetManager, it) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            Intent.ACTION_POWER_CONNECTED,
            Intent.ACTION_POWER_DISCONNECTED,
            Intent.ACTION_BATTERY_LOW,
            Intent.ACTION_BATTERY_OKAY,
            Intent.ACTION_WALLPAPER_CHANGED,
            Intent.ACTION_CONFIGURATION_CHANGED,
            ACTION_OVERLAY_CHANGED -> {
                val manager = AppWidgetManager.getInstance(context)
                val ids = manager.getAppWidgetIds(ComponentName(context, javaClass))
                ids.forEach { updateAppWidget(context, manager, it) }
            }
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        updateAppWidget(context, appWidgetManager, appWidgetId)
    }

    companion object {
        private const val ACTION_OVERLAY_CHANGED = "android.intent.action.OVERLAY_CHANGED"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val info = readBatteryInfo(context)
            val views = RemoteViews(context.packageName, R.layout.widget_battery)
            views.setImageViewBitmap(
                R.id.image_battery,
                createBatteryBitmap(context, info, appWidgetManager.getAppWidgetOptions(appWidgetId))
            )
            views.setOnClickPendingIntent(R.id.widget_root, batterySettingsIntent(context, appWidgetId))
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun readBatteryInfo(context: Context): BatteryInfo {
            val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
            val percent = if (level >= 0 && scale > 0) level * 100 / scale else 0
            return BatteryInfo(level = percent.coerceIn(0, 100))
        }

        private fun createBatteryBitmap(context: Context, info: BatteryInfo, options: Bundle): Bitmap {
            val density = context.resources.displayMetrics.density
            val widthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 110).coerceAtLeast(110)
            val heightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 110).coerceAtLeast(110)
            val width = (widthDp * density).toInt().coerceIn(240, 900)
            val height = (heightDp * density).toInt().coerceIn(240, 900)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val lightColor = resolveColor(context, R.color.widget_surface, 0xFFEADDFF.toInt())
            val deepColor = blend(lightColor, Color.BLACK, 0.58f)
            val iconColor = bestContentColor(lightColor)
            val shortSide = minOf(width, height).toFloat()
            val inset = shortSide * 0.095f

            val card = RectF(0f, 0f, width.toFloat(), height.toFloat())
            val cardRadius = shortSide * 0.17f
            val cardPath = Path().apply {
                addRoundRect(card, cardRadius, cardRadius, Path.Direction.CW)
            }
            val deepPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = deepColor
                style = Paint.Style.FILL
            }
            canvas.drawPath(cardPath, deepPaint)

            canvas.save()
            canvas.clipPath(cardPath)
            val lightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = lightColor
                style = Paint.Style.FILL
            }
            val fillRight = card.left + card.width() * (info.level / 100f)
            canvas.drawRect(card.left, card.top, fillRight, card.bottom, lightPaint)
            canvas.restore()

            val boltPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = iconColor
                style = Paint.Style.FILL
            }
            drawBolt(canvas, inset, inset * 0.94f, shortSide * 0.15f, boltPaint)

            val levelText = "${info.level}%"
            val levelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = iconColor
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                textSize = shortSide * 0.16f
            }
            canvas.drawText(levelText, inset, height - inset, levelPaint)
            return bitmap
        }

        private fun drawBolt(canvas: Canvas, x: Float, y: Float, size: Float, paint: Paint) {
            val path = Path().apply {
                moveTo(x + size * 0.42f, y)
                lineTo(x + size * 0.14f, y + size * 0.48f)
                lineTo(x + size * 0.42f, y + size * 0.48f)
                lineTo(x + size * 0.26f, y + size)
                lineTo(x + size * 0.86f, y + size * 0.36f)
                lineTo(x + size * 0.56f, y + size * 0.36f)
                lineTo(x + size * 0.72f, y)
                close()
            }
            canvas.drawPath(path, paint)
        }

        private fun resolveColor(context: Context, resId: Int, fallback: Int): Int {
            return runCatching { context.getColor(resId) }.getOrDefault(fallback)
        }

        private fun blend(foreground: Int, background: Int, ratio: Float): Int {
            val inverse = 1f - ratio
            return Color.argb(
                255,
                (Color.red(foreground) * inverse + Color.red(background) * ratio).toInt(),
                (Color.green(foreground) * inverse + Color.green(background) * ratio).toInt(),
                (Color.blue(foreground) * inverse + Color.blue(background) * ratio).toInt()
            )
        }

        private fun bestContentColor(color: Int): Int {
            val luminance = (0.299f * Color.red(color) + 0.587f * Color.green(color) + 0.114f * Color.blue(color)) / 255f
            return if (luminance > 0.55f) Color.argb(230, 0, 0, 0) else Color.WHITE
        }

        private fun batterySettingsIntent(context: Context, appWidgetId: Int): PendingIntent {
            val intent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val fallback = Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val resolvedIntent = if (intent.resolveActivity(context.packageManager) != null) intent else fallback
            return PendingIntent.getActivity(
                context,
                appWidgetId + 6000,
                resolvedIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}

private data class BatteryInfo(
    val level: Int
)
