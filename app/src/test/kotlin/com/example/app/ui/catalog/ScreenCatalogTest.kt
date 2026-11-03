package com.example.app.ui.catalog

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToString
import com.example.app.ui.theme.AppTheme
import com.github.takahirom.roborazzi.captureRoboImage
import java.io.File
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Pour chaque ecran du catalogue :
 *  - ecrit l'arbre semantics dans app/build/ui-dumps/<nom>.txt (lu par tools/see.ps1) ;
 *  - capture/compare une image Roborazzi dans app/src/test/snapshots/<nom>.png
 *    (seulement avec recordRoborazziDebug / verifyRoborazziDebug).
 * NE PAS MODIFIER : ajouter les ecrans dans ScreenCatalog.kt.
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w411dp-h891dp-xxhdpi")
class ScreenCatalogTest(private val name: String) {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun screen() {
        val content = screenCatalog.getValue(name)
        composeRule.setContent { AppTheme(darkTheme = false) { content() } }
        composeRule.waitForIdle()

        val dump = composeRule.onRoot(useUnmergedTree = true).printToString(maxDepth = Int.MAX_VALUE)
        File("build/ui-dumps").apply { mkdirs() }.resolve("$name.txt").writeText(dump)

        composeRule.onRoot().captureRoboImage("src/test/snapshots/$name.png")
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun screens(): List<Array<Any>> = screenCatalog.keys.map { arrayOf(it) }
    }
}
