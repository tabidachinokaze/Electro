package cn.tabidachi.electro

import android.content.Context
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.inputmethod.EditorInfoCompat
import androidx.core.view.inputmethod.InputConnectionCompat
import io.ktor.http.ContentType

/**
 * A [android.view.View] that can host Jetpack Compose UI content.
 * Use [setContent] to supply the content composable function for the view.
 *
 * By default, the composition is disposed according to [ViewCompositionStrategy.Default].
 * Call [disposeComposition] to dispose of the underlying composition earlier, or if the view is
 * never initially attached to a window. (The requirement to dispose of the composition explicitly
 * in the event that the view is never (re)attached is temporary.)
 */
class ElectroComposeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AbstractComposeView(context, attrs, defStyleAttr) {

    private val content = mutableStateOf<(@Composable () -> Unit)?>(null)

    @Suppress("RedundantVisibilityModifier")
    protected override var shouldCreateCompositionOnAttachedToWindow: Boolean = false
        private set

    @Composable
    override fun Content() {
        content.value?.invoke()
    }

    override fun getAccessibilityClassName(): CharSequence {
        return javaClass.name
    }

    /**
     * Set the Jetpack Compose UI content for this view.
     * Initial composition will occur when the view becomes attached to a window or when
     * [createComposition] is called, whichever comes first.
     */
    fun setContent(content: @Composable () -> Unit) {
        shouldCreateCompositionOnAttachedToWindow = true
        this.content.value = content
        if (isAttachedToWindow) {
            createComposition()
        }
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection {
        println("onCreateInputConnection")
        val connection = super.onCreateInputConnection(outAttrs)
        EditorInfoCompat.setContentMimeTypes(outAttrs, arrayOf(ContentType.Image.Any.toString()))
        val listener =
            InputConnectionCompat.OnCommitContentListener { inputContentInfo, flags, opts ->
                println(inputContentInfo)
                println(flags)
                println(opts)
                true
            }
        return InputConnectionCompat.createWrapper(connection, outAttrs, listener)
    }
}
