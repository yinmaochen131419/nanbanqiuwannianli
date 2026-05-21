package com.example.wannianli.data.remote

import com.example.wannianli.data.model.SolarTermInfo
import retrofit2.http.GET
import retrofit2.http.Query

data class DeepSeekRequest(
    val model: String = "deepseek-chat",
    val messages: List<DeepSeekMessage>,
    val temperature: Double = 0.0,
    val max_tokens: Int = 4096
)

data class DeepSeekMessage(
    val role: String,
    val content: String
)

data class DeepSeekResponse(
    val id: String?,
    val choices: List<DeepSeekChoice>?
)

data class DeepSeekChoice(
    val message: DeepSeekMessage?
)

interface DeepSeekApiService {
    companion object {
        const val BASE_URL = "https://api.deepseek.com/"
    }
}