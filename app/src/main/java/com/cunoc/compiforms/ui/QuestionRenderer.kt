package com.cunoc.compiforms.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cunoc.compiforms.data.remote.ApiService
import com.cunoc.compiforms.form.model.questions.*
import com.cunoc.compiforms.form.model.styles.StyleSet

@Composable
fun QuestionRenderer(
    element: QuestionElement,
    parentStyle: StyleSet? = null
) {
    val effectiveStyle = element.styles?.mergeWith(parentStyle) ?: parentStyle
    val textColor = effectiveStyle?.textColor?.toComposeColor() ?: LocalContentColor.current
    val fontSize = (effectiveStyle?.textSize?.toFloat() ?: 16f).sp
    val fontFamily = effectiveStyle?.fontFamily?.toComposeFontFamily()

    val layout = element.layout
    val widthValue = layout.width
    val heightValue = layout.height

    ElevatedCard(
        modifier = Modifier
            .then(if (widthValue != null) Modifier.width(widthValue.dp) else Modifier.fillMaxWidth())
            .then(if (heightValue != null) Modifier.height(heightValue.dp) else Modifier.wrapContentHeight())
            .padding(vertical = 4.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = effectiveStyle?.backgroundColor?.toComposeColor() ?: MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            when (element) {
                is OpenQuestionElement -> OpenQuestionUI(element, textColor, fontSize, fontFamily)
                is SelectQuestionElement -> SelectQuestionUI(element, textColor, fontSize, fontFamily)
                is MultipleQuestionElement -> MultipleQuestionUI(element, textColor, fontSize, fontFamily)
                is DropQuestionElement -> DropQuestionUI(element, textColor, fontSize, fontFamily)
            }
        }
    }
}

@Composable
private fun OpenQuestionUI(q: OpenQuestionElement, color: Color, size: androidx.compose.ui.unit.TextUnit, font: androidx.compose.ui.text.font.FontFamily?) {
    var text by remember { mutableStateOf("") }
    Text(text = q.label, color = color, fontSize = size, fontFamily = font, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        modifier = Modifier.fillMaxWidth(),
        textStyle = LocalTextStyle.current.copy(color = color, fontFamily = font),
        placeholder = { Text("Escriba su respuesta...") }
    )
}

@Composable
private fun SelectQuestionUI(q: SelectQuestionElement, color: Color, size: androidx.compose.ui.unit.TextUnit, font: androidx.compose.ui.text.font.FontFamily?) {
    var selected by remember { mutableIntStateOf(-1) }
    Text(text = "Seleccione una opción:", color = color, fontSize = size, fontFamily = font, fontWeight = FontWeight.Bold)
    q.options.forEachIndexed { index, option ->
        Row(
            Modifier.fillMaxWidth().clickable { selected = index }.padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = (selected == index), onClick = { selected = index })
            Text(text = option, color = color, fontSize = size, fontFamily = font)
        }
    }
}

@Composable
private fun MultipleQuestionUI(q: MultipleQuestionElement, color: Color, size: androidx.compose.ui.unit.TextUnit, font: androidx.compose.ui.text.font.FontFamily?) {
    val selectedIndices = remember { mutableStateListOf<Int>() }
    Text(text = "Selección múltiple:", color = color, fontSize = size, fontFamily = font, fontWeight = FontWeight.Bold)
    q.options.forEachIndexed { index, option ->
        Row(
            Modifier.fillMaxWidth().clickable { 
                if (selectedIndices.contains(index)) selectedIndices.remove(index) else selectedIndices.add(index)
            }.padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = selectedIndices.contains(index), onCheckedChange = null)
            Text(text = option, color = color, fontSize = size, fontFamily = font)
        }
    }
}

@Composable
private fun DropQuestionUI(q: DropQuestionElement, color: Color, size: androidx.compose.ui.unit.TextUnit, font: androidx.compose.ui.text.font.FontFamily?) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf("Seleccionar...") }
    val apiService = remember { ApiService() }

    DisposableEffect(Unit) {
        onDispose { apiService.close() }
    }
    
    // Estado para las opciones finales (cargadas o estáticas)
    var finalOptions by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(q.optionsSource) {
        when (val source = q.optionsSource) {
            is OptionsSource.Static -> {
                finalOptions = source.options
            }
            is OptionsSource.PokemonRange -> {
                isLoading = true
                finalOptions = apiService.getPokemonRange(source.start, source.end)
                isLoading = false
            }
        }
    }

    Text(text = q.label, color = color, fontSize = size, fontFamily = font, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(8.dp))
    
    Box {
        OutlinedButton(
            onClick = { if (!isLoading) expanded = true },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
                Text("Cargando Pokémon...", color = color)
            } else {
                Text(selectedOption, color = color)
            }
        }
        
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            finalOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        selectedOption = option
                        expanded = false
                    }
                )
            }
        }
    }
}
