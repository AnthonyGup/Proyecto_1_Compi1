package com.cunoc.compiforms.ui


import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cunoc.compiforms.Lexer
import com.cunoc.compiforms.Parser
import com.cunoc.compiforms.data.remote.ApiService
import com.cunoc.compiforms.form.model.FormDocument
import com.cunoc.compiforms.form.model.ParseResult
import com.cunoc.compiforms.saved.FormSerializer
import com.cunoc.compiforms.saved.PkmDeserializer
import com.cunoc.compiforms.saved.PkmSerializer
import com.cunoc.compiforms.tokens.TokenInfo
import com.cunoc.compiforms.ui.renderers.ElementRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.text.font.FontFamily as ComposeFontFamily

private enum class MainTab {
    EDITOR,
    RESPONDER
}

fun analyzeFormCode(code: String): ParseResult {
    return try {
        val reader = StringReader(code)
        val lexer = Lexer(reader)
        val parser = Parser(lexer)
        parser.parse()
        parser.parseResult
    } catch (e: Exception) {
        ParseResult(semanticErrors = listOf("Error: ${e.message}"))
    }
}

/**
 * Función que genera un fragmento de código de una SECTION con una pregunta
 */
private fun generateSectionWithQuestion(type: String): String {
    val questionCode = when (type) {
        "OPEN" -> """
            OPEN_QUESTION [
                label: "Nueva Pregunta Abierta"
            ]
        """.trimIndent()
        "DROP" -> """
            DROP_QUESTION [
                label: "Selecciona una opción",
                options: {"Opción 1", "Opción 2"}
            ]
        """.trimIndent()
        "SELECT" -> """
            SELECT_QUESTION [
                label: "Selección Única",
                options: {"A", "B", "C"},
                correct: 0
            ]
        """.trimIndent()
        "MULTIPLE" -> """
            MULTIPLE_QUESTION [
                label: "Selección Múltiple",
                options: {"Opción 1", "Opción 2", "Opción 3"},
                correct: {0, 1}
            ]
        """.trimIndent()
        else -> ""
    }

    return """

SECTION [
    width: 400,
    height: 250,
    pointX: 0,
    pointY: 0,
    elements: {
        $questionCode
    }
]
    """.trimIndent()
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormStudioApp() {
    var selectedTab by remember { mutableStateOf(MainTab.EDITOR) }
    var sourceCode by remember { mutableStateOf(TextFieldValue(DEFAULT_TEMPLATE)) }
    var parseResult by remember { mutableStateOf(ParseResult()) }
    val context = LocalContext.current
    var codeTokens by remember { mutableStateOf(emptyList<TokenInfo>()) }
    
    // Estados para los menús
    var showMenu by remember { mutableStateOf(false) }
    var showQuestionMenu by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showServerConfigDialog by remember { mutableStateOf(false) }
    var serverFiles by remember { mutableStateOf(emptyList<String>()) }
    var isServerRequestInProgress by remember { mutableStateOf(false) }
    
    // Nuevos estados para guardado
    var showSaveDialog by remember { mutableStateOf(false) }
    var saveFileName by remember { mutableStateOf("") }

    // Servicios
    val apiService = remember { ApiService() }
    var serverHostInput by remember { mutableStateOf(apiService.getCurrentServerHost()) }
    val pkmSerializer = remember { PkmSerializer() }
    val pkmDeserializer = remember { PkmDeserializer() }
    val formSerializer = remember { FormSerializer() }
    val scope = rememberCoroutineScope()

    val codeVerticalScroll = rememberScrollState()
    val codeHorizontalScroll = rememberScrollState()
    val codeVisualTransformation = remember(codeTokens) { CodeVisualTransformation(codeTokens) }

    DisposableEffect(Unit) {
        onDispose { apiService.close() }
    }

    LaunchedEffect(Unit) {
      snapshotFlow { sourceCode.text }
        .debounce(120)
        .distinctUntilChanged()
        .collect { currentCode ->
          val tokens = withContext(Dispatchers.Default) {
              try {
                val lexer = Lexer(StringReader(currentCode))
                while (lexer.next_token().sym != com.cunoc.compiforms.sym.EOF) { }
                lexer.tokens
              } catch (e: Throwable) {
                emptyList()
              }
          }
          codeTokens = tokens
        }
    }

    val commonTextStyle = TextStyle(
        fontFamily = ComposeFontFamily.Monospace,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )

    // DIÁLOGO DE IMPORTACIÓN
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Archivos en Servidor") },
            text = {
                if (serverFiles.isEmpty()) {
                    Text("No hay archivos disponibles. Verifica que el servidor esté corriendo en tu PC.")
                } else {
                    LazyColumn(modifier = Modifier.height(300.dp)) {
                        items(serverFiles) { fileName ->
                            Text(
                                text = fileName,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isServerRequestInProgress) {
                                            Toast.makeText(context, "Espera, la solicitud anterior sigue en proceso", Toast.LENGTH_SHORT).show()
                                            return@clickable
                                        }
                                        scope.launch {
                                            isServerRequestInProgress = true
                                            Toast.makeText(context, "Descargando archivo del servidor...", Toast.LENGTH_SHORT).show()
                                            try {
                                                val pkmContent = apiService.downloadPkmFile(fileName)
                                                if (pkmContent != null) {
                                                    val deserializeResult = pkmDeserializer.deserializeWithDiagnostics(pkmContent)
                                                    val doc = deserializeResult.document
                                                    if (doc != null) {
                                                        val formCode = formSerializer.serialize(doc)
                                                        sourceCode = TextFieldValue(formCode)
                                                        showImportDialog = false
                                                        Toast.makeText(context, "Archivo cargado", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        val detail = deserializeResult.errorMessage ?: "Error sintactico al procesar $fileName"
                                                        Toast.makeText(context, detail, Toast.LENGTH_LONG).show()
                                                    }
                                                } else {
                                                    Toast.makeText(context, "No se pudo descargar el archivo", Toast.LENGTH_SHORT).show()
                                                }
                                            } finally {
                                                isServerRequestInProgress = false
                                            }
                                        }
                                    }
                                    .padding(12.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showImportDialog = false }) { Text("Cerrar") }
            }
        )
    }

    if (showServerConfigDialog) {
        AlertDialog(
            onDismissRequest = { showServerConfigDialog = false },
            title = { Text("Configurar Servidor") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Ingresa el host/IP del servidor Tomcat.")
                    OutlinedTextField(
                        value = serverHostInput,
                        onValueChange = { serverHostInput = it },
                        singleLine = true,
                        label = { Text("Host") },
                        placeholder = { Text("Ej: 192.168.100.147") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    apiService.updateServerHost(serverHostInput)
                    serverHostInput = apiService.getCurrentServerHost()
                    showServerConfigDialog = false
                    Toast.makeText(context, "Servidor: ${apiService.getCurrentServerHost()}", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showServerConfigDialog = false }) { Text("Cancelar") }
            }
        )
    }

    // DIÁLOGO DE GUARDADO
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Guardar en Servidor") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = saveFileName,
                        onValueChange = { saveFileName = it },
                        label = { Text("Nombre del archivo") },
                        placeholder = { Text("ejemplo_formulario") },
                        suffix = { Text(".pkm") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = saveFileName.isNotBlank() && !isServerRequestInProgress,
                    onClick = {
                        if (isServerRequestInProgress) {
                            Toast.makeText(context, "Espera, la solicitud anterior sigue en proceso", Toast.LENGTH_SHORT).show()
                            return@TextButton
                        }
                        val currentResult = analyzeFormCode(sourceCode.text)
                        if (currentResult.isSuccess) {
                            scope.launch {
                                isServerRequestInProgress = true
                                Toast.makeText(context, "Subiendo archivo al servidor...", Toast.LENGTH_SHORT).show()
                                try {
                                    val pkmContent = pkmSerializer.serialize(
                                        currentResult.document,
                                        "",
                                        ""
                                    )
                                    val finalName = if (saveFileName.endsWith(".pkm")) saveFileName else "$saveFileName.pkm"
                                    val response = apiService.uploadPkmFile(finalName, pkmContent)
                                    Toast.makeText(context, response, Toast.LENGTH_LONG).show()
                                    showSaveDialog = false
                                } finally {
                                    isServerRequestInProgress = false
                                }
                            }
                        }
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Compi Forms Studio", fontWeight = FontWeight.Bold) })
        },
        modifier = Modifier.fillMaxSize().imePadding()
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            TabRow(selectedTabIndex = selectedTab.ordinal) {
                Tab(selected = selectedTab == MainTab.EDITOR, onClick = { selectedTab = MainTab.EDITOR }, text = { Text("Editor") })
                Tab(selected = selectedTab == MainTab.RESPONDER, onClick = { selectedTab = MainTab.RESPONDER }, text = { Text("Contestar") })
            }

            when (selectedTab) {
                MainTab.EDITOR -> {
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Card(modifier = Modifier.fillMaxWidth().weight(1f), shape = RoundedCornerShape(12.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFF2B2B2B))
                                    .padding(12.dp)
                            ) {
                                BasicTextField(
                                    value = sourceCode,
                                    onValueChange = { sourceCode = it },
                                  visualTransformation = codeVisualTransformation,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(codeVerticalScroll)
                                        .horizontalScroll(codeHorizontalScroll),
                                    textStyle = commonTextStyle.copy(color = Color.White),
                                    singleLine = false
                                )

                                if (sourceCode.text.isEmpty()) {
                                    Text(
                                        text = "Escribe código .form...",
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                            
                            Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
                                Button(
                                    onClick = { showQuestionMenu = true },
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                ) {
                                    Text("Agregar Pregunta")
                                }
                                DropdownMenu(
                                    expanded = showQuestionMenu,
                                    onDismissRequest = { showQuestionMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Pregunta Abierta") },
                                        onClick = {
                                            sourceCode = sourceCode.copy(text = sourceCode.text + generateSectionWithQuestion("OPEN"))
                                            showQuestionMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Desplegable") },
                                        onClick = {
                                            sourceCode = sourceCode.copy(text = sourceCode.text + generateSectionWithQuestion("DROP"))
                                            showQuestionMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Selección Única") },
                                        onClick = {
                                            sourceCode = sourceCode.copy(text = sourceCode.text + generateSectionWithQuestion("SELECT"))
                                            showQuestionMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Selección Múltiple") },
                                        onClick = {
                                            sourceCode = sourceCode.copy(text = sourceCode.text + generateSectionWithQuestion("MULTIPLE"))
                                            showQuestionMenu = false
                                        }
                                    )
                                }
                            }

                            Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
                                IconButton(onClick = { showMenu = true }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Opciones")
                                }
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Guardar en Servidor (.pkm)") },
                                        onClick = { 
                                            showMenu = false
                                            val currentResult = analyzeFormCode(sourceCode.text)
                                            if (currentResult.isSuccess) {
                                                val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                                                saveFileName = "form_$timestamp"
                                                showSaveDialog = true
                                            } else {
                                                Toast.makeText(context, "Corrige errores primero", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Importar del Servidor") },
                                        onClick = { 
                                            showMenu = false
                                            if (isServerRequestInProgress) {
                                                Toast.makeText(context, "Espera, la solicitud anterior sigue en proceso", Toast.LENGTH_SHORT).show()
                                                return@DropdownMenuItem
                                            }
                                            scope.launch {
                                                isServerRequestInProgress = true
                                                Toast.makeText(context, "Conectando al servidor...", Toast.LENGTH_SHORT).show()
                                                try {
                                                    val files = apiService.listPkmFiles()
                                                    serverFiles = files
                                                    showImportDialog = true
                                                } finally {
                                                    isServerRequestInProgress = false
                                                }
                                            }
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Configurar Servidor") },
                                        onClick = {
                                            showMenu = false
                                            serverHostInput = apiService.getCurrentServerHost()
                                            showServerConfigDialog = true
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Limpiar Editor") },
                                        onClick = { 
                                            showMenu = false
                                            sourceCode = TextFieldValue("")
                                        }
                                    )
                                }
                            }

                            Button(onClick = { 
                                val result = analyzeFormCode(sourceCode.text)
                                parseResult = result
                                if (result.isSuccess) Toast.makeText(context, "Éxito", Toast.LENGTH_SHORT).show()
                            }) {
                                Text("Analizar Código")
                            }
                        }

                        if (!parseResult.isSuccess) {
                            Card(modifier = Modifier.fillMaxWidth().height(120.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))) {
                                Column(modifier = Modifier.padding(8.dp).verticalScroll(rememberScrollState())) {
                                    Text("Errores:", color = Color.Red, fontWeight = FontWeight.Bold)
                                    parseResult.lexicalErrors.forEach { ErrorItem("Léxico", it.description, it.line, it.column) }
                                    parseResult.syntaxErrors.forEach { Text("- Sintáctico: $it", color = Color.Red, fontSize = 12.sp) }
                                    parseResult.semanticErrors.forEach { Text("- Semántico: $it", color = Color(0xFFD32F2F), fontSize = 12.sp) }
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
                            Text("Sin datos válidos")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorItem(tipo: String, desc: String, linea: Int, columna: Int) {
    Text("[$tipo] $linea:$columna - $desc", color = Color.Red, fontSize = 12.sp)
}

@Composable
private fun FormRenderer(document: FormDocument) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items(document.elements) { element -> 
            ElementRenderer(element = element) 
        }
    }
}

private val DEFAULT_TEMPLATE = """
$ Programa completo con variables y formulario
number edad_usuario = 25
string nombre_usuario = "Carlos Lopez"
number experiencia = 5

IF (edad_usuario >= 18)
{
    SECTION [
        width: 500,
        height: 600,
        pointX: 10,
        pointY: 10,
        orientation: VERTICAL,
        elements: {
            TEXT [
                content: "Bienvenido " + nombre_usuario + " @[:smile:]"
            ],
            OPEN_QUESTION [
                label: "Cual es tu correo?"
            ],
            DROP_QUESTION [
                label: "Anos de experiencia",
                options: {"0-2", "3-5", "6-10", "11+"}
            ]
        }
    ]
}

$ Logica condicional
number nivel_acceso = 3
IF (nivel_acceso == 3 || nivel_acceso == 4)
{
    SECTION [
        width: 360,
        height: 120,
        pointX: 520,
        pointY: 10,
        elements: {
            TEXT [
                content: "Tienes acceso premium @[:star:5:]"
            ]
        }
    ]
}

$ Iteracion con generacion de secciones
FOR (pregunta_num in 1 .. 3)
{
    SECTION [
        width: 360,
        height: 120,
        pointX: 520,
        pointY: 140,
        elements: {
            OPEN_QUESTION [
                label: "Pregunta " + pregunta_num
            ]
        }
    ]
}

$ Acumulador y resultado
number puntuacion_total = 0
number respuesta1 = 10
number respuesta2 = 15
number respuesta3 = 20
puntuacion_total = respuesta1 + respuesta2 + respuesta3

IF (puntuacion_total >= 40)
{
    SECTION [
        width: 380,
        height: 100,
        pointX: 520,
        pointY: 270,
        elements: {
            TEXT [
                content: "Excelente desempeno"
            ]
        }
    ]
}
ELSE
{
    IF (puntuacion_total >= 30)
    {
        SECTION [
            width: 380,
            height: 100,
            pointX: 520,
            pointY: 380,
            elements: {
                TEXT [
                    content: "Buen desempeno"
                ]
            }
        ]
    }
    ELSE
    {
        SECTION [
            width: 380,
            height: 100,
            pointX: 520,
            pointY: 490,
            elements: {
                TEXT [
                    content: "Necesita mejorar"
                ]
            }
        ]
    }
}

$ Tabla dinamica valida
string col1_name = "Producto"
string col2_name = "Cantidad"
TABLE [
    width: 420,
    height: 220,
    pointX: 10,
    pointY: 620,
    elements: {
        [
            { TEXT [ content: col1_name ] },
            { TEXT [ content: col2_name ] }
        ],
        [
            { TEXT [ content: "Laptop" ] },
            { TEXT [ content: "3" ] }
        ],
        [
            { TEXT [ content: "Mouse" ] },
            { TEXT [ content: "15" ] }
        ]
    },
    styles [
        "border": (1, LINE, BLUE)
    ]
]

$ Seccion anidada compleja
SECTION [
    width: 600,
    height: 500,
    pointX: 10,
    pointY: 860,
    elements: {
        TEXT [
            content: "Sistema de Gestion"
        ],
        SECTION [
            width: 550,
            height: 180,
            pointX: 0,
            pointY: 0,
            elements: {
                TEXT [
                    content: "Datos del usuario"
                ],
                OPEN_QUESTION [
                    label: "Nombre"
                ]
            }
        ],
        SECTION [
            width: 550,
            height: 180,
            pointX: 0,
            pointY: 190,
            elements: {
                TEXT [
                    content: "Preferencias"
                ],
                OPEN_QUESTION [
                    label: "Cuentanos tus preferencias"
                ]
            }
        ]
    }
]

$ Conteo pares y reporte
number contador_iteracion = 0
FOR (i in 1 .. 10)
{
    IF (i % 2 == 0)
    {
        contador_iteracion = contador_iteracion + 1
    }
}

SECTION [
    width: 360,
    height: 120,
    pointX: 620,
    pointY: 620,
    elements: {
        TEXT [
            content: "Numeros pares encontrados: " + contador_iteracion
        ]
    }
]

$ Encuesta completa
string titulo_encuesta = "Encuesta de Satisfaccion"
number preguntas_totales = 5
SECTION [
    width: 560,
    height: 620,
    pointX: 620,
    pointY: 760,
    orientation: VERTICAL,
    elements: {
        TEXT [
            content: titulo_encuesta + " (Pregunta 1 de " + preguntas_totales + ")"
        ],
        OPEN_QUESTION [
            label: "Cual es tu nombre?"
        ],
        DROP_QUESTION [
            label: "Departamento",
            options: {"Ventas", "Operaciones", "Finanzas", "RH"}
        ],
        SELECT_QUESTION [
            options: {"Muy satisfecho", "Satisfecho", "Neutral", "Insatisfecho"}
        ],
        MULTIPLE_QUESTION [
            options: {"Comunicacion", "Liderazgo", "Eficiencia"},
            width: 500
        ],
        TEXT [
            content: "Gracias por completar la encuesta @[:heart:]"
        ]
    },
    styles [
        "background color": WHITE,
        "border": (2, DOUBLE, BLUE)
    ]
]

$ Regla final condicional
number score_usuario = 12
IF (score_usuario >= 12)
{
    SECTION [
        width: 420,
        height: 160,
        pointX: 620,
        pointY: 1390,
        elements: {
            DROP_QUESTION [
                label: "Deseas recibir mas informacion?",
                options: {"Si", "No"}
            ]
        }
    ]
}


""".trimIndent()
