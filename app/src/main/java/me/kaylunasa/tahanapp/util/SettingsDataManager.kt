package me.kaylunasa.tahanapp.util

import android.content.Context
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader

object SettingsDataManager {
    private val TAG: String = SettingsDataManager::class.java.simpleName
    private const val STORAGE_FILE = "app_settings.json"

    private const val KEY_LANGUAGE = "language"
    private const val KEY_NARRATION = "forceNarration"

    @JvmStatic
    fun dumpSettings(context: Context) {
        Log.d(TAG, "dumpSettings: Dumping settings data")
        Log.d(TAG, "dumpSettings: " + getSettings(context))
    }

    @JvmStatic
    fun getLanguage(context: Context): String? {
        return try {
            getSettingKey<String?>(context, KEY_LANGUAGE)
        } catch (e: JSONException) {
            Log.e(TAG, "getLanguage: Could not properly fetch language", e)
            null
        }
    }

    @JvmStatic
    @Throws(JSONException::class)
    fun putLanguage(context: Context, language: String?) {
        putSettingKey(context, KEY_LANGUAGE, language)
    }

    @JvmStatic
    fun getForceNarrations(context: Context): Boolean {
        return try {
            return getSettingKey<Boolean>(context, KEY_NARRATION) ?: false
        } catch (e: JSONException) {
            Log.e(TAG, "getForceNarrations: Could not properly fetch forced narration display", e)
            false
        }
    }

    @JvmStatic
    @Throws(JSONException::class)
    fun putForceNarrations(context: Context, narrations: Boolean) {
        putSettingKey(context, KEY_NARRATION, narrations)
    }

    @Throws(JSONException::class)
    private inline fun <reified T> getSettingKey(context: Context, key: String): T? {
        val jsonObject = getSettings(context)
        return if (jsonObject.has(key) &&
            !jsonObject.isNull(key) &&
            jsonObject.get(key) is T
        ) jsonObject.get(key) as T?
        else null
    }

    @Throws(JSONException::class)
    private fun <T> putSettingKey(context: Context, key: String, value: T?) {
        val jsonObject = getSettings(context)
        if (value == null)
            jsonObject.remove(key)
        else
            jsonObject.put(key, value)
        putSettings(context, jsonObject)
    }

    @JvmStatic
    @Throws(IOException::class)
    fun resetSettings(context: Context) {
        context.openFileOutput(STORAGE_FILE, Context.MODE_PRIVATE).use { fos ->
            val jsonObject = JSONObject()
            fos.write(jsonObject.toString().toByteArray())
        }
    }

    private fun getSettings(context: Context): JSONObject {
        try {
            BufferedReader(InputStreamReader(context.openFileInput(STORAGE_FILE))).use { reader ->
                val sb = StringBuilder()
                var input: String?
                while ((reader.readLine().also { input = it }) != null) sb.append(input)
                check(!sb.toString().isEmpty())

                val jsonObject = JSONObject(sb.toString())

                Log.d(TAG, "getSettings: Loaded settings from file storage")
                return jsonObject
            }
        } catch (e : Exception) {
            if (e !is IllegalStateException && e !is FileNotFoundException) throw e
            try {
                context.openFileOutput(STORAGE_FILE, Context.MODE_PRIVATE).use { fos ->
                    val jsonObject = JSONObject()
                    fos.write(jsonObject.toString().toByteArray())
                    Log.d(TAG, "getSettings: Created file $STORAGE_FILE")
                }
            } catch (ex: IOException) {
                Log.e(TAG, "getSettings: Could not create file $STORAGE_FILE", ex)
            }
        } catch (e: Exception) {
            if (e !is IOException && e !is JSONException) throw e
            Log.e(TAG, "getSettings: Could not read JSON object", e)
        }

        return JSONObject()
    }

    private fun putSettings(context: Context, settings: JSONObject) {
        try {
            context.openFileOutput(STORAGE_FILE, Context.MODE_PRIVATE).use { fos ->
                fos.write(settings.toString().toByteArray())
                Log.d(TAG, "putSettings: Updated settings file.")
                Log.d(TAG, "putSettings: JSON Data: $settings")
            }
        } catch (e: IOException) {
            Log.e(TAG, "putSettings: Could not write settings", e)
        }
    }
}