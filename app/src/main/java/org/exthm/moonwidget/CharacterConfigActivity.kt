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

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat

class CharacterConfigActivity : AppCompatActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    // 颜色
    private val hairColors = listOf(
        Color.parseColor("#8D6E63"), // 棕
        Color.parseColor("#212121"), // 黑
        Color.parseColor("#FFEB3B"), // 黄
        Color.parseColor("#00BCD4"), // 青
        Color.parseColor("#F44336")  // 红
    )
    private val bowColors = listOf(
        Color.parseColor("#E91E63"), // 粉
        Color.parseColor("#3F51B5"), // 蓝
        Color.parseColor("#4CAF50"), // 绿
        Color.parseColor("#9C27B0"), // 紫
        Color.parseColor("#FF9800")  // 橙
    )

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

        setContent {
            MaterialTheme {
                WidgetConfigScreen(
                    hairColorOptions = hairColors,
                    bowColorOptions = bowColors,
                    onSaveClick = { hairColor, bowColor ->
                        saveColorPrefs(this, appWidgetId, hairColor, bowColor)

                        val appWidgetManager = AppWidgetManager.getInstance(this)
                        CharacterWidgetProvider.updateAppWidget(this, appWidgetManager, appWidgetId)

                        finishWidgetConfiguration(appWidgetId)
                    }
                )
            }
        }
    }

    private fun saveColorPrefs(context: Context, appWidgetId: Int, hair: Int, bow: Int) {
        val prefs = context.getSharedPreferences("WidgetPrefs_$appWidgetId", Context.MODE_PRIVATE).edit()
        prefs.putInt("hair_color", hair)
        prefs.putInt("bow_color", bow)
        prefs.apply()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetConfigScreen(
    hairColorOptions: List<Int>,
    bowColorOptions: List<Int>,
    onSaveClick: (hairColor: Int, bowColor: Int) -> Unit
) {
    var hairColor by remember { mutableStateOf(hairColorOptions.first()) }
    var bowColor by remember { mutableStateOf(bowColorOptions.first()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.config_title),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            WidgetPreview(
                hairColor = hairColor,
                bowColor = bowColor,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .aspectRatio(1f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.config_hint),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            ColorSettingItem(
                title = stringResource(R.string.toufa),
                colorOptions = hairColorOptions,
                selectedColor = hairColor,
                onColorSelected = { hairColor = it }
            )

            Spacer(modifier = Modifier.height(8.dp))

            ColorSettingItem(
                title = stringResource(R.string.hudiejie),
                colorOptions = bowColorOptions,
                selectedColor = bowColor,
                onColorSelected = { bowColor = it }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onSaveClick(hairColor, bowColor) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = stringResource(R.string.save),
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun WidgetPreview(
    hairColor: Int,
    bowColor: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val hairBitmap by remember(hairColor) {
        mutableStateOf(createColoredBitmap(context, R.drawable.toufa, hairColor))
    }
    val bowBitmap by remember(bowColor) {
        mutableStateOf(createColoredBitmap(context, R.drawable.hudiejie, bowColor))
    }

    Box(
        modifier = modifier.clip(RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {

        bowBitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = stringResource(R.string.character_preview_bow),
                modifier = Modifier.fillMaxSize()
            )
        }
        hairBitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = stringResource(R.string.character_preview_hair),
                modifier = Modifier.fillMaxSize()
            )
        }

    }
}


@Composable
fun ColorSettingItem(
    title: String,
    colorOptions: List<Int>,
    selectedColor: Int,
    onColorSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            colorOptions.forEach { color ->
                val isSelected = color == selectedColor
                Box(
                    modifier = Modifier
                        .size(if (isSelected) 40.dp else 32.dp)
                        .clip(CircleShape)
                        .background(color = androidx.compose.ui.graphics.Color(color))
                        .clickable { onColorSelected(color) }
                        .then(
                            if (isSelected) {
                                Modifier.border(
                                    2.dp,
                                    MaterialTheme.colorScheme.primary,
                                    CircleShape
                                )
                            } else Modifier
                        )
                )
            }
        }
    }
}

fun createColoredBitmap(context: Context, drawableId: Int, color: Int): Bitmap? {
    val drawable = ContextCompat.getDrawable(context, drawableId) ?: return null

    val width = (drawable.intrinsicWidth).coerceAtMost(512)
    val height = (drawable.intrinsicHeight).coerceAtMost(512)
    if (width <= 0 || height <= 0) return null

    val wrappedDrawable = DrawableCompat.wrap(drawable).mutate()
    wrappedDrawable.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    wrappedDrawable.setBounds(0, 0, canvas.width, canvas.height)
    wrappedDrawable.draw(canvas)
    return bitmap
}
