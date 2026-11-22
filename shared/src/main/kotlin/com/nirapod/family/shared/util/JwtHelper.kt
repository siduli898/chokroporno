package com.nirapod.family.shared.util

import android.util.Base64
import org.json.JSONObject

object JwtHelper {
    fun createToken(payload: Map<String, Any>): String {
        val header = JSONObject(mapOf("alg" to "HS256", "typ" to "JWT")).toString()
        val body = JSONObject(payload).toString()
        val encHeader = Base64.encodeToString(header.toByteArray(), Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
        val encBody = Base64.encodeToString(body.toByteArray(), Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
        return "$encHeader.$encBody.mock_sig"
    }

    fun parseToken(token: String): Map<String, Any> {
        val parts = token.split(".")
        if (parts.size >= 2) {
            val bodyJson = String(Base64.decode(parts[1], Base64.URL_SAFE))
            val json = JSONObject(bodyJson)
            val map = mutableMapOf<String, Any>()
            json.keys().forEach { key ->
                map[key] = json.get(key)
            }
            return map
        }
        return emptyMap()
    }
}
