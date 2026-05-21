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
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import java.time.DateTimeException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class CountdownConfigActivity : AppCompatActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val initialTitle = CountdownWidgetProvider.loadTitle(this, appWidgetId)
        val initialDate = CountdownWidgetProvider.loadTargetDate(this, appWidgetId)
        setContent {
            MaterialTheme {
                CountdownConfigScreen(initialTitle, initialDate) { title, targetDate ->
                    CountdownWidgetProvider.savePrefs(this, appWidgetId, title, targetDate)
                    CountdownWidgetProvider.updateAppWidget(
                        this,
                        AppWidgetManager.getInstance(this),
                        appWidgetId
                    )
                    finishWidgetConfiguration(appWidgetId)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountdownConfigScreen(
    initialTitle: String,
    initialDate: LocalDate,
    onSave: (String, LocalDate) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var year by remember { mutableStateOf(initialDate.year.toString()) }
    var month by remember { mutableStateOf(initialDate.monthValue.toString()) }
    var day by remember { mutableStateOf(initialDate.dayOfMonth.toString()) }
    val parsedDate = remember(year, month, day) { parseDateOrNull(year, month, day) }
    val dateFormatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.countdown_config_title)) }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.countdown_title_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                NumberField(
                    value = year,
                    onValueChange = { year = it },
                    label = stringResource(R.string.countdown_year),
                    modifier = Modifier.weight(1.3f)
                )
                NumberField(
                    value = month,
                    onValueChange = { month = it },
                    label = stringResource(R.string.countdown_month),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                )
                NumberField(
                    value = day,
                    onValueChange = { day = it },
                    label = stringResource(R.string.countdown_day),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                )
            }
            if (parsedDate == null) {
                Text(
                    text = stringResource(R.string.countdown_invalid_date),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else {
                Text(
                    text = stringResource(
                        R.string.countdown_target_date,
                        parsedDate.format(dateFormatter)
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            Spacer(Modifier.height(24.dp))
            Button(
                enabled = parsedDate != null,
                onClick = { parsedDate?.let { onSave(title, it) } },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}

@Composable
private fun NumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.filter(Char::isDigit).take(4)) },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier
    )
}

private fun parseDateOrNull(year: String, month: String, day: String): LocalDate? {
    return try {
        LocalDate.of(year.toInt(), month.toInt(), day.toInt())
    } catch (_: NumberFormatException) {
        null
    } catch (_: DateTimeException) {
        null
    }
}
