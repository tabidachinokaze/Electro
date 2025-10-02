package cn.tabidachi.electro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.junit.Test

class ModifierTest {
    @Test
    fun testModifier() {
        val modifier = Modifier
            .background(color = Color.Transparent)
            .padding(16.dp)
        val modifier2 = Modifier
            .background(color = Color.Transparent)
            .then(Modifier.padding(16.dp))
        ComponentInner(modifier = modifier)
        val foldIn1 = modifier.foldIn(modifier) { a, b ->
            println("foldIn1: $b")
            a
        }
        val foldIn2 = modifier2.foldIn(modifier2) { a, b ->
            println("foldIn2: $b")
            a
        }
        val foldOut = modifier.foldOut(modifier) { a, b ->
            println("foldOut: $a")
            b
        }
        println("foldIn1 = $foldIn1")
        println("foldIn2 = $foldIn2")
        println("foldOut = $foldOut")
    }
}

fun ComponentInner(
    modifier: Modifier
) {
    modifier.background(
        color = Color.Blue
    )
}