package io.legado.app.ui

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class NovelHelperStage6VisualContractTest {

    @Test
    fun bookshelfItemsUseUnifiedCardSurfaceWithoutChangingProgressContract() {
        listOf(
            "item_bookshelf_grid.xml",
            "item_bookshelf_grid2.xml",
            "item_bookshelf_list.xml",
            "item_bookshelf_list2.xml",
        ).forEach { name ->
            val layout = projectFile("src/main/res/layout/$name").readText()
            assertTrue(layout.contains("@drawable/novel_helper_preference_card"))
            assertTrue(layout.contains("android:elevation=\"1dp\""))
            assertTrue(layout.contains("@+id/pb_read_progress"))
            assertTrue(layout.contains("app:trackThickness=\"2dp\""))
        }
    }

    @Test
    fun discoveryAndBookshelfEmptyStatesUseNovelHelperStateCards() {
        listOf("fragment_bookshelf2.xml", "fragment_books.xml", "fragment_explore.xml")
            .forEach { name ->
                val layout = projectFile("src/main/res/layout/$name").readText()
                assertTrue(layout.contains("@drawable/novel_helper_state_card"))
                assertTrue(layout.contains("android:textColor=\"@color/tv_text_summary\""))
            }
        val explore = projectFile("src/main/res/layout/fragment_explore.xml").readText()
        assertTrue(explore.contains("android:clipToPadding=\"false\""))
        assertTrue(explore.contains("@+id/fast_scroller"))
    }

    @Test
    fun readRecordUsesLayeredCardsAndKeepsAuthorSlot() {
        val activity = projectFile("src/main/res/layout/activity_read_record.xml").readText()
        val item = projectFile("src/main/res/layout/item_read_record.xml").readText()
        assertTrue(activity.contains("android:background=\"@color/background\""))
        assertTrue(activity.contains("@drawable/novel_helper_preference_card"))
        assertTrue(item.contains("@drawable/novel_helper_preference_card"))
        assertTrue(item.contains("@+id/tv_author"))
        assertTrue(item.contains("app:layout_constraintTop_toBottomOf=\"@id/tv_author\""))
    }

    @Test
    fun landscapeBookInfoUsesTwoCardPanels() {
        val layout = projectFile("src/main/res/layout-land/activity_book_info.xml").readText()
        assertTrue(layout.contains("android:id=\"@+id/scroll_view_l\""))
        assertTrue(layout.split("@drawable/novel_helper_preference_card").size - 1 >= 2)
        assertTrue(layout.split("android:elevation=\"2dp\"").size - 1 >= 2)
        assertTrue(layout.contains("android:clipToOutline=\"true\""))
        assertTrue(layout.contains("@drawable/novel_helper_section_outline"))
    }

    private fun projectFile(pathInApp: String): File =
        listOf(File(pathInApp), File("app/$pathInApp"))
            .firstOrNull { it.isFile }
            ?: error("Missing project file: $pathInApp")
}
