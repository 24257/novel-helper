package io.legado.app.ui.book.searchContent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class SearchContentLayoutTest {

    @Test
    fun searchResultTextIsConstrainedAcrossParentWidth() {
        val view = searchResultView()

        assertEquals("0dp", view.getAttributeNS(ANDROID_NS, "layout_width"))
        assertEquals("wrap_content", view.getAttributeNS(ANDROID_NS, "layout_height"))
        assertEquals("false", view.getAttributeNS(ANDROID_NS, "singleLine"))
        assertEquals("14sp", view.getAttributeNS(ANDROID_NS, "textSize"))
        assertEquals("parent", view.getAttributeNS(APP_NS, "layout_constraintStart_toStartOf"))
        assertEquals("parent", view.getAttributeNS(APP_NS, "layout_constraintEnd_toEndOf"))
        assertFalse(view.hasAttributeNS(APP_NS, "layout_constraintRight_toLeftOf"))
    }

    @Test
    fun searchResultItemUsesFlatClickableCard() {
        val card = searchResultLayout().documentElement

        assertEquals("androidx.constraintlayout.widget.ConstraintLayout", card.tagName)
        assertEquals("match_parent", card.getAttributeNS(ANDROID_NS, "layout_width"))
        assertEquals("wrap_content", card.getAttributeNS(ANDROID_NS, "layout_height"))
        assertEquals("8dp", card.getAttributeNS(ANDROID_NS, "layout_marginHorizontal"))
        assertEquals("4dp", card.getAttributeNS(ANDROID_NS, "layout_marginVertical"))
        assertEquals("@drawable/novel_helper_preference_card", card.getAttributeNS(ANDROID_NS, "background"))
        assertEquals("true", card.getAttributeNS(ANDROID_NS, "clipToOutline"))
        assertEquals("1dp", card.getAttributeNS(ANDROID_NS, "elevation"))
        assertEquals("?android:attr/selectableItemBackground", card.getAttributeNS(ANDROID_NS, "foreground"))
        assertEquals("12dp", card.getAttributeNS(ANDROID_NS, "padding"))
    }

    private fun searchResultView(): Element {
        val textViews = searchResultLayout().getElementsByTagName("TextView")
        return (0 until textViews.length)
            .asSequence()
            .map { textViews.item(it) as Element }
            .first { it.getAttributeNS(ANDROID_NS, "id") == "@+id/tv_search_result" }
    }

    private fun searchResultLayout(): Document {
        val layout = listOf(
            File("src/main/res/layout/item_search_list.xml"),
            File("app/src/main/res/layout/item_search_list.xml"),
        ).firstOrNull { it.isFile } ?: error("item_search_list.xml not found")
        return DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = true
        }.newDocumentBuilder().parse(layout)
    }

    companion object {
        private const val ANDROID_NS = "http://schemas.android.com/apk/res/android"
        private const val APP_NS = "http://schemas.android.com/apk/res-auto"
    }
}
