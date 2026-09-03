package io.legado.app.ui

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class XuanjuanAlertDialogThemeContractTest {

    @Test
    fun baseThemeUsesHighContrastXuanjuanAlertDialogs() {
        val styles = projectFile("src/main/res/values/styles.xml").readText()

        assertTrue(styles.contains("<item name=\"alertDialogTheme\">@style/XuanjuanAlertDialogTheme</item>"))
        assertTrue(styles.contains("<style name=\"XuanjuanAlertDialogTheme\""))
        assertTrue(styles.contains("<item name=\"android:windowBackground\">@drawable/xuanjuan_reader_dialog_bg</item>"))
        assertTrue(styles.contains("<item name=\"android:textColorPrimary\">@color/xuanjuan_text_primary</item>"))
        assertTrue(styles.contains("<item name=\"android:textColorSecondary\">@color/xuanjuan_text_secondary</item>"))
        assertTrue(styles.contains("<item name=\"textColorAlertDialogListItem\">@color/xuanjuan_text_primary</item>"))
        assertTrue(styles.contains("<item name=\"colorAccent\">@color/xuanjuan_gold</item>"))
    }

    private fun projectFile(pathInApp: String): File =
        listOf(File(pathInApp), File("app/$pathInApp"))
            .firstOrNull { it.isFile }
            ?: error("Missing project file: $pathInApp")
}
