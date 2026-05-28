package com.example.data

import org.json.JSONObject

data class Question(
    val type: String, // "pilgan", "kompleks", "benarsalah", "menjodohkan"
    val text: String,
    val imagePrompt: String? = "",
    val options: List<String> = emptyList(),
    val pilganAnswer: Int = 0,
    val kompleksAnswer: List<Int> = emptyList(),
    val benarSalahAnswer: List<Boolean> = emptyList(),
    val menjodohkanPairs: List<PairItem> = emptyList(),
    val topic: String = "Materi Umum",
    val explanation: String = "",
    var cachedImageBase64: String? = null,
    var statements: List<String> = emptyList(),
    var shuffledRights: List<String> = emptyList() // helper for UI
)

data class PairItem(
    val left: String,
    val right: String
)

object QuestionParser {
    fun parseQuestionFromJsonObject(obj: JSONObject): Question {
        val type = obj.optString("type", "pilgan")
        val text = obj.optString("text", "")
        val imagePrompt = obj.optString("imagePrompt", "")
        val topic = obj.optString("topic", "Materi Umum")
        val explanation = obj.optString("explanation", "")

        val options = mutableListOf<String>()
        val optionsArr = obj.optJSONArray("options")
        if (optionsArr != null) {
            for (i in 0 until optionsArr.length()) {
                options.add(optionsArr.getString(i))
            }
        }

        var pilganAns = 0
        val kompleksAns = mutableListOf<Int>()
        val benarSalahAns = mutableListOf<Boolean>()
        val pairsList = mutableListOf<PairItem>()

        when (type) {
            "pilgan" -> {
                pilganAns = obj.optInt("answer", 0)
            }
            "kompleks" -> {
                val ansArr = obj.optJSONArray("answer")
                if (ansArr != null) {
                    for (i in 0 until ansArr.length()) {
                        kompleksAns.add(ansArr.getInt(i))
                    }
                }
            }
            "benarsalah" -> {
                val ansArr = obj.optJSONArray("answer")
                if (ansArr != null) {
                    for (i in 0 until ansArr.length()) {
                        benarSalahAns.add(ansArr.getBoolean(i))
                    }
                }
            }
            "menjodohkan" -> {
                val pairsArr = obj.optJSONArray("pairs")
                if (pairsArr != null) {
                    for (i in 0 until pairsArr.length()) {
                        val pObj = pairsArr.getJSONObject(i)
                        pairsList.add(PairItem(pObj.optString("left", ""), pObj.optString("right", "")))
                    }
                }
            }
        }

        val statements = mutableListOf<String>()
        val statementsArr = obj.optJSONArray("statements")
        if (statementsArr != null) {
            for (i in 0 until statementsArr.length()) {
                statements.add(statementsArr.getString(i))
            }
        }

        return Question(
            type = type,
            text = text,
            imagePrompt = imagePrompt,
            options = options,
            pilganAnswer = pilganAns,
            kompleksAnswer = kompleksAns,
            benarSalahAnswer = benarSalahAns,
            menjodohkanPairs = pairsList,
            topic = topic,
            explanation = explanation,
            statements = statements,
            shuffledRights = pairsList.map { it.right }.shuffled()
        )
    }
}
