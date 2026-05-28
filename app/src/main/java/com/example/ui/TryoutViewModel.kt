package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.util.PdfParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

enum class ScreenStatus {
    LANDING, SETUP, LOADING, EXAM, RESULT, HISTORY, REPORT, CHAT
}

enum class TryoutMode {
    REGULER, US, MEGA, ADAPTIVE
}

data class UserProfile(
    val name: String = "",
    val age: String = "",
    val kelas: String = "",
    val sekolah: String = "",
    val photoUri: String? = null,
    val subjectName: String = "",
    val difficultyCode: String = "Normal",
    val difficultyText: String = "Sesuai Kurikulum",
    val pdfFileUri: String? = null,
    val pdfExtractedText: String = "",
    val indicators: String = "",
    val bloomLevel: String = "",
    val generateImages: Boolean = true,
    val nisn: String = ""
)

data class FinalResultPayload(
    val score: Int = 0,
    val correctPts: Int = 0,
    val wrongPts: Int = 0,
    val timeUsed: Int = 0,
    val mistakes: List<String> = emptyList(),
    val comment: String = "Selesai Evaluasi.",
    val summaries: List<TopicSummary> = emptyList()
)

data class TopicSummary(
    val topic: String,
    val explanation: String
)

data class ChatMessage(
    val isUser: Boolean,
    val text: String,
    val name: String = "Siswa"
)

data class TryoutUiState(
    val status: ScreenStatus = ScreenStatus.LANDING,
    val mode: TryoutMode = TryoutMode.REGULER,
    val isReviewMode: Boolean = false,
    val totalTarget: Int = 30,
    val currentIdx: Int = 0,
    val timerSeconds: Int = 4500,
    val userProfile: UserProfile = UserProfile(),
    val questions: List<Question> = emptyList(),
    val answers: Map<Int, Any> = emptyMap(), // index to Answer
    val finalResult: FinalResultPayload? = null,
    val history: List<TryoutHistoryItem> = emptyList(),
    val totalXP: Int = 0,
    val averageScore: Int = 0,
    val userRankTitle: String = "Elite Bronze",
    val userRankBadge: String = "BRONZE",
    val nextRankXP: Int = 1000,
    val prevRankXP: Int = 0,
    val xpProgress: Float = 0f,
    val chatHistory: List<ChatMessage> = emptyList(),
    val isChatThinking: Boolean = false,
    val loadingText: String = "Sistem Sedang Menyiapkan...",
    val backgroundLoading: Boolean = false
)

class TryoutViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "TryoutViewModel"
    private val sharedPrefs = application.getSharedPreferences("ai_smart_tryout_prefs", Context.MODE_PRIVATE)
    private val repository: TryoutRepository

    private val _uiState = MutableStateFlow(TryoutUiState())
    val uiState: StateFlow<TryoutUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var isFetchingBatch: Boolean = false

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TryoutRepository(database.tryoutHistoryDao())

        // Load profile
        loadProfileFromPreferences()

        // Sync local database history
        viewModelScope.launch {
            repository.allHistory.collect { list ->
                _uiState.update { state ->
                    val totalXP = list.sumOf { it.score * 10 }
                    val avgScore = if (list.isNotEmpty()) list.map { it.score }.average().toInt() else 0
                    
                    // Rank stats
                    val (badge, title, minXP, maxXP) = calculateRankConfig(totalXP)
                    val range = maxXP - minXP
                    val progress = if (range > 0) (totalXP - minXP).toFloat() / range else 1.0f

                    state.copy(
                        history = list,
                        totalXP = totalXP,
                        averageScore = avgScore,
                        userRankBadge = badge,
                        userRankTitle = title,
                        prevRankXP = minXP,
                        nextRankXP = maxXP,
                        xpProgress = progress.coerceIn(0f, 1f)
                    )
                }
            }
        }
    }

    private fun loadProfileFromPreferences() {
        val name = sharedPrefs.getString("name", "Fahri") ?: "Fahri"
        val age = sharedPrefs.getString("age", "15") ?: "15"
        val kelas = sharedPrefs.getString("kelas", "IX B") ?: "IX B"
        val sekolah = sharedPrefs.getString("sekolah", "SMPN 2 SIMPANG KATIS") ?: "SMPN 2 SIMPANG KATIS"
        val photo = sharedPrefs.getString("photo", null)
        val generateImages = sharedPrefs.getBoolean("generate_images", true)
        val nisn = sharedPrefs.getString("nisn", "01202020") ?: "01202020"

        _uiState.update {
            it.copy(
                userProfile = UserProfile(
                    name = name,
                    age = age,
                    kelas = kelas,
                    sekolah = sekolah,
                    photoUri = photo,
                    generateImages = generateImages,
                    nisn = nisn
                )
            )
        }
    }

    fun saveProfileToPreferences(profile: UserProfile) {
        sharedPrefs.edit().apply {
            putString("name", profile.name)
            putString("age", profile.age)
            putString("kelas", profile.kelas)
            putString("sekolah", profile.sekolah)
            putString("photo", profile.photoUri)
            putBoolean("generate_images", profile.generateImages)
            putString("nisn", profile.nisn)
            apply()
        }
        _uiState.update { it.copy(userProfile = profile) }
    }

    fun setScreenStatus(status: ScreenStatus) {
        if (status != ScreenStatus.EXAM) {
            _uiState.update { it.copy(isReviewMode = false) }
        }
        _uiState.update { it.copy(status = status) }
    }

    fun setTryoutMode(mode: TryoutMode) {
        val total = when (mode) {
            TryoutMode.US -> 40
            TryoutMode.ADAPTIVE -> 10
            else -> 30
        }
        _uiState.update {
            it.copy(
                mode = mode,
                status = ScreenStatus.SETUP,
                totalTarget = total,
                answers = emptyMap(),
                questions = emptyList(),
                currentIdx = 0,
                finalResult = null
            )
        }
    }

    fun updateProfileBuilder(updater: (UserProfile) -> UserProfile) {
        _uiState.update {
            val updated = updater(it.userProfile)
            it.copy(userProfile = updated)
        }
    }

    fun handlePdfSelection(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            val parsedText = PdfParser.parsePdfText(context, uri)
            _uiState.update {
                it.copy(
                    userProfile = it.userProfile.copy(
                        pdfFileUri = uri.toString(),
                        pdfExtractedText = parsedText
                    )
                )
            }
        }
    }

    fun setAnswer(index: Int, answer: Any) {
        if (_uiState.value.isReviewMode) return
        _uiState.update {
            val updatedAnswers = it.answers.toMutableMap()
            updatedAnswers[index] = answer
            it.copy(answers = updatedAnswers)
        }
    }

    fun changeQuestion(offset: Int) {
        val state = _uiState.value
        val nextIdx = state.currentIdx + offset
        if (nextIdx in 0 until state.totalTarget) {
            _uiState.update { it.copy(currentIdx = nextIdx) }
            
            // Background fetch when user gets close to limits
            if (!state.isReviewMode && nextIdx >= state.questions.size - 3 && state.questions.size < state.totalTarget) {
                triggerBackgroundFetch()
            }
        } else if (nextIdx == state.totalTarget) {
            if (state.isReviewMode) {
                setScreenStatus(ScreenStatus.RESULT)
            } else {
                submitExam()
            }
        }
    }

    fun submitExam() {
        stopTimer()
        val state = _uiState.value
        viewModelScope.launch(Dispatchers.Default) {
            var totalMaxPoints = 0
            var userPoints = 0
            val mistakes = mutableListOf<String>()

            for (i in 0 until state.totalTarget) {
                val q = state.questions.getOrNull(i)
                val ans = state.answers[i]
                if (q == null) {
                    totalMaxPoints += 1
                    mistakes.add("Materi Kosong")
                    continue
                }

                when (q.type) {
                    "pilgan" -> {
                        totalMaxPoints += 1
                        if (ans is Int && ans == q.pilganAnswer) userPoints += 1
                        else mistakes.add(q.topic)
                    }
                    "kompleks" -> {
                        totalMaxPoints += 2
                        if (ans is List<*> && ans.toSet() == q.kompleksAnswer.toSet()) userPoints += 2
                        else mistakes.add(q.topic)
                    }
                    "benarsalah" -> {
                        val stmtCount = q.statements.size.coerceAtLeast(1)
                        totalMaxPoints += stmtCount
                        var allC = true
                        for (j in 0 until stmtCount) {
                            if (ans is List<*> && j < ans.size && ans[j] == q.benarSalahAnswer.getOrNull(j)) userPoints += 1
                            else allC = false
                        }
                        if (!allC) mistakes.add(q.topic)
                    }
                    "menjodohkan" -> {
                        val pairCount = q.menjodohkanPairs.size
                        totalMaxPoints += pairCount
                        var allC = true
                        for (j in 0 until pairCount) {
                            if (ans is Map<*, *> && ans[j] == q.menjodohkanPairs.getOrNull(j)?.right) userPoints += 1
                            else allC = false
                        }
                        if (!allC) mistakes.add(q.topic)
                    }
                }
            }

            val rawScore = if (totalMaxPoints > 0) (userPoints.toFloat() / totalMaxPoints * 100).toInt() else 0
            val elapsedSecs = (state.userProfile.difficultyCode.let { if (state.mode == TryoutMode.ADAPTIVE) 20 else 75 } * 60) - state.timerSeconds
            val elapsedMins = (elapsedSecs / 60).coerceAtLeast(1)

            val uniqueMistakes = mistakes.distinct().take(3)
            val resultPayload = FinalResultPayload(
                score = rawScore.coerceIn(0, 100),
                correctPts = userPoints,
                wrongPts = totalMaxPoints - userPoints,
                timeUsed = elapsedMins,
                mistakes = uniqueMistakes,
                comment = "Memproses analisis tutor kognitif...",
                summaries = emptyList()
            )

            _uiState.update {
                it.copy(
                    status = ScreenStatus.RESULT,
                    finalResult = resultPayload
                )
            }

            // Save to Local Database
            val historyItem = TryoutHistoryItem(
                mode = state.mode.name,
                subject = state.userProfile.subjectName,
                difficulty = state.userProfile.difficultyCode,
                score = rawScore.coerceIn(0, 100),
                correctPts = userPoints,
                wrongPts = totalMaxPoints - userPoints,
                mistakesJson = JSONArray(uniqueMistakes).toString(),
                timeUsed = elapsedMins,
                timestamp = System.currentTimeMillis()
            )
            repository.insertHistory(historyItem)

            // Fetch Commentary Evaluation from Gemini
            fetchGeminiEvaluation(rawScore, uniqueMistakes)
        }
    }

    private fun fetchGeminiEvaluation(score: Int, mistakes: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            val commentPrompt = """
                Evaluasi absolut. Nama: ${_uiState.value.userProfile.name} | Skor: $score/100. Topik lemah: ${mistakes.joinToString(", ").ifEmpty { "Sempurna." }}
                Tugas: Beri komentar mendidik ttg skor (maksimal 2 kalimat saja, ceria, asyik dan gunakan gaya Pak Rei). Beri maks 2 diagnosa konsep. 
                Kembalikan persis dalam format JSON murni: {"comment": "...", "summaries": [{"topic": "...", "explanation": "..."}]}
            """.trimIndent()

            val systemPrompt = """
                Anda adalah "Pak Rei", AI Evaluator Pendidikan tingkat tinggi.
                PRINSIP WAJIB:
                1. SINTESIS TEKS BARU: DILARANG menjiplak mentah-mentah referensi.
                2. FORMAT KETAT: JSON murni wajib ditaati. Jangan memotong struktur JSON.
            """.trimIndent()

            try {
                val jsonString = repository.fetchQuestions(commentPrompt, systemPrompt)
                if (jsonString.isNotEmpty()) {
                    val root = JSONObject(jsonString)
                    val comment = root.optString("comment", "Evaluasi selesai.")
                    val summaries = mutableListOf<TopicSummary>()
                    val sumArr = root.optJSONArray("summaries")
                    if (sumArr != null) {
                        for (i in 0 until sumArr.length()) {
                            val obj = sumArr.getJSONObject(i)
                            summaries.add(TopicSummary(obj.optString("topic", ""), obj.optString("explanation", "")))
                        }
                    }
                    _uiState.update { state ->
                        state.copy(
                            finalResult = state.finalResult?.copy(
                                comment = comment,
                                summaries = summaries
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed parsing evaluation commentary: ${e.message}")
            }
        }
    }

    fun startAdaptiveExam() {
        val state = _uiState.value
        val history = state.history
        if (history.isEmpty()) return

        // Extract top mistakes
        val weaknesses = mutableMapOf<String, Int>()
        history.forEach { item ->
            try {
                val arr = JSONArray(item.mistakesJson)
                for (i in 0 until arr.length()) {
                    val topic = arr.getString(i)
                    weaknesses[topic] = (weaknesses[topic] ?: 0) + 1
                }
            } catch (e: Exception) {}
        }
        val topWeaknesses = weaknesses.entries.sortedByDescending { it.value }.map { it.key }.take(3)
        if (topWeaknesses.isEmpty()) return

        setTryoutMode(TryoutMode.ADAPTIVE)
        _uiState.update {
            it.copy(
                userProfile = it.userProfile.copy(
                    subjectName = "Adaptif: ${topWeaknesses.joinToString(", ")}",
                    difficultyCode = "Penebusan AI",
                    difficultyText = "Sesuai Memori Kesalahan"
                )
            )
        }
        startAiPreparation()
    }

    fun startAiPreparation() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(
                    status = ScreenStatus.LOADING,
                    loadingText = "Pak Rei sedang merancang matriks kognitif..."
                )
            }

            val amount = 10.coerceAtMost(_uiState.value.totalTarget)
            val batch = fetchQuestionsBatch(1, amount)

            if (batch.isEmpty()) {
                _uiState.update { it.copy(status = ScreenStatus.SETUP) }
                return@launch
            }

            // Prefetch first image if needed
            val firstQ = batch.firstOrNull()
            if (firstQ != null && _uiState.value.userProfile.generateImages && !firstQ.imagePrompt.isNullOrBlank()) {
                _uiState.update { it.copy(loadingText = "Menggambar stimulus visual...") }
                val img = repository.fetchImage(firstQ.imagePrompt)
                if (img != null) {
                    firstQ.cachedImageBase64 = img
                }
            }

            _uiState.update {
                it.copy(
                    questions = batch,
                    status = ScreenStatus.EXAM,
                    answers = emptyMap(),
                    currentIdx = 0,
                    timerSeconds = (_uiState.value.userProfile.difficultyCode.let { d -> if (it.mode == TryoutMode.ADAPTIVE) 20 else 75 }) * 60
                )
            }
            startTimer()
        }
    }

    private suspend fun fetchQuestionsBatch(batchNum: Int, amount: Int): List<Question> {
        val state = _uiState.value
        val u = state.userProfile

        val imgInstruct = if (u.generateImages) {
            "PENTING (GAMBAR SEBAGAI STIMULUS): JANGAN buat gambar pemanis belaka. Isi \"imagePrompt\" HANYA untuk menciptakan stimulus analisis (contoh prompt: \"A detailed scientific diagram showing cell respiration...\", \"A line graph showing currency fluctuation...\"). Jika tidak perlu gambar, kosongkan \"\"."
        } else {
            "PENTING: DILARANG MENGGUNAKAN GAMBAR. Field \"imagePrompt\" WAJIB diisi string kosong \"\"."
        }

        val jsonSchema = """
            {
              "questions": [
                { "type": "pilgan", "text": "...", "imagePrompt": "...", "options": ["A", "B", "C", "D"], "answer": 0, "topic": "...", "explanation": "..." },
                { "type": "kompleks", "text": "...", "imagePrompt": "", "options": ["A", "B", "C", "D"], "answer": [0, 2], "topic": "...", "explanation": "..." },
                { "type": "benarsalah", "text": "...", "imagePrompt": "", "statements": ["1", "2", "3", "4"], "answer": [true, false, true, false], "topic": "...", "explanation": "..." },
                { "type": "menjodohkan", "text": "...", "imagePrompt": "", "pairs": [{"left": "A", "right": "B"}], "topic": "...", "explanation": "..." }
              ]
            }
        """.trimIndent()

        val prompt = when (state.mode) {
            TryoutMode.ADAPTIVE -> """
                Rancang TEPAT $amount blok soal ACAK untuk MODE PENEBUSAN (Remedial). 
                Fokus utama pada kelemahan sirkular siswa: "${u.subjectName}" (Kelas ${u.kelas}).
                Tingkat kesulitan harus memadai, menyentuh dasar penguasaan sebelum melaju ke HOTS.
                $imgInstruct
            """.trimIndent()
            TryoutMode.US -> """
                Rancang TEPAT $amount blok soal ACAK untuk UJIAN SEKOLAH AKM nasional. Mapel: "${u.subjectName}", Kelas ${u.kelas}.
                Indikator: "${u.indicators}". Level Kognitif Bloom: "${u.bloomLevel}".
                Kombinasikan tipe "pilgan", "kompleks", "benarsalah", "menjodohkan".
                ${if (u.pdfExtractedText.isNotEmpty()) "REFERENSI MATERI: ${u.pdfExtractedText}\n" else ""}$imgInstruct
            """.trimIndent()
            TryoutMode.MEGA -> """
                Rancang TEPAT $amount blok soal ACAK tingkat ${u.difficultyCode}. Mapel: "${u.subjectName}", Kelas ${u.kelas}.
                WAJIB menggunakan referensi silabus kurikulum berikut:
                \"\"\"${u.pdfExtractedText.take(3000)}\"\"\"
                ${if (u.difficultyCode == "TKA") "Fokus pada problem solving tingkat TKA Nasional." else ""}
                $imgInstruct
            """.trimIndent()
            else -> """
                Rancang TEPAT $amount blok soal ACAK. Mapel: "${u.subjectName}", Kelas ${u.kelas}. Tingkat kesulitan: ${u.difficultyText}.
                $imgInstruct
            """.trimIndent()
        } + "\n\nKEMBALIKAN OUTPUT HARUS SELALU BERUPA JSON SESUAI SCHEMA DI ATAS. JANGAN MENAMBAHKAN MARKDOWN ATAPUN TEKS MULTIPART LAINNYA. HANYA JSON MURNI."

        val systemPrompt = """
            Anda adalah "Pak Rei", AI Evaluator Pendidikan tingkat tinggi.
            PRINSIP WAJIB:
            1. SINTESIS TEKS BARU: DILARANG menjiplak mentah-mentah referensi.
            2. FORMAT KETAT: JSON murni wajib ditaati. Jangan memotong struktur JSON.
        """.trimIndent()

        try {
            val responseText = repository.fetchQuestions(prompt, systemPrompt)
            if (responseText.isNotEmpty()) {
                val cleanJson = responseText
                    .replace("```json", "")
                    .replace("```", "")
                    .trim()
                val root = JSONObject(cleanJson)
                val qArr = root.optJSONArray("questions") ?: JSONArray()
                val parsedList = mutableListOf<Question>()
                for (i in 0 until qArr.length()) {
                    parsedList.add(QuestionParser.parseQuestionFromJsonObject(qArr.getJSONObject(i)))
                }
                return parsedList
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching questions: ${e.message}", e)
        }
        return emptyList()
    }

    private fun triggerBackgroundFetch() {
        if (isFetchingBatch || _uiState.value.questions.size >= _uiState.value.totalTarget) return
        isFetchingBatch = true
        _uiState.update { it.copy(backgroundLoading = true) }

        viewModelScope.launch(Dispatchers.IO) {
            var tries = 0
            while (_uiState.value.questions.size < _uiState.value.totalTarget && tries < 3) {
                val needed = _uiState.value.totalTarget - _uiState.value.questions.size
                val amount = 10.coerceAtMost(needed)
                val batch = fetchQuestionsBatch(2, amount)
                if (batch.isNotEmpty()) {
                    _uiState.update { state ->
                        state.copy(questions = state.questions + batch)
                    }
                } else {
                    break
                }
                tries++
            }
            isFetchingBatch = false
            _uiState.update { it.copy(backgroundLoading = false) }
        }
    }

    fun startImageGenerationForIndex(index: Int) {
        val state = _uiState.value
        val q = state.questions.getOrNull(index) ?: return
        if (q.cachedImageBase64 != null || q.imagePrompt.isNullOrBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            val base64 = repository.fetchImage(q.imagePrompt)
            if (base64 != null) {
                _uiState.update { ui ->
                    val updatedList = ui.questions.toMutableList()
                    val targetQ = updatedList.getOrNull(index)
                    if (targetQ != null) {
                        updatedList[index] = targetQ.copy(cachedImageBase64 = base64)
                    }
                    ui.copy(questions = updatedList)
                }
            }
        }
    }

    // Timer logic
    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                var expired = false
                _uiState.update { state ->
                    if (state.timerSeconds > 0) {
                        state.copy(timerSeconds = state.timerSeconds - 1)
                    } else {
                        expired = true
                        state
                    }
                }
                if (expired) {
                    submitExam()
                    break
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    fun startReview() {
        _uiState.update {
            it.copy(
                isReviewMode = true,
                currentIdx = 0,
                status = ScreenStatus.EXAM
            )
        }
    }

    fun exitReview() {
        _uiState.update {
            it.copy(
                isReviewMode = false,
                status = ScreenStatus.RESULT
            )
        }
    }

    fun restartAll() {
        stopTimer()
        _uiState.update {
            it.copy(
                status = ScreenStatus.LANDING,
                questions = emptyList(),
                answers = emptyMap(),
                currentIdx = 0,
                finalResult = null,
                isReviewMode = false,
                chatHistory = emptyList()
            )
        }
    }

    fun openChatConversation(contextual: Boolean) {
        val state = _uiState.value
        val u = state.userProfile
        val res = state.finalResult

        val introMsg = if (contextual && res != null) {
            "Halo ${u.name}! Pak Rei melihat kamu baru saja menyelesaikan tryout dengan skor ${res.score}/100. Mari bahas materi ${res.mistakes.joinToString(", ").ifEmpty { "dasar" }} yang kamu rasa butuh bimbingan!"
        } else {
            "Halo ${u.name}! Ada materi ujian atau konsep sains yang membuatmu bingung hari ini? Tanyakan saja, Pak Rei siap membantumu!"
        }

        _uiState.update {
            it.copy(
                status = ScreenStatus.CHAT,
                chatHistory = listOf(ChatMessage(isUser = false, text = introMsg, name = "Pak Rei"))
            )
        }
    }

    fun sendChatMessage(text: String) {
        val message = text.trim()
        if (message.isEmpty() || _uiState.value.isChatThinking) return

        _uiState.update {
            it.copy(
                chatHistory = it.chatHistory + ChatMessage(isUser = true, text = message, name = it.userProfile.name),
                isChatThinking = true
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            val state = _uiState.value
            val sysPrompt = "Anda adalah \"Pak Rei\", guru virtual Secondary School terlatih, penuh antusiasme, asyik, interaktif, dan akrab. Menjawab terstruktur, sabar, menggunakan bahasa Indonesia sederhana, sapaan aku-kamu. Ingat, jawab dengan ramah."
            
            val jsonHistory = mutableListOf<JSONObject>()
            // We feed the last 10 turns for context safety
            state.chatHistory.takeLast(10).forEach { msg ->
                val obj = JSONObject()
                obj.put("role", if (msg.isUser) "user" else "model")
                val partsArr = JSONArray()
                val partObj = JSONObject()
                partObj.put("text", msg.text)
                partsArr.put(partObj)
                obj.put("parts", partsArr)
                jsonHistory.add(obj)
            }

            // Append newest message to history
            val newObj = JSONObject()
            newObj.put("role", "user")
            val partsArr = JSONArray()
            val partObj = JSONObject()
            partObj.put("text", message)
            partsArr.put(partObj)
            newObj.put("parts", partsArr)
            jsonHistory.add(newObj)

            val reply = GeminiService.chatConversation(jsonHistory, sysPrompt)
            _uiState.update {
                it.copy(
                    chatHistory = it.chatHistory + ChatMessage(isUser = false, text = reply.ifEmpty { "Maaf, koneksi ke sinyal Pak Rei terputus. Coba kirim ulang pesanmu ya!" }, name = "Pak Rei"),
                    isChatThinking = false
                )
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllHistory()
        }
    }

    private fun calculateRankConfig(xp: Int): RankConfig {
        return when {
            xp >= 10000 -> RankConfig("MYTHIC", "Mythic Legend", 10000, 10000)
            xp >= 6000 -> RankConfig("PLATINUM", "Epic Platinum", 6000, 10000)
            xp >= 3000 -> RankConfig("GOLD", "Grandmaster Gold", 3000, 6000)
            xp >= 1000 -> RankConfig("SILVER", "Master Silver", 1000, 3000)
            else -> RankConfig("BRONZE", "Elite Bronze", 0, 1000)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}

data class RankConfig(
    val badge: String,
    val title: String,
    val minXP: Int,
    val maxXP: Int
)
