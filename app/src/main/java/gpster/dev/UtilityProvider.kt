package gpster.dev

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.ParseException

class UtilityProvider(
    private val context: Context
) {
    fun toast(s: String) {
        Toast.makeText(
            context, s, Toast.LENGTH_SHORT
        ).show()
    }
    fun snackbar(v: View, s: String) {
        Snackbar.make(
            v, s, Snackbar.LENGTH_SHORT
        ).show()
    }
    fun hideKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        activity.currentFocus?.let {
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }
    fun showKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        activity.currentFocus?.let {
            imm.showSoftInput(it, InputMethodManager.SHOW_IMPLICIT)
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
    fun parseToDouble(s: String, format: String, min: Double, max: Double, def: Double) : Double {
        try {
            val numFormat : NumberFormat = DecimalFormat(format)
            val parsedNum : Number = numFormat.parse(s.replace("[^\\d.]".toRegex(), ""))
            val result : Double = parsedNum.toDouble()

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
}