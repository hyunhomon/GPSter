package gpster.dev

import android.app.Activity
import android.content.Context
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import java.text.ParseException

class UtilityProvider(
    private val context: Context
) {
    fun toast(s: String) {
        Toast.makeText(
            context, s, Toast.LENGTH_SHORT
        ).show()
    }
    fun hideKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        activity.currentFocus?.let {
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }
    fun focusHandling(currentEt: EditText, nextEt: EditText?, activity: Activity?) {
        val listener = TextView.OnEditorActionListener { _, actionId, _ ->
            when(actionId) {
                EditorInfo.IME_ACTION_NEXT -> {
                    nextEt!!.requestFocus()
                    true
                }
                EditorInfo.IME_ACTION_DONE -> {
                    hideKeyboard(activity!!)
                    currentEt.clearFocus()
                    true
                }
                else -> false
            }
        }
        currentEt.setOnEditorActionListener(listener)
    }
    fun focusClear(etList: List<EditText>) {
        for(et in etList) {
            et.setOnEditorActionListener(null)
        }
    }
    fun parseToDouble(s: String, min: Double, max: Double, def: Double, format: String) : Double {
        try {
            val num = s.toDoubleOrNull() ?: def
            val result : Double = format.format(num).toDouble()

            return if(result in min .. max) result else {
                toast("입력 범위를 벗어났습니다")
                if(result > 0) max else min
            }
        } catch (e: ParseException) {
            e.printStackTrace()
            toast("유효한 숫자를 입력해주세요")
            return def
        }
    }
    fun getSpeed() : Double {
        val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        return prefs.getFloat("speed", 10.0f).toDouble()
    }
    fun setSpeed(value: Double) {
        val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        prefs.edit().putFloat("speed", value.toFloat()).apply()
    }
}