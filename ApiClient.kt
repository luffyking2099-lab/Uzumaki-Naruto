package com.family.guardian.network

import android.content.Context
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface GuardianApi {
    @POST("usage")
    suspend fun reportUsage(
        childId: String,
        totalMinutesToday: Int,
        perAppMinutes: Map<String, Long>
    )

    @POST("location")
    suspend fun reportLocation(
        childId: String,
        lat: Double,
        lng: Double,
        accuracyM: Float
    )
}

object ApiClient {
    // Point this at your own backend once it exists; this is a placeholder.
    private const val BASE_URL = "https://api.your-family-dashboard.example/"

    val service: GuardianApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GuardianApi::class.java)
    }

    // The child device's paired ID, set during pairing in MainActivity and stored locally.
    fun childId(context: Context): String =
        context.getSharedPreferences("guardian", Context.MODE_PRIVATE)
            .getString("child_id", "unpaired") ?: "unpaired"
}
