package org.exthm.moonwidget

import android.content.Context
import java.util.Calendar
import java.util.Locale

data class FormattedTime(val intro: String, val hour: String, val minute: String)

object TimeUtil {

    private val chineseNum = arrayOf("〇", "一", "二", "三", "四", "五", "六", "七", "八", "九", "十")
    private val englishNum = arrayOf(
        "twelve", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten",
        "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen"
    )
    private val englishTens = arrayOf("", "", "twenty", "thirty", "forty", "fifty")

    fun getFormattedTime(context: Context): FormattedTime {
        val locale = context.resources.configuration.locales[0]
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        return if (locale.language == Locale.CHINESE.language) {
            getChineseFormattedTime(hour, minute)
        } else {
            getEnglishFormattedTime(hour, minute)
        }
    }

    private fun getChineseFormattedTime(hour: Int, minute: Int): FormattedTime {
        val intro = "现在是"
        val hourText = toChineseHour(hour) + "点"
        val minuteText = toChineseMinute(minute) + "分"
        return FormattedTime(intro, hourText, minuteText)
    }

    private fun getEnglishFormattedTime(hour: Int, minute: Int): FormattedTime {
        val intro = "It's"
        val hour12 = if (hour % 12 == 0) 12 else hour % 12
        val hourText = toEnglishNumber(hour12)
        val minuteText = toEnglishNumber(minute)
        return FormattedTime(intro, hourText, minuteText)
    }

    private fun toChineseHour(hour: Int): String {
        return if (hour == 2) "两" else numberToChinese(hour)
    }

    private fun toChineseMinute(minute: Int): String {
        return when {
            minute == 0 -> "整"
            minute < 10 -> chineseNum[0] + chineseNum[minute]
            minute == 10 -> "十"
            minute < 20 -> "十" + chineseNum[minute % 10]
            minute % 10 == 0 -> chineseNum[minute / 10] + "十"
            else -> chineseNum[minute / 10] + "十" + chineseNum[minute % 10]
        }
    }

    private fun numberToChinese(num: Int): String {
        return if (num <= 10) chineseNum[num] else toChineseMinute(num)
    }

    private fun toEnglishNumber(num: Int): String {
        return when {
            num == 0 -> "o'clock"
            num < 20 -> englishNum[num]
            else -> {
                val ten = num / 10
                val one = num % 10
                if (one == 0) {
                    englishTens[ten]
                } else {
                    "${englishTens[ten]}-${englishNum[one]}"
                }
            }
        }
    }
}