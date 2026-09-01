package io.legado.app.ui.main.bookshelf

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class BookshelfConfigContrastTest {

    @Test
    fun darkBookshelfDialogUsesExplicitReadableControls() {
        val layout = projectFile("src/main/res/layout/dialog_bookshelf_config.xml").readText()
        val styles = projectFile("src/main/res/values/styles.xml").readText()
        val fragment = projectFile(
            "src/main/java/io/legado/app/ui/main/bookshelf/BaseBookshelfFragment.kt"
        ).readText()

        assertTrue(layout.contains("@+id/tv_bookshelf_config_title"))
        assertTrue(layout.contains("android:text=\"@string/bookshelf_layout\""))
        assertTrue(layout.contains("android:textColor=\"@color/xuanjuan_gold_soft\""))

        val switchCount = layout.split("<io.legado.app.lib.theme.view.ThemeSwitch").size - 1
        assertTrue(switchCount >= 7)
        layout.split("<io.legado.app.lib.theme.view.ThemeSwitch").drop(1).forEach { block ->
            assertTrue(block.substringBefore("/>").contains("android:textColor=\"@color/primaryText\""))
        }

        val radioCount = layout.split("<io.legado.app.lib.theme.view.ThemeRadioButton").size - 1
        assertTrue(radioCount >= 16)
        layout.split("<io.legado.app.lib.theme.view.ThemeRadioButton").drop(1).forEach { block ->
            assertTrue(block.substringBefore("/>").contains("android:textColor=\"@color/primaryText\""))
        }
        assertFalse(layout.contains("<RadioButton"))

        assertTrue(layout.split("android:theme=\"@style/XuanjuanSpinner\"").size - 1 >= 2)
        assertTrue(layout.contains("app:layout_constraintTop_toBottomOf=\"@+id/sw_auto_follow\""))
        assertTrue(styles.contains("<style name=\"XuanjuanSpinner\" parent=\"android:Theme.Holo\">"))
        assertTrue(styles.contains("<item name=\"android:textColorPrimary\">@color/primaryText</item>"))

        assertFalse(fragment.contains("alert(titleResource = R.string.bookshelf_layout)"))
        assertTrue(fragment.contains("fun configBookshelf()"))
    }

    private fun projectFile(pathInApp: String): File =
        listOf(File(pathInApp), File("app/$pathInApp"))
            .firstOrNull { it.isFile }
            ?: error("Missing project file: $pathInApp")
}
