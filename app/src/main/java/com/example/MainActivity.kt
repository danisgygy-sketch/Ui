package com.example

import android.app.Application
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.PairItem
import com.example.data.Question
import com.example.data.TryoutHistoryItem
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: TryoutViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(Color(0xFFF8FAFC))
                    ) {
                        AnimatedContent(
                            targetState = uiState.status,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "screenTransition"
                        ) { status ->
                            when (status) {
                                ScreenStatus.LANDING -> LandingScreen(viewModel, uiState)
                                ScreenStatus.SETUP -> SetupScreen(viewModel, uiState)
                                ScreenStatus.LOADING -> LoadingScreen(viewModel, uiState)
                                ScreenStatus.EXAM -> ExamScreen(viewModel, uiState)
                                ScreenStatus.RESULT -> ResultScreen(viewModel, uiState)
                                ScreenStatus.HISTORY -> HistoryScreen(viewModel, uiState)
                                ScreenStatus.REPORT -> ReportScreen(viewModel, uiState)
                                ScreenStatus.CHAT -> ChatScreen(viewModel, uiState)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 1. LANDING SCREEN
// ==========================================
@Composable
fun LandingScreen(viewModel: TryoutViewModel, uiState: TryoutUiState) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        
        // Header Logo/Badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(Color(0xFFEEF2FF), RoundedCornerShape(50.dp))
                .border(1.dp, Color(0xFFC7D2FE), RoundedCornerShape(50.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SmartToy,
                contentDescription = null,
                tint = Color(0xFF4F46E5),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "POWERED BY PAK REI AI PRO",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4F46E5),
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Pilih Mode Evaluasi",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF0F172A),
            textAlign = TextAlign.Center
        )
        Text(
            text = "Platform simulasi ujian adaptif dengan Memori Kemajuan Awan & stimulus visual terintegrasi.",
            fontSize = 14.sp,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Mode: Reguler
        InteractiveCard(
            title = "Simulasi Reguler",
            subtitle = "30 Soal terstruktur. Konfigurasi umum AI berstandar kurikulum nasional.",
            icon = Icons.Default.Layers,
            iconBg = Color(0xFFEFF6FF),
            iconColor = Color(0xFF2563EB),
            tintColor = Color(0xFF1E3A8A),
            onClick = { viewModel.setTryoutMode(TryoutMode.REGULER) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Mode: US
        InteractiveGradientCard(
            title = "Ujian Sekolah (US)",
            subtitle = "Simulasi AKM (40 Soal). Meliputi Variasi Kompleks, analisis silabus PDF, & Generasi Visual Konteks.",
            label = "NEW",
            gradient = Brush.linearGradient(listOf(Color(0xFF064E3B), Color(0xFF10B981))),
            icon = Icons.Default.School,
            onClick = { viewModel.setTryoutMode(TryoutMode.US) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Mode: Mega Tryout
        InteractiveGradientCard(
            title = "Mega Tryout Pro",
            subtitle = "Analisis PDF komprehensif, kalibrasi tiers kesulitan ekstrem S-Class, dan Mode TKA Nasional Resmi.",
            label = "PRO",
            gradient = Brush.linearGradient(listOf(Color(0xFF4F46E5), Color(0xFF9333EA))),
            icon = Icons.Default.Memory,
            onClick = { viewModel.setTryoutMode(TryoutMode.MEGA) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Divider
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFFE2E8F0)))
            Text(
                text = "MEMORI AI & CLOUD STATS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFFE2E8F0)))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Actions Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { viewModel.setScreenStatus(ScreenStatus.HISTORY) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.weight(1f).height(60.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, contentDescription = null, tint = Color(0xFF2563EB))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Riwayat", color = Color(0xFF334155), fontWeight = FontWeight.Bold)
                }
            }

            Button(
                onClick = { viewModel.setScreenStatus(ScreenStatus.REPORT) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.weight(1f).height(60.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PieChart, contentDescription = null, tint = Color(0xFF7C3AED))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Rapor & XP", color = Color(0xFF334155), fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Penebusan Mode
        val hasHistory = uiState.history.isNotEmpty()
        Button(
            onClick = {
                if (hasHistory) {
                    viewModel.startAdaptiveExam()
                } else {
                    Toast.makeText(context, "Selesaikan evaluasi Anda minimal sekali agar AI mengenali weaknesses!", Toast.LENGTH_LONG).show()
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (hasHistory) Color(0xFFFFE4E6) else Color(0xFFF1F5F9)
            ),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, if (hasHistory) Color(0xFFFECDD3) else Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth().height(64.dp),
            enabled = true
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Whatshot,
                    contentDescription = null,
                    tint = if (hasHistory) Color(0xFFE11D48) else Color(0xFF94A3B8)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "Mode Penebusan",
                        color = if (hasHistory) Color(0xFF9F1239) else Color(0xFF64748B),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = if (hasHistory) "AI Adaptive remedial hajar kelemahan" else "Terkunci (Selesaikan evaluasi dahulu)",
                        color = if (hasHistory) Color(0xFFBE123C) else Color(0xFF94A3B8),
                        fontSize = 10.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(30.dp))
        
        // Floating chat launcher shortcut
        Button(
            onClick = { viewModel.openChatConversation(contextual = false) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(30.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Chat Konsultasi Pak Rei", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun InteractiveCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    iconColor: Color,
    tintColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(iconBg, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = tintColor)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = subtitle, fontSize = 13.sp, color = Color(0xFF64748B), lineHeight = 18.sp)
        }
    }
}

@Composable
fun InteractiveGradientCard(
    title: String,
    subtitle: String,
    label: String,
    gradient: Brush,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .background(gradient)
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(text = label, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = subtitle, fontSize = 13.sp, color = Color.White.copy(alpha = 0.9f), lineHeight = 18.sp)
            }
        }
    }
}

// ==========================================
// 2. SETUP PROFILE SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(viewModel: TryoutViewModel, uiState: TryoutUiState) {
    val profile = uiState.userProfile
    val context = LocalContext.current
    var isIdentityCompiled by remember { mutableStateOf(false) }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.updateProfileBuilder { p -> p.copy(photoUri = it.toString()) } }
    }

    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.handlePdfSelection(it) }
    }

    val primaryColor = when (uiState.mode) {
        TryoutMode.US -> Color(0xFF059669)
        TryoutMode.MEGA -> Color(0xFF7C3AED)
        else -> Color(0xFF2563EB)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Persiapan Simulasi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.restartAll() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isIdentityCompiled) {
                // Settings Form
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "Matriks Data Siswa",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = profile.name,
                            onValueChange = { viewModel.updateProfileBuilder { p -> p.copy(name = it) } },
                            label = { Text("Nama Lengkap") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = profile.age,
                                onValueChange = { viewModel.updateProfileBuilder { p -> p.copy(age = it) } },
                                label = { Text("Umur") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = profile.kelas,
                                onValueChange = { viewModel.updateProfileBuilder { p -> p.copy(kelas = it) } },
                                label = { Text("Kelas") },
                                modifier = Modifier.weight(1.2f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = profile.sekolah,
                            onValueChange = { viewModel.updateProfileBuilder { p -> p.copy(sekolah = it) } },
                            label = { Text("Asal Sekolah") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Mode Specific Settings
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                        Spacer(modifier = Modifier.height(16.dp))

                        when (uiState.mode) {
                            TryoutMode.REGULER -> {
                                Text(
                                    text = "Konfigurasi Reguler",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2563EB)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = profile.subjectName,
                                    onValueChange = { viewModel.updateProfileBuilder { p -> p.copy(subjectName = it) } },
                                    label = { Text("Fokus Pelajaran (Cth: Biologi Sel)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                            TryoutMode.US -> {
                                Text(
                                    text = "Konfigurasi Ujian Sekolah",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF059669)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = profile.nisn,
                                    onValueChange = {
                                        viewModel.updateProfileBuilder { p -> p.copy(nisn = it) }
                                        if (it == "01202020") {
                                            viewModel.updateProfileBuilder { p -> 
                                                p.copy(name = "Fahri", kelas = "IX B", sekolah = "SMPN 2 SIMPANG KATIS", age = "15")
                                            }
                                            Toast.makeText(context, "Kode Master Diterima! Data terisi otomatis.", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    label = { Text("NISN (Masukkan 01202020 untuk mock)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = profile.subjectName,
                                    onValueChange = { viewModel.updateProfileBuilder { p -> p.copy(subjectName = it) } },
                                    label = { Text("Mata Pelajaran Ujian") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = profile.indicators,
                                    onValueChange = { viewModel.updateProfileBuilder { p -> p.copy(indicators = it) } },
                                    label = { Text("Indikator Kompetensi") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Text("Level Kognitif Bloom:", fontSize = 12.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                val bloomOptions = listOf("Campuran C3 - C4", "HOTS Ekstrem C4 - C6", "Dasar C1 - C2")
                                var selectedBloomIdx by remember { mutableStateOf(0) }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    bloomOptions.forEachIndexed { i, opt ->
                                        val isSel = selectedBloomIdx == i
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isSel) Color(0xFFECFDF5) else Color(0xFFF8FAFC))
                                                .border(2.dp, if (isSel) Color(0xFF10B981) else Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                                                .clickable { 
                                                    selectedBloomIdx = i
                                                    viewModel.updateProfileBuilder { p -> p.copy(bloomLevel = opt, difficultyCode = "US") }
                                                }
                                                .padding(10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(opt, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSel) Color(0xFF065F46) else Color(0xFF64748B), textAlign = TextAlign.Center)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { pdfLauncher.launch("application/pdf") },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFECFDF5), contentColor = Color(0xFF047857)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.UploadFile, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(if (profile.pdfFileUri != null) "Syllabus Diunggah!" else "Upload PDF Silabus (Opsional)")
                                }
                            }
                            TryoutMode.MEGA -> {
                                Text(
                                    text = "Konfigurasi Mega Mode (Syllabus PDF Wajib)",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF7C3AED)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = profile.nisn,
                                    onValueChange = {
                                        viewModel.updateProfileBuilder { p -> p.copy(nisn = it) }
                                        if (it == "01202020") {
                                            viewModel.updateProfileBuilder { p -> 
                                                p.copy(name = "Fahri", kelas = "IX B", sekolah = "SMPN 2 SIMPANG KATIS", age = "15")
                                            }
                                            Toast.makeText(context, "Kode Master Diterima!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    label = { Text("NISN") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = profile.subjectName,
                                    onValueChange = { viewModel.updateProfileBuilder { p -> p.copy(subjectName = it) } },
                                    label = { Text("Fokus Pokok Bahasan") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = { pdfLauncher.launch("application/pdf") },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5F3FF), contentColor = Color(0xFF6D28D9)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.UploadFile, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(if (profile.pdfFileUri != null) "Syllabus Diunggah!" else "Syllabus PDF (WAJIB)")
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Tingkat Kalibrasi AI:", fontSize = 12.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                var selectedTier by remember { mutableStateOf("A") }
                                val tiers = listOf("A" to "Normal", "S" to "HOTS X", "TKA" to "TKA Resmi")
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    tiers.forEach { (code, text) ->
                                        val isSel = selectedTier == code
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isSel) Color(0xFFF5F3FF) else Color(0xFFF8FAFC))
                                                .border(2.dp, if (isSel) Color(0xFF7C3AED) else Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                                                .clickable { 
                                                    selectedTier = code
                                                    viewModel.updateProfileBuilder { p -> p.copy(difficultyCode = code, difficultyText = text) }
                                                }
                                                .padding(10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSel) Color(0xFF5B21B6) else Color(0xFF64748B), textAlign = TextAlign.Center)
                                        }
                                    }
                                }
                            }
                            else -> {}
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Trigger visual stimulus toggle
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Generasi Gambar Konteks (Imagen 4.0)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                                Text("Munculkan diagram & kurva visual analisis", fontSize = 10.sp, color = Color(0xFF64748B))
                            }
                            Switch(
                                checked = profile.generateImages,
                                onCheckedChange = { viewModel.updateProfileBuilder { p -> p.copy(generateImages = it) } }
                            )
                        }

                        // Profile Photo picker
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { photoLauncher.launch("image/*") },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = Color(0xFF475569)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (profile.photoUri != null) "Foto Profil Dipilih!" else "Pilih Foto Profil (Opsional)")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (profile.name.isBlank()) {
                            Toast.makeText(context, "Silakan isi nama Anda!", Toast.LENGTH_SHORT).show()
                        } else if (uiState.mode == TryoutMode.MEGA && profile.pdfFileUri == null) {
                            Toast.makeText(context, "Sistem Mega Mode wajib mengunggah PDF Silabus!", Toast.LENGTH_SHORT).show()
                        } else {
                            isIdentityCompiled = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text("Kompilasi Identitas", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            } else {
                // Interactive Card display
                Spacer(modifier = Modifier.height(20.dp))
                StudentExamBadge(profile, uiState)
                
                Spacer(modifier = Modifier.height(30.dp))
                
                Button(
                    onClick = { viewModel.startAiPreparation() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Inisiasi Sistem Pak Rei", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = Color(0xFFFBBF24))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { isIdentityCompiled = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Text("Ubah Data Persiapan", color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StudentExamBadge(profile: UserProfile, uiState: TryoutUiState) {
    val gradientBrush = when (uiState.mode) {
        TryoutMode.US -> Brush.linearGradient(listOf(Color(0xFF064E3B), Color(0xFF10B981)))
        TryoutMode.MEGA -> Brush.linearGradient(listOf(Color(0xFF4F46E5), Color(0xFF9333EA)))
        TryoutMode.ADAPTIVE -> Brush.linearGradient(listOf(Color(0xFFEF4444), Color(0xFFF97316)))
        else -> Brush.linearGradient(listOf(Color(0xFF1E3A8A), Color(0xFF3B82F6)))
    }

    val badgeTitle = when (uiState.mode) {
        TryoutMode.US -> "UJIAN SEKOLAH (US)"
        TryoutMode.MEGA -> "MEGA TRYOUT PRO"
        TryoutMode.ADAPTIVE -> "PENEBUSAN ADAPTIF"
        else -> "SIMULASI REGULER"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .background(gradientBrush)
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header of the Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("AI", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.Black)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = badgeTitle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White.copy(alpha = 0.9f),
                            letterSpacing = 1.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "AI-${profile.nisn.takeLast(4).ifEmpty { "0426" }}",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))

                // Student Details block
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Profile Photo
                    Box(
                        modifier = Modifier
                            .size(86.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .border(2.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                    ) {
                        if (profile.photoUri != null) {
                            AsyncImage(
                                model = profile.photoUri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .size(48.dp)
                                    .align(Alignment.Center)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = profile.subjectName.ifEmpty { "MATERI UMUM" }.uppercase(),
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = profile.name.uppercase(),
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Box(
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "KELAS ${profile.kelas.uppercase()}",
                                color = Color(0xFFFBBF24),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = profile.sekolah.uppercase(),
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = if (uiState.mode == TryoutMode.ADAPTIVE) "20M" else "75M",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = profile.difficultyCode.uppercase(),
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. LOADING SCREEN
// ==========================================
@Composable
fun LoadingScreen(viewModel: TryoutViewModel, uiState: TryoutUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = Color(0xFF2563EB),
            strokeWidth = 5.dp,
            modifier = Modifier.size(72.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Sistem Sedang Berpikir...",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF0F172A)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = uiState.loadingText,
            fontSize = 14.sp,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 30.dp)
        )

        Spacer(modifier = Modifier.height(30.dp))
        Text(
            text = "Menganalisis parameter, memvalidasi fakta kurikulum, meracik stimulus kognitif, dan mengoptimalkan Tiers.",
            fontSize = 11.sp,
            color = Color(0xFF94A3B8),
            fontStyle = FontStyle.Italic,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

// ==========================================
// 4. EXAM & REVIEWS SCREEN
// ==========================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExamScreen(viewModel: TryoutViewModel, uiState: TryoutUiState) {
    val question = uiState.questions.getOrNull(uiState.currentIdx)
    val answer = uiState.answers[uiState.currentIdx]
    val isReview = uiState.isReviewMode

    val minutes = (uiState.timerSeconds / 60).toString().padStart(2, '0')
    val seconds = (uiState.timerSeconds % 60).toString().padStart(2, '0')

    Column(modifier = Modifier.fillMaxSize()) {
        // App Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9))
                    ) {
                        if (uiState.userProfile.photoUri != null) {
                            AsyncImage(
                                model = uiState.userProfile.photoUri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.align(Alignment.Center))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(uiState.userProfile.name, fontWeight = FontWeight.Black, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(uiState.userProfile.subjectName.uppercase(), color = Color(0xFF2563EB), fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }

                // Countdown Timer Box
                Box(
                    modifier = Modifier
                        .background(
                            if (isReview) Color(0xFFEEF2FF) else (if (uiState.timerSeconds < 300) Color(0xFFFFE4E6) else Color(0xFF0F172A)),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isReview) "REVIEW" else "$minutes:$seconds",
                        color = if (isReview) Color(0xFF4F46E5) else (if (uiState.timerSeconds < 300) Color(0xFFE11D48) else Color.White),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                }

                Button(
                    onClick = {
                        if (isReview) viewModel.exitReview() else viewModel.submitExam()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isReview) Color(0xFFEEF2FF) else Color(0xFFFEF2F2),
                        contentColor = if (isReview) Color(0xFF4F46E5) else Color(0xFFEF4444)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(if (isReview) "Tutup" else "Selesai", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (question == null) {
            // Loading placeholder
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFF2563EB))
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Menyiapkan lembar tanya-jawab...", fontSize = 13.sp, color = Color(0xFF64748B))
                }
            }
        } else {
            // Launch image generation if required
            LaunchedEffect(uiState.currentIdx) {
                if (uiState.userProfile.generateImages && question.cachedImageBase64 == null) {
                    viewModel.startImageGenerationForIndex(uiState.currentIdx)
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Navigation Indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFEFF6FF), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                "SOAL ${uiState.currentIdx + 1}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF2563EB)
                            )
                        }
                        Text(
                            "  /  ${uiState.totalTarget}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(Color(0xFFEEF2FF), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = question.topic.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF4F46E5)
                        )
                    }
                }

                // AI Generated image helper
                if (uiState.userProfile.generateImages && !question.imagePrompt.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFE2E8F0))
                            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (question.cachedImageBase64 != null) {
                            AsyncImage(
                                model = question.cachedImageBase64,
                                contentDescription = "Stimulus visual AI",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = Color(0xFF7C3AED), modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Menggambar stimulus visual AI...", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Question statement Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFEEF2FF)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        val badgeLabel = when (question.type) {
                            "kompleks" -> "PILIHAN GANDA KOMPLEKS"
                            "benarsalah" -> "BENAR / SALAH MATRIX"
                            "menjodohkan" -> "MENJODOHKAN MATRIKS"
                            else -> "PILIHAN GANDA TUNGGAL"
                        }
                        val badgeColor = when (question.type) {
                            "kompleks" -> Color(0xFF7C3AED)
                            "benarsalah" -> Color(0xFFD97706)
                            "menjodohkan" -> Color(0xFF059669)
                            else -> Color(0xFF2563EB)
                        }

                        Box(
                            modifier = Modifier
                                .background(badgeColor.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(badgeLabel, fontSize = 9.sp, fontWeight = FontWeight.Black, color = badgeColor)
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = question.text,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B),
                            lineHeight = 22.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Answer Options Container
                when (question.type) {
                    "pilgan" -> {
                        question.options.forEachIndexed { i, opt ->
                            val isChecked = answer == i
                            val isCorrect = question.pilganAnswer == i
                            
                            val outlineColor = if (isReview) {
                                if (isCorrect) Color(0xFF10B981) else (if (isChecked) Color(0xFFEF4444) else Color(0xFFE2E8F0))
                            } else {
                                if (isChecked) Color(0xFF2563EB) else Color(0xFFE2E8F0)
                            }
                            val bg = if (isReview) {
                                if (isCorrect) Color(0xFFECFDF5) else (if (isChecked) Color(0xFFFEF2F2) else Color.White)
                            } else {
                                if (isChecked) Color(0xFFEFF6FF) else Color.White
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp)
                                    .clickable { viewModel.setAnswer(uiState.currentIdx, i) },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = bg),
                                border = BorderStroke(2.dp, outlineColor)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isChecked && !isReview) Color(0xFF2563EB) else Color(0xFFF1F5F9)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = listOf("A", "B", "C", "D")[i],
                                            color = if (isChecked && !isReview) Color.White else Color(0xFF475569),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(opt, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
                                }
                            }
                        }
                    }

                    "kompleks" -> {
                        val currentList = (answer as? List<*>)?.mapNotNull { it as? Int } ?: emptyList()
                        question.options.forEachIndexed { i, opt ->
                            val isChecked = currentList.contains(i)
                            val isCorrect = question.kompleksAnswer.contains(i)

                            val outlineColor = if (isReview) {
                                if (isCorrect) Color(0xFF10B981) else (if (isChecked) Color(0xFFEF4444) else Color(0xFFE2E8F0))
                            } else {
                                if (isChecked) Color(0xFF7C3AED) else Color(0xFFE2E8F0)
                            }
                            val bg = if (isReview) {
                                if (isCorrect) Color(0xFFECFDF5) else (if (isChecked) Color(0xFFFEF2F2) else Color.White)
                            } else {
                                if (isChecked) Color(0xFFF5F3FF) else Color.White
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp)
                                    .clickable {
                                        val newList = currentList.toMutableList()
                                        if (newList.contains(i)) newList.remove(i) else newList.add(i)
                                        viewModel.setAnswer(uiState.currentIdx, newList)
                                    },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = bg),
                                border = BorderStroke(2.dp, outlineColor)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isChecked) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                        contentDescription = null,
                                        tint = if (isChecked) Color(0xFF7C3AED) else Color(0xFF94A3B8)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(opt, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
                                }
                            }
                        }
                    }

                    "benarsalah" -> {
                        val currentList = (answer as? List<*>)?.mapNotNull { it as? Boolean } ?: listOf(null, null, null, null)
                        question.statements.forEachIndexed { i, stmt ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(stmt, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        // Benar Button
                                        val isBenarChecked = currentList.getOrNull(i) == true
                                        val isBenarCorrect = question.benarSalahAnswer.getOrNull(i) == true
                                        val trueBorder = if (isReview) {
                                            if (isBenarCorrect) Color(0xFF10B981) else (if (isBenarChecked) Color(0xFFEF4444) else Color(0xFFCBD5E1))
                                        } else {
                                            if (isBenarChecked) Color(0xFF10B981) else Color(0xFFCBD5E1)
                                        }

                                        Button(
                                            onClick = {
                                                val newList = currentList.toMutableList()
                                                while (newList.size <= i) newList.add(null)
                                                newList[i] = true
                                                viewModel.setAnswer(uiState.currentIdx, newList)
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isBenarChecked) Color(0xFFECFDF5) else Color.White,
                                                contentColor = if (isBenarChecked) Color(0xFF047857) else Color(0xFF475569)
                                            ),
                                            border = BorderStroke(2.dp, trueBorder)
                                        ) {
                                            Text("BENAR", fontSize = 10.sp, fontWeight = FontWeight.Black)
                                        }

                                        // Salah Button
                                        val isSalahChecked = currentList.getOrNull(i) == false
                                        val isSalahCorrect = question.benarSalahAnswer.getOrNull(i) == false
                                        val falseBorder = if (isReview) {
                                            if (isSalahCorrect) Color(0xFF10B981) else (if (isSalahChecked) Color(0xFFEF4444) else Color(0xFFCBD5E1))
                                        } else {
                                            if (isSalahChecked) Color(0xFFEF4444) else Color(0xFFCBD5E1)
                                        }

                                        Button(
                                            onClick = {
                                                val newList = currentList.toMutableList()
                                                while (newList.size <= i) newList.add(null)
                                                newList[i] = false
                                                viewModel.setAnswer(uiState.currentIdx, newList)
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isSalahChecked) Color(0xFFFEF2F2) else Color.White,
                                                contentColor = if (isSalahChecked) Color(0xFFB91C1C) else Color(0xFF475569)
                                            ),
                                            border = BorderStroke(2.dp, falseBorder)
                                        ) {
                                            Text("SALAH", fontSize = 10.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "menjodohkan" -> {
                        val currentMap = (answer as? Map<*, *>)?.map { it.key as Int to it.value as String }?.toMap() ?: emptyMap()
                        question.menjodohkanPairs.forEachIndexed { i, pairItem ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(pairItem.left, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                                    Spacer(modifier = Modifier.height(10.dp))

                                    var expanded by remember { mutableStateOf(false) }
                                    val selectedVal = currentMap[i] ?: ""
                                    val isCorrect = selectedVal == pairItem.right

                                    Box {
                                        Button(
                                            onClick = { if (!isReview) expanded = true },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isReview) (if (isCorrect) Color(0xFFECFDF5) else Color(0xFFFEF2F2)) else Color(0xFFF8FAFC),
                                                contentColor = Color(0xFF0F172A)
                                            ),
                                            border = BorderStroke(
                                                1.dp, 
                                                if (isReview) (if (isCorrect) Color(0xFF10B981) else Color(0xFFEF4444)) else Color(0xFFE2E8F0)
                                            )
                                        ) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text(selectedVal.ifEmpty { "Pilih pasangan..." }, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                            }
                                        }
                                        DropdownMenu(
                                            expanded = expanded,
                                            onDismissRequest = { expanded = false }
                                        ) {
                                            question.shuffledRights.forEach { optionText ->
                                                DropdownMenuItem(
                                                    text = { Text(optionText, fontWeight = FontWeight.Medium) },
                                                    onClick = {
                                                        expanded = false
                                                        val newMap = currentMap.toMutableMap()
                                                        newMap[i] = optionText
                                                        viewModel.setAnswer(uiState.currentIdx, newMap)
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    if (isReview && !isCorrect) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFFECFDF5), RoundedCornerShape(8.dp))
                                                .padding(8.dp)
                                        ) {
                                            Text(
                                                "Benar: ${pairItem.right}",
                                                color = Color(0xFF047857),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // AI Tutor Commentary / Explanations inside Review Screen
                if (isReview && !question.explanation.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                        border = BorderStroke(1.dp, Color(0xFFDBEAFE))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFFF59E0B))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("PEMBAHASAN PAK REI", color = Color(0xFF1E40AF), fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = question.explanation,
                                color = Color(0xFF1E3A8A),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Controls Footer
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val pAvailable = uiState.currentIdx > 0
                    Button(
                        onClick = { viewModel.changeQuestion(-1) },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF64748B)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        enabled = pAvailable
                    ) {
                        Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Kembali", fontWeight = FontWeight.Bold)
                    }

                    val isLast = uiState.currentIdx == uiState.totalTarget - 1
                    Button(
                        onClick = { viewModel.changeQuestion(1) },
                        modifier = Modifier
                            .weight(1.2f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLast) Color(0xFF10B981) else Color(0xFF2563EB),
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = if (isLast) (if (isReview) "Selesai" else "Kumpul") else "Lanjut",
                            fontWeight = FontWeight.Bold
                        )
                        if (!isLast) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. EVALUATE RESULTS SCREEN
// ==========================================
@Composable
fun ResultScreen(viewModel: TryoutViewModel, uiState: TryoutUiState) {
    val res = uiState.finalResult
    val scrollState = rememberScrollState()

    if (res == null) {
        LoadingScreen(viewModel, uiState.copy(loadingText = "Menghitung nilai evaluasi Anda..."))
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color(0xFFEEF2FF), RoundedCornerShape(50.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Flag, contentDescription = null, tint = Color(0xFF3B82F6))
                Spacer(modifier = Modifier.width(6.dp))
                Text("EVALUASI SELESAI!", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF1D4ED8))
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Laporan Analisis Pak Rei", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
            Spacer(modifier = Modifier.height(24.dp))

            // Score Dial Card
            val statusColor = if (res.score >= 80) Color(0xFF10B981) else (if (res.score >= 60) Color(0xFFF59E0B) else Color(0xFFEF4444))
            val statusText = if (res.score >= 80) "LULUS / EXCELLENT" else (if (res.score >= 60) "CUKUP / AMAN" else "REMEDIAL / KRITIS")
            
            val statusBg = when (uiState.mode) {
                TryoutMode.US -> Brush.linearGradient(listOf(Color(0xFF0F172A), Color(0xFF111827)))
                else -> Brush.linearGradient(listOf(Color(0xFF0F172A), Color(0xFF1E293B)))
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(statusBg)
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Skor Evaluasi Absolut", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${res.score}",
                            fontSize = 72.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            lineHeight = 72.sp
                        )
                        Text("Skala 0 - 100", fontSize = 10.sp, color = Color(0xFF64748B))
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .background(statusColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .border(1.dp, statusColor, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(statusText, fontSize = 11.sp, fontWeight = FontWeight.Black, color = statusColor)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Accuracy breakdowns
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ResultCompactStat(label = "Poin Didapat", value = "${res.correctPts}", icon = Icons.Default.CheckCircle, color = Color(0xFF10B981), modifier = Modifier.weight(1f))
                ResultCompactStat(label = "Poin Hilang", value = "${res.wrongPts}", icon = Icons.Default.Cancel, color = Color(0xFFEF4444), modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ResultCompactStat(label = "Durasi", value = "${res.timeUsed}m", icon = Icons.Default.HistoryToggleOff, color = Color(0xFF2563EB), modifier = Modifier.weight(1f))
                ResultCompactStat(label = "Mode", value = uiState.mode.name, icon = Icons.Default.Settings, color = Color(0xFF7C3AED), modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Catatan Pak Rei Speech text
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFEFF6FF))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SmartToy, contentDescription = null, tint = Color(0xFF4F46E5))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("CATATAN EVALUASI PAK REI", color = Color(0xFF312E81), fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "\"${res.comment}\"",
                        fontSize = 14.sp,
                        color = Color(0xFF1E293B),
                        fontWeight = FontWeight.SemiBold,
                        fontStyle = FontStyle.Italic,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Weakness diagnoses list
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Dangerous, contentDescription = null, tint = Color(0xFFEF4444))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("PETA KELEMAHAN UTAMA", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    if (res.summaries.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Hebat! Tidak ditemukan kelemahan yang dominan pada evaluasi ini.", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), textAlign = TextAlign.Center)
                        }
                    } else {
                        res.summaries.forEach { summary ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp)
                                    .background(Color.White, RoundedCornerShape(12.dp))
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                                    .padding(14.dp)
                            ) {
                                Column {
                                    Text(summary.topic.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFFBE1233))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(summary.explanation, fontSize = 12.sp, color = Color(0xFF64748B), lineHeight = 16.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Action Buttons
            Button(
                onClick = { viewModel.startReview() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
            ) {
                Icon(Icons.Default.FindInPage, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Bahas Soal & Review", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { viewModel.openChatConversation(contextual = true) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
            ) {
                Icon(Icons.Default.Forum, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Konsultasi (Mode Les)", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { viewModel.restartAll() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF475569))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Kembali ke Menu Utama", color = Color(0xFF475569), fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun ResultCompactStat(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFEEF2FF))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(label, fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E293B))
        }
    }
}

// ==========================================
// 6. LONG HISTORY BROWSER
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: TryoutViewModel, uiState: TryoutUiState) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Riwayat Evaluasi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.setScreenStatus(ScreenStatus.LANDING) }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.history.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearHistory() }) {
                            Icon(imageVector = Icons.Default.DeleteForever, contentDescription = "Clear All", tint = Color(0xFFEF4444))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        if (uiState.history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CloudQueue, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(72.dp))
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Memori Awan Kosong", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF64748B))
                    Text("Selesaikan tryout untuk menyimpan jejak belajarmu.", fontSize = 12.sp, color = Color(0xFF94A3B8))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.history) { item ->
                    HistoryItemCard(item)
                }
            }
        }
    }
}

@Composable
fun HistoryItemCard(item: TryoutHistoryItem) {
    val scoreColor = if (item.score >= 80) Color(0xFF10B981) else (if (item.score >= 60) Color(0xFFF59E0B) else Color(0xFFEF4444))
    val scoreBg = if (item.score >= 80) Color(0xFFECFDF5) else (if (item.score >= 60) Color(0xFFFFFBEB) else Color(0xFFFEF2F2))

    val date = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(item.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFEFF2FF))
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        "${item.mode.uppercase()} - ${item.difficulty.uppercase()}",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF475569)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = item.subject,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(10.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(date, fontSize = 11.sp, color = Color(0xFF94A3B8))
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(scoreBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${item.score}",
                    color = scoreColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

// ==========================================
// 7. RAPOR & XP LEVEL DISPLAY
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(viewModel: TryoutViewModel, uiState: TryoutUiState) {
    val context = LocalContext.current
    val weaknessesList = remember(uiState.history) {
        val weaknesses = mutableMapOf<String, Int>()
        uiState.history.forEach { item ->
            try {
                val arr = JSONArray(item.mistakesJson)
                for (i in 0 until arr.length()) {
                    val topic = arr.getString(i)
                    weaknesses[topic] = (weaknesses[topic] ?: 0) + 1
                }
            } catch (e: Exception) {}
        }
        weaknesses.entries.sortedByDescending { it.value }.take(3)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Laporan Studi & XP", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.setScreenStatus(ScreenStatus.LANDING) }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A))
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Rank Card
            val badgeColor = when (uiState.userRankBadge) {
                "MYTHIC" -> Color(0xFFF43F5E)
                "PLATINUM" -> Color(0xFF22D3EE)
                "GOLD" -> Color(0xFFFBBF24)
                "SILVER" -> Color(0xFFE2E8F0)
                else -> Color(0xFFD97706)
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF334155))
                            .border(3.dp, badgeColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        val icon = when (uiState.userRankBadge) {
                            "MYTHIC" -> Icons.Default.EmojiEvents
                            "PLATINUM" -> Icons.Default.Diamond
                            "GOLD" -> Icons.Default.WorkspacePremium
                            "SILVER" -> Icons.Default.Security
                            else -> Icons.Default.School
                        }
                        Icon(icon, contentDescription = null, tint = badgeColor, modifier = Modifier.size(36.dp))
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = uiState.userProfile.name.ifBlank { "Pengguna" },
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = uiState.userRankTitle,
                                color = badgeColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "•  Total XP: ${uiState.totalXP}",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { uiState.xpProgress },
                            color = badgeColor,
                            trackColor = Color(0xFF334155),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (uiState.userRankBadge == "MYTHIC") "MAX RANK REACHED!" else "${uiState.totalXP} / ${uiState.nextRankXP} XP menuju level berikutnya",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Two stat boxes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Rata-Rata Skor", color = Color(0xFF64748B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${uiState.averageScore}", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Total Tryout", color = Color(0xFF64748B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${uiState.history.size}", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Peta Kelemahan Utama (Statistics of wrong answers)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFF2D1B24), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Adjust, contentDescription = null, tint = Color(0xFFF43F5E), modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Diagnosa Kelemahan", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }

                        if (weaknessesList.isNotEmpty()) {
                            Button(
                                onClick = { viewModel.startAdaptiveExam() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Whatshot, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("HAJAR", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.White)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        "Berdasarkan jejak evaluasi awan yang terekam, AI menemukan kelemahan mendasar Anda pada sub-terkait berikut:",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (weaknessesList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF334155).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Hebat! Anda tidak memiliki jaring kelemahan materi.", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                        }
                    } else {
                        weaknessesList.forEach { (topic, count) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp)
                                    .background(Color(0xFF0F172A), RoundedCornerShape(14.dp))
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = topic.uppercase(),
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF450A0A), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("$count X SALAH", color = Color(0xFFFCA5A5), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 8. CHAT CONSULT DIRECTLY WITH PAK REI TUTOR
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: TryoutViewModel, uiState: TryoutUiState) {
    var txtInput by remember { mutableStateOf("") }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    // Autoscroll chat list
    LaunchedEffect(uiState.chatHistory.size) {
        if (uiState.chatHistory.isNotEmpty()) {
            listState.animateScrollToItem(uiState.chatHistory.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Pak Rei - AI Tutor", fontWeight = FontWeight.Black, fontSize = 16.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).background(Color(0xFF10B981), CircleShape))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ONLINE & SIAP MEMBANTU", color = Color(0xFF10B981), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.exitReview() }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF1F5F9))
        ) {
            // Live scrollable conversation
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(uiState.chatHistory) { msg ->
                    ChatBubble(msg)
                }
                if (uiState.isChatThinking) {
                    item {
                        ThinkingBubble()
                    }
                }
            }

            // Input panel
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = txtInput,
                        onValueChange = { txtInput = it },
                        placeholder = { Text("Tanyakan konsep, evaluasi, atau curhat...", fontSize = 13.sp) },
                        modifier = Modifier.weight(1f),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (txtInput.isNotBlank()) {
                                viewModel.sendChatMessage(txtInput)
                                txtInput = ""
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFF4F46E5), CircleShape)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(msg: ChatMessage) {
    val align = if (msg.isUser) Alignment.End else Alignment.Start
    val bg = if (msg.isUser) Color(0xFF2563EB) else Color.White
    val borderStroke = if (msg.isUser) null else BorderStroke(1.dp, Color(0xFFE2E8F0))
    val textColor = if (msg.isUser) Color.White else Color(0xFF0F172A)
    val cornerRadius = if (msg.isUser) {
        RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
    } else {
        RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = align
    ) {
        Text(
            text = msg.name.uppercase(),
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
        Surface(
            shape = cornerRadius,
            color = bg,
            border = borderStroke,
            shadowElevation = 1.dp,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = msg.text,
                color = textColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(14.dp),
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun ThinkingBubble() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "PAK REI",
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
        Surface(
            shape = RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            shadowElevation = 1.dp
        ) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(color = Color(0xFF4F46E5), modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Pak Rei sedang menulis...", fontSize = 12.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
            }
        }
    }
}
