package com.example.data

import kotlinx.coroutines.flow.Flow

class TryoutRepository(private val tryoutHistoryDao: TryoutHistoryDao) {
    val allHistory: Flow<List<TryoutHistoryItem>> = tryoutHistoryDao.getAllHistory()

    suspend fun insertHistory(item: TryoutHistoryItem) {
        tryoutHistoryDao.insertHistory(item)
    }

    suspend fun clearAllHistory() {
        tryoutHistoryDao.clearAllHistory()
    }

    // Call Gemini API to generate questions
    suspend fun fetchQuestions(
        prompt: String,
        systemInstruction: String,
        responseMimeType: String = "application/json"
    ): String {
        return GeminiService.generateContent(
            prompt = prompt,
            systemInstruction = systemInstruction,
            responseMimeType = responseMimeType
        )
    }

    // Call Gemini API to generate image
    suspend fun fetchImage(prompt: String): String? {
        return GeminiService.generateImage(prompt)
    }
}
