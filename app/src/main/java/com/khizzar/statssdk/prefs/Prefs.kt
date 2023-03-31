package com.khizzar.statssdk.prefs


import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.khizzar.statssdk.utils.FILE_NAME

/**
 * Created By: Norgic
 * Date & Time: On 1/20/21 At 3:31 PM in 2021
 *
 * This class is mainly used to locally store and use data in the application
 * @param context the context of the application or the activity from where it is called
 */
class Prefs(context: Context?) {
    private val mPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val TIME_INTERVAL = "time_interval"
    private val WRITE_LOGS = "write_logs"

    var timeInterval: Int
        get() {
            return mPrefs.getInt(TIME_INTERVAL, 0)
        }
        set(timeInterval) {
            val mEditor: SharedPreferences.Editor = mPrefs.edit()
            mEditor.putInt(TIME_INTERVAL, timeInterval)
            mEditor.apply()
        }

    var enableLogsWriting: Boolean?
        get(){
            return mPrefs.getBoolean(WRITE_LOGS, false)
        }
        set(enableLogsWriting) {
            val mEditor: SharedPreferences.Editor = mPrefs.edit()
            mEditor.putBoolean(WRITE_LOGS, enableLogsWriting?:false)
            mEditor.apply()
        }

    var fileInfo: String?
        get(){
            return mPrefs.getString(FILE_NAME, "")
        }
        set(fileInfo) {
            val mEditor: SharedPreferences.Editor = mPrefs.edit()
            mEditor.putString(FILE_NAME, fileInfo)
            mEditor.apply()
        }

    /**
     * Function to clear all prefs from storage
     * */
    fun clearAll() {
        mPrefs.edit().clear().apply()
    }
}
