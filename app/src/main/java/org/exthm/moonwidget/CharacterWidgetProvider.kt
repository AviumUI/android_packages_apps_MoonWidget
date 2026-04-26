/*
 *
 * Copyright (C) 2025 The AviumUI Project
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import java.util.Calendar

class CharacterWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val prefs = context.getSharedPreferences("WidgetPrefs_$appWidgetId", Context.MODE_PRIVATE)
            val hairColor = prefs.getInt("hair_color", Color.parseColor("#8D6E63"))
            val bowColor = prefs.getInt("bow_color", Color.parseColor("#E91E63"))

            val views = RemoteViews(context.packageName, R.layout.widget_character)
            views.setTextViewText(R.id.text_character_greeting, greeting(context))

            val options = appWidgetManager.getAppWidgetOptions(appWidgetId)

            val widgetWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
            val widgetHeightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)

            val displayMetrics = context.resources.displayMetrics
            val targetWidthPx = (widgetWidthDp * displayMetrics.density).toInt()
            val targetHeightPx = (widgetHeightDp * displayMetrics.density).toInt()

            if (targetWidthPx > 0 && targetHeightPx > 0) {

                val hairBitmap = getBitmapFromVectorDrawable(context, R.drawable.toufa, hairColor, targetWidthPx, targetHeightPx)
                views.setImageViewBitmap(R.id.image_toufa, hairBitmap)

                val bowBitmap = getBitmapFromVectorDrawable(context, R.drawable.hudiejie, bowColor, targetWidthPx, targetHeightPx)
                views.setImageViewBitmap(R.id.image_hudiejie, bowBitmap)
            } else {
                val hairBitmap = getBitmapFromVectorDrawable(context, R.drawable.toufa, hairColor, 150, 150)
                views.setImageViewBitmap(R.id.image_toufa, hairBitmap)
                val bowBitmap = getBitmapFromVectorDrawable(context, R.drawable.hudiejie, bowColor, 150, 150)
                views.setImageViewBitmap(R.id.image_hudiejie, bowBitmap)
            }


            val intent = Intent(context, CharacterConfigActivity::class.java)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)

            val pendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun greeting(context: Context): String {
            return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
                in 5..11 -> context.getString(R.string.character_morning)
                in 12..17 -> context.getString(R.string.character_afternoon)
                in 18..22 -> context.getString(R.string.character_evening)
                else -> context.getString(R.string.character_night)
            }
        }

        private fun getBitmapFromVectorDrawable(context: Context, drawableId: Int, color: Int, targetWidth: Int, targetHeight: Int): Bitmap? {
            val drawable = ContextCompat.getDrawable(context, drawableId) ?: return null

            val wrappedDrawable = DrawableCompat.wrap(drawable).mutate()
            wrappedDrawable.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)

            val intrinsicWidth = wrappedDrawable.intrinsicWidth
            val intrinsicHeight = wrappedDrawable.intrinsicHeight
            if (intrinsicWidth <= 0 || intrinsicHeight <= 0) {
                return null
            }
            val aspectRatio = intrinsicWidth.toFloat() / intrinsicHeight.toFloat()

            val finalWidth: Int
            val finalHeight: Int

            if (intrinsicWidth > intrinsicHeight) {
                finalWidth = targetWidth
                finalHeight = (finalWidth / aspectRatio).toInt()
            } else {
                finalHeight = targetHeight
                finalWidth = (finalHeight * aspectRatio).toInt()
            }

            val bitmap = Bitmap.createBitmap(finalWidth, finalHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            wrappedDrawable.setBounds(0, 0, canvas.width, canvas.height)
            wrappedDrawable.draw(canvas)
            return bitmap
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        appWidgetIds.forEach { WidgetPrefs.clear(context, it) }
    }
}
