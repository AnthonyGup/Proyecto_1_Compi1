package com.cunoc.compiforms

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cunoc.compiforms.form.model.*
import com.cunoc.compiforms.form.model.elements.*
import com.cunoc.compiforms.form.model.questions.*
import com.cunoc.compiforms.form.model.styles.*
import com.cunoc.compiforms.tokens.*
import java.io.StringReader

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppContent()
        }
    }
}

@Composable
private fun AppContent() {
    val colorScheme = MaterialTheme.colorScheme.copy(
        primary = Color(0xFF0F766E),
        secondary = Color(0xFFB45309),
        background = Color(0xFFF6F9F8),
        surface = Color(0xFFFFFFFF)
    )

    MaterialTheme(colorScheme = colorScheme) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            FormStudioScreen()
        }
    }
}

private enum class MainTab {
    EDITOR,
    RESPONDER
}

private val DEFAULT_TEMPLATE = """
$ FORM válido mínimo para tu MainActivity

number edad = 20
string nombre = "Ash"

SECTION [
  width: 420,
  height: 700,
  pointX: 0,
  pointY: 0,
  orientation: VERTICAL,
  elements: {
    TEXT [
      width: 380,
      height: 40,
      pointX: 10,
      pointY: 10,
      content: "Bienvenido al formulario @[ :smile: ]"
    ],
    OPEN_QUESTION [
      width: 360,
      height: 90,
      label: "¿Como te llamas?"
    ],
    DROP_QUESTION [
      width: 360,
      height: 90,
      label: "Elige un tipo",
      options: { "Fuego", "Agua", "Planta" },
      correct: 1
    ],
    SELECT_QUESTION [
      width: 360,
      height: 90,
      options: { "Kanto", "Johto", "Hoenn" },
      correct: 2
    ],
    MULTIPLE_QUESTION [
      width: 360,
      height: 110,
      options: { "Pikachu", "Charmander", "Bulbasaur", "Squirtle" },
      correct: { 1, 3 }
    ]
  }
]
""".trimIndent()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormStudioScreen() {
    var selectedTab by remember { mutableStateOf(MainTab.EDITOR) }
    var sourceCode by remember { mutableStateOf(TextFieldValue(DEFAULT_TEMPLATE)) }
    var parseResult by remember { mutableStateOf(ParseResult()) }
    val context = LocalContext.current

    fun analyzeCode() {
        try {
            val reader = StringReader(sourceCode.text)
            val lexer = Lexer(reader)
            val parser = Parser(lexer)
            parser.parse()
            val result = parser.parseResult
            parseResult = result
            
            if (result.isSuccess) {
                Toast.makeText(context, "Análisis exitoso", Toast.LENGTH_SHORT).show()
            } else {
                val errorCount = result.lexicalErrors.size + result.syntaxErrors.size + result.semanticErrors.size
                Toast.makeText(context, "Se encontraron ${errorCount} errores", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "PKM_FORM Studio",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = selectedTab.ordinal) {
                Tab(
                    selected = selectedTab == MainTab.EDITOR,
                    onClick = { selectedTab = MainTab.EDITOR },
                    text = { Text("Editor") }
                )
                Tab(
                    selected = selectedTab == MainTab.RESPONDER,
                    onClick = { selectedTab = MainTab.RESPONDER },
                    text = { Text("Contestar") }
                )
            }

            when (selectedTab) {
                MainTab.EDITOR -> {
                    val codeVerticalScroll = rememberScrollState()
                    val codeHorizontalScroll = rememberScrollState()

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Código Fuente", fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(8.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                        .background(Color.White, RoundedCornerShape(8.dp))
                                        .padding(12.dp)
                                ) {
                                    BasicTextField(
                                        value = sourceCode,
                                        onValueChange = { sourceCode = it },
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .verticalScroll(codeVerticalScroll)
                                            .horizontalScroll(codeHorizontalScroll),
                                        textStyle = TextStyle(
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 14.sp
                                        ),
                                        singleLine = false
                                    )

                                    if (sourceCode.text.isEmpty()) {
                                        Text(
                                            text = "Escribe código .form...",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Button(onClick = { analyzeCode() }) {
                                Text("Analizar Código")
                            }
                        }

                        if (parseResult.lexicalErrors.isNotEmpty() || parseResult.syntaxErrors.isNotEmpty() || parseResult.semanticErrors.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth().height(150.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                            ) {
                                Column(modifier = Modifier.padding(8.dp).verticalScroll(rememberScrollState())) {
                                    Text("Errores:", color = Color.Red, fontWeight = FontWeight.Bold)
                                    parseResult.lexicalErrors.forEach { Text("- Lexico: ${it.description} en [${it.line}, ${it.column}]", color = Color.Red, fontSize = 12.sp) }
                                    parseResult.syntaxErrors.forEach { Text("- Sintáctico: $it", color = Color.Red, fontSize = 12.sp) }
                                    parseResult.semanticErrors.forEach { Text("- Semántico: $it", color = Color.Red, fontSize = 12.sp) }
                                }
                            }
                        }
                    }
                }
                MainTab.RESPONDER -> {
                    if (parseResult.isSuccess && parseResult.document.elements.isNotEmpty()) {
                        FormRenderer(parseResult.document)
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                if (!parseResult.isSuccess) "Corrige los errores para ver el formulario" 
                                else "El formulario está vacío", 
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormRenderer(document: FormDocument) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(document.elements) { element ->
            ElementRenderer(element)
        }
    }
}

@Composable
private fun ElementRenderer(element: FormElement) {
    when (element) {
        is SectionElement -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    element.elements.forEach { subElement ->
                        ElementRenderer(subElement)
                    }
                }
            }
        }
        is TextElement -> {
            Text(
                text = element.content,
                style = MaterialTheme.typography.bodyLarge,
                fontSize = (element.styles?.textSize?.toFloat() ?: 16f).sp,
                color = element.styles?.textColor?.toComposeColor() ?: Color.Unspecified
            )
        }
        is OpenQuestionElement -> {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = element.label, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Escribe tu respuesta...") }
                )
            }
        }
        is SelectQuestionElement -> {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Pregunta de Selección", fontWeight = FontWeight.Medium)
                element.options.forEach { opcion ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = false, onClick = {})
                        Text(text = opcion)
                    }
                }
            }
        }
        is DropQuestionElement -> {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = element.label, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                var expanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(onClick = { expanded = true }) {
                        Text("Seleccionar opción...")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        val source = element.optionsSource
                        val optionsList = when (source) {
                            is OptionsSource.Static -> source.options
                            is OptionsSource.PokemonRange -> listOf("Pokemon ${source.start} a ${source.end}")
                        }
                        optionsList.forEach { option ->
                            DropdownMenuItem(text = { Text(option) }, onClick = { expanded = false })
                        }
                    }
                }
            }
        }
        is MultipleQuestionElement -> {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Selección Múltiple", fontWeight = FontWeight.Medium)
                element.options.forEach { opcion ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = false, onCheckedChange = {})
                        Text(text = opcion)
                    }
                }
            }
        }
        is TableElement -> {
            Column(modifier = Modifier.fillMaxWidth().border(1.dp, Color.Gray)) {
                element.rows.forEach { row ->
                    Row(modifier = Modifier.fillMaxWidth().border(0.5.dp, Color.LightGray)) {
                        row.cells.forEach { cell ->
                            Box(modifier = Modifier.weight(1f).padding(4.dp)) {
                                ElementRenderer(cell.element)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun ColorValue.toComposeColor(): Color {
    return when (this) {
        is ColorValue.Named -> {
            when (this.value) {
                NamedColor.RED -> Color.Red
                NamedColor.BLUE -> Color.Blue
                NamedColor.GREEN -> Color.Green
                NamedColor.PURPLE -> Color.Magenta
                NamedColor.SKY -> Color(0xFF87CEEB)
                NamedColor.YELLOW -> Color.Yellow
                NamedColor.BLACK -> Color.Black
                NamedColor.WHITE -> Color.White
            }
        }
        is ColorValue.Hex -> {
            try {
                val colorString = if (this.value.startsWith("#")) this.value else "#${this.value}"
                Color(android.graphics.Color.parseColor(colorString))
            } catch (e: Exception) {
                Color.Black
            }
        }
        else -> Color.Black
    }
}
