/*
 *
 * Copyright (C) 2025 The AviumUI Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 */

package org.exthm.moonwidget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent

object WidgetPrefs {
    fun forWidget(context: Context, appWidgetId: Int) =
        context.getSharedPreferences("WidgetPrefs_$appWidgetId", Context.MODE_PRIVATE)

    fun clear(context: Context, appWidgetId: Int) {
        forWidget(context, appWidgetId).edit().clear().apply()
    }
}

fun Activity.finishWidgetConfiguration(appWidgetId: Int) {
    val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
    setResult(Activity.RESULT_OK, resultValue)
    finish()
}
