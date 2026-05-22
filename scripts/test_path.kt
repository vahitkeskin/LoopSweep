import androidx.compose.ui.graphics.vector.PathParser

fun main() {
    val p = PathParser()
    p.parsePathString("M0,0 L10,10")
    println(p.toPath())
}
