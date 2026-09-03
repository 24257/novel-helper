package io.legado.app.ui.book.search

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SearchHistoryVisualContractTest {

    @Test
    fun historyUsesOpenSurfaceAndXuanjuanChips() {
        val layout = projectFile("src/main/res/layout/activity_book_search.xml").readText()
        val adapter = projectFile("src/main/java/io/legado/app/ui/book/search/HistoryKeyAdapter.kt").readText()

        val panel = layout.substringAfter("@+id/input_help_panel").substringBefore("</LinearLayout>")
        assertTrue(panel.contains("@android:color/transparent"))
        assertTrue(panel.contains("android:elevation=\"0dp\""))
        assertTrue(adapter.contains("R.drawable.xuanjuan_explore_chip"))
        assertTrue(adapter.contains("R.color.xuanjuan_text_primary"))
    }

    private fun projectFile(pathInApp: String): File =
        listOf(File(pathInApp), File("app/$pathInApp"))
            .firstOrNull { it.isFile }
            ?: error("Missing project file: $pathInApp")
}
