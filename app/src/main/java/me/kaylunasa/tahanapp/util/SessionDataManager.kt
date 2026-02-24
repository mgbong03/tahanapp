package me.kaylunasa.tahanapp.util

import android.content.Context
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader
import java.security.MessageDigest

object SessionDataManager {
    private val TAG: String = SessionDataManager::class.java.simpleName
    private const val STORAGE_FILE = "session_data.json"

    private const val KEY_USERNAME = "username"
    private const val KEY_PASSWORD = "passwordHash"

    @JvmStatic
    fun dumpSessionData(context: Context) {
        Log.d(TAG, "dumpSessionData: Dumping session data")
        Log.d(TAG, "dumpSessionData: " + getSessionData(context))
    }

    @JvmStatic
    @Throws(JSONException::class)
    fun getSessionUsername(context: Context): String? {
        return getSessionKey<String?>(context, KEY_USERNAME)
    }

    @JvmStatic
    @Throws(JSONException::class)
    fun putSessionUsername(context: Context, username: String) {
        putSessionKey(context, KEY_USERNAME, username)
    }

    @JvmStatic
    @Throws(JSONException::class)
    fun getSessionPasswordHash(context: Context): String? {
        return getSessionKey<String?>(context, KEY_PASSWORD)
    }

    @JvmStatic
    @Throws(JSONException::class)
    fun putSessionPasswordHash(context: Context, passwordHash: String) {
        putSessionKey(context, KEY_PASSWORD, passwordHash)
    }

    @Throws(JSONException::class)
    private inline fun <reified T> getSessionKey(context: Context, key: String): T? {
        val jsonObject = getSessionData(context)
        return if (jsonObject.has(key) &&
            !jsonObject.isNull(key) &&
            jsonObject.get(key) is T
        ) jsonObject.get(key) as T?
        else null
    }

    @Throws(JSONException::class)
    private fun <T> putSessionKey(context: Context, key: String, value: T?) {
        val jsonObject = getSessionData(context)
        jsonObject.put(key, value)
        putSessionData(context, jsonObject)
    }
    
    @JvmStatic
    @Throws(IOException::class)
    fun resetSessionData(context: Context) {
        context.openFileOutput(STORAGE_FILE, Context.MODE_PRIVATE).use { fos ->
            val jsonObject = JSONObject()
            fos.write(jsonObject.toString().toByteArray())
        }
    }

    private fun getSessionData(context: Context): JSONObject {
        try {
            BufferedReader(InputStreamReader(context.openFileInput(STORAGE_FILE))).use { reader ->
                val sb = StringBuilder()
                var input: String?
                while ((reader.readLine().also { input = it }) != null) sb.append(input)
                check(!sb.toString().isEmpty())

                val jsonObject = JSONObject(sb.toString())

                Log.d(TAG, "getSessionData: Loaded session data from file storage")
                return jsonObject
            }
        } catch (e : Exception) {
            if (e !is IllegalStateException && e !is FileNotFoundException) throw e
            try {
                context.openFileOutput(STORAGE_FILE, Context.MODE_PRIVATE).use { fos ->
                    val jsonObject = JSONObject()
                    fos.write(jsonObject.toString().toByteArray())
                    Log.d(TAG, "getSessionData: Created file $STORAGE_FILE")
                }
            } catch (ex: IOException) {
                Log.e(TAG, "getSessionData: Could not create file $STORAGE_FILE", ex)
            }
        } catch (e: Exception) {
            if (e !is IOException && e !is JSONException) throw e
            Log.e(TAG, "getSessionData: Could not read JSON object", e)
        }

        return JSONObject()
    }

    private fun putSessionData(context: Context, data: JSONObject) {
        try {
            context.openFileOutput(STORAGE_FILE, Context.MODE_PRIVATE).use { fos ->
                fos.write(data.toString().toByteArray())
                Log.d(TAG, "putSessionData: Updated session data file.")
                Log.d(TAG, "putSessionData: JSON Data: $data")
            }
        } catch (e: IOException) {
            Log.e(TAG, "putSessionData: Could not write session data", e)
        }
    }
}