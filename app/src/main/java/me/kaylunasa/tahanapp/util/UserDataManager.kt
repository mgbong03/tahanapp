package me.kaylunasa.tahanapp.util

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader

object UserDataManager {
    private val TAG: String = UserDataManager::class.java.simpleName
    private const val STORAGE_PATH_FORMAT = "%s_userdata.json"

    private fun checkUserPassword(jsonObject: JSONObject, passwordHash: String): Boolean {
        if (!jsonObject.has("passwordHash") || jsonObject.get("passwordHash") !is String)
            return false
        return jsonObject.getString("passwordHash").equals(passwordHash)
    }

    @JvmStatic
    fun checkUserPassword(context: Context, username: String, passwordHash: String): Boolean {
        val filePath: String = STORAGE_PATH_FORMAT.format(username)

        if (!userDataExists(context, username)) {
            Log.e(TAG, "checkUserPassword: User data at $filePath does not exist")
            return false
        }
        try {
            BufferedReader(InputStreamReader(context.openFileInput(filePath))).use { reader ->
                val sb = StringBuilder()
                var input: String?
                while ((reader.readLine().also { input = it }) != null) sb.append(input)
                check(!sb.toString().isEmpty())

                val jsonObject = JSONObject(sb.toString())

                Log.d(TAG, "checkUserPassword: Loaded user data at $filePath from file storage")

                return checkUserPassword(jsonObject, passwordHash)
            }
        } catch (e: Exception) {
            Log.e(TAG, "checkUserPassword: Could not check password, malformed or inaccessible JSON data", e)
        }

        return false
    }

    @JvmStatic
    fun getUserData(context: Context, username: String, passwordHash: String): JSONObject {
        val filePath: String = STORAGE_PATH_FORMAT.format(username)
        try {
            BufferedReader(InputStreamReader(context.openFileInput(filePath))).use { reader ->
                val sb = StringBuilder()
                var input: String?
                while ((reader.readLine().also { input = it }) != null) sb.append(input)
                check(!sb.toString().isEmpty())

                val jsonObject = JSONObject(sb.toString())

                Log.d(TAG, "getUserData: Loaded user data at $filePath from file storage")

                if (!checkUserPassword(jsonObject, passwordHash)) {
                    Log.d(TAG, "getUserData: Invalid or inaccessible password")
                    return JSONObject()
                }

                return jsonObject
            }
        } catch (e: FileNotFoundException) {
            Log.e(TAG, "getUserData: File $filePath does not exist yet", e)
        } catch (e: Exception) {
            if (e !is IllegalStateException && e !is IOException && e !is JSONException) throw e
            Log.e(TAG, "getUserData: Could not read JSON object", e)
        }

        return JSONObject()
    }

    @JvmStatic
    fun putUserData(context: Context, username: String, passwordHash: String, data: JSONObject): String {
        val filePath: String = STORAGE_PATH_FORMAT.format(username)

        if (userDataExists(context, username) && !checkUserPassword(context, username, passwordHash)) {
            Log.d(TAG, "putUserData: Invalid or inaccessible password")
            return "Invalid password"
        }
        
        try {
            context.openFileOutput(filePath, Context.MODE_PRIVATE).use { fos ->
                fos.write(data.toString().toByteArray())
                Log.d(TAG, "putUserData: Updated user data file.")
                Log.d(TAG, "putUserData: JSON Data: $data")
            }
            return "Successfully wrote data"
        } catch (e: IOException) {
            Log.e(TAG, "putUserData: Could not write user data", e)
        }
        return "Could not (over)write user data"
    }
    
    @JvmStatic
    fun createUserData(context: Context, username: String, passwordHash: String): String {
        val jsonObject = JSONObject()
        jsonObject.put("username", username)
        jsonObject.put("passwordHash", passwordHash)
        jsonObject.put("childProfiles", JSONArray())

        if (userDataExists(context, username)) {
            Log.e(TAG, "createUserData: User already exists")
            return "User already exists"
        }
        return putUserData(context, username, passwordHash, jsonObject)
    }

    @JvmStatic
    fun deleteUserData(context: Context, username: String, passwordHash: String): String {
        val filePath: String = STORAGE_PATH_FORMAT.format(username)
        if (!userDataExists(context, username)) {
            Log.d(TAG, "deleteUserData: User data doesn't exist")
            return "User does not exist"
        }
        if (!checkUserPassword(context, username, passwordHash)) {
            Log.d(TAG, "deleteUserData: Invalid or inaccessible password")
            return "Invalid password"
        }
        return if (context.deleteFile(filePath)) "Successfully deleted user data"
        else "Could not delete user data"
    }

    @JvmStatic
    fun userDataExists(context: Context, username: String): Boolean =
        File(context.filesDir, STORAGE_PATH_FORMAT.format(username)).exists()
}