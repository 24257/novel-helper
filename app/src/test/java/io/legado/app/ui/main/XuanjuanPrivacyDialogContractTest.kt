package io.legado.app.ui.main

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class XuanjuanPrivacyDialogContractTest {

    @Test
    fun privacyDialogUsesReadableXuanjuanPaletteAndCannotLeaveStartupSuspended() {
        val source = projectFile(
            "src/main/java/io/legado/app/ui/main/MainActivity.kt"
        ).readText().replace("\r\n", "\n")

        val privacy = source.substringAfter("private suspend fun privacyPolicy()")
            .substringBefore("private suspend fun upVersion()")

        assertTrue(privacy.contains("dialog.setCancelable(false)"))
        assertTrue(privacy.contains("dialog.setCanceledOnTouchOutside(false)"))
        assertTrue(privacy.contains("R.drawable.xuanjuan_reader_dialog_bg"))
        assertTrue(privacy.contains("R.color.xuanjuan_text_primary"))
        assertTrue(privacy.contains("R.color.xuanjuan_gold_soft"))
        assertTrue(privacy.contains("R.color.xuanjuan_gold"))
    }

    private fun projectFile(path: String): File = sequenceOf(File(path), File("app/$path"))
        .first(File::isFile)
}
