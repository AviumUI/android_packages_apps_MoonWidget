package org.exthm.moonwidget

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.TypedValue
import android.widget.RemoteViews

class TextClockWidgetProvider : AppWidgetProvider() {

    companion object {
        private const val ACTION_TICK = "org.exthm.moonwidget.ACTION_TICK"

        private fun nextMinuteTrigger(): Long {
            val now = System.currentTimeMillis()
            return (now / 60000 + 1) * 60000
        }

        private fun getUpdateIntent(context: Context): PendingIntent {
            val intent = Intent(context, TextClockWidgetProvider::class.java)
                .setAction(ACTION_TICK)
            return PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        @SuppressLint("ScheduleExactAlarm")
        private fun scheduleNextTick(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextMinuteTrigger(),
                getUpdateIntent(context)
            )
        }

        internal fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val time = TimeUtil.getFormattedTime(context)
            val views = RemoteViews(context.packageName, R.layout.widget_text_clock)

            val accentColor = getAccentColor(context)
            views.setTextColor(R.id.text_intro, accentColor)
            views.setTextViewText(R.id.text_intro, time.intro)
            views.setTextViewText(R.id.text_hour, time.hour)
            views.setTextViewText(R.id.text_minute, time.minute)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun getAccentColor(context: Context): Int {
            val tv = TypedValue()
            context.theme.resolveAttribute(android.R.attr.colorAccent, tv, true)
            return tv.data
        }
    }

    override fun onEnabled(context: Context) {
        scheduleNextTick(context)
    }

    override fun onDisabled(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(getUpdateIntent(context))
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (id in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, id)
        }
        scheduleNextTick(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_TICK) {
            val mgr = AppWidgetManager.getInstance(context)
            val cn = android.content.ComponentName(context, javaClass)
            val ids = mgr.getAppWidgetIds(cn)
            onUpdate(context, mgr, ids)
            scheduleNextTick(context)
        }
    }
}
