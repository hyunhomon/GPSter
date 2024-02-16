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
    fun focusHandling(currentEt: EditText, nextEt: EditText?) {
        val listener = TextView.OnEditorActionListener { _, actionId, _ ->
            when(actionId) {
                EditorInfo.IME_ACTION_NEXT -> {
                    nextEt!!.requestFocus()
                    true
                }
                EditorInfo.IME_ACTION_DONE -> {
                    currentEt.clearFocus()
                    true
                }
                else -> false
            }
        }
        currentEt.setOnEditorActionListener(listener)
    }
}