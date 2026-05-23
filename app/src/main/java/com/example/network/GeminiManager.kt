package com.example.network

import android.util.Log
import com.example.BuildConfig
import com.example.data.MediaAsset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiManager {
    private const val TAG = "GeminiManager"
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Calls Gemini 3.5 Flash directly via REST to suggest highly customized tags
     * based on camera metadata and filename profiles.
     */
    suspend fun suggestTags(asset: MediaAsset): List<String> = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey.contains("PLACEHOLDER")) {
            Log.w(TAG, "Gemini API Key missing or placeholder. Returning smart fallback tags.")
            return@withContext getSmartFallbackTags(asset)
        }

        val prompt = """
            Actúa como un catalogador fotográfico profesional. Te proporcionaré los datos técnicos (EXIF) y el nombre de un archivo RAW/video. Devuelve únicamente entre 4 y 6 etiquetas (tags) en español separadas por comas que describan el tipo de escena, la estética, la iluminación y el tema adecuado para la toma. No añadas explicaciones, títulos, markdown ni viñetas. Solo una línea con las palabras clave separadas por comas.
            
            Archivo: ${asset.fileName}
            Extensión: ${asset.fileExtension} (Soporte RAW)
            Ubicación: ${asset.locationName}
            Cámara: ${asset.cameraModel}
            Apertura: ${asset.exifAperture}
            Velocidad: ${asset.exifShutter}
            ISO: ${asset.exifIso}
        """.trimIndent()

        val jsonRequest = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
        }

        val requestBody = jsonRequest.toString().toRequestBody("application/json".toMediaType())
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .header("Content-Type", "application/json")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Gemini REST API Call unsuccessful: Code ${response.code}. Body: ${response.body?.string()}")
                    return@withContext getSmartFallbackTags(asset)
                }

                val responseStr = response.body?.string() ?: ""
                val responseJson = JSONObject(responseStr)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val contentObj = firstCandidate.optJSONObject("content")
                    val parts = contentObj?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val textResult = parts.getJSONObject(0).optString("text", "")
                        if (textResult.isNotEmpty()) {
                            val parsedList = textResult.split(",")
                                .map { it.replace("\n", "").replace("\r", "").trim() }
                                .filter { it.isNotEmpty() }
                            if (parsedList.isNotEmpty()) {
                                return@withContext parsedList
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching from Gemini API: ${e.message}", e)
        }

        return@withContext getSmartFallbackTags(asset)
    }

    /**
     * Graceful smart suggestions if Gemini API is not yet activated in the panel
     */
    private fun getSmartFallbackTags(asset: MediaAsset): List<String> {
        val list = mutableListOf<String>()
        val nameLower = asset.fileName.lowercase()
        val locLower = asset.locationName.lowercase()
        
        list.add("fotografía-profesional")
        list.add(if (asset.fileMimeType().contains("video")) "metraje-video" else "captura-raw")

        if (nameLower.contains("sunset") || locLower.contains("uyuni") || nameLower.contains("amanecer")) {
            list.addAll(listOf("atardecer", "paisaje", "reflejo-salino", "dorado"))
        } else if (nameLower.contains("volcano") || nameLower.contains("andes") || locLower.contains("ecuador")) {
            list.addAll(listOf("volcan", "montaña", "cordillera-andes", "altitud"))
        } else if (nameLower.contains("tokyo") || nameLower.contains("neon") || locLower.contains("japan")) {
            list.addAll(listOf("tokyo-cyberpunk", "noches-neon", "lluvia", "luces-shibuya"))
        } else if (nameLower.contains("iceland") || nameLower.contains("waterfall") || nameLower.contains("aerial")) {
            list.addAll(listOf("vista-aerea", "cascada", "islandia", "cinematico-drone"))
        } else if (nameLower.contains("patagonia") || nameLower.contains("peaks") || locLower.contains("chile")) {
            list.addAll(listOf("patagonia-chile", "trekking", "glaciar", "andes-sur"))
        } else {
            list.addAll(listOf("naturaleza-alta", "exif-detalles", "catalogo-estudio"))
        }
        return list.take(6)
    }

    private fun MediaAsset.fileMimeType(): String {
        return when (fileExtension.lowercase()) {
            "mp4" -> "video/mp4"
            "mkv" -> "video/x-matroska"
            "mov" -> "video/quicktime"
            "jpg", "jpeg" -> "image/jpeg"
            else -> "image/x-raw"
        }
    }
}
