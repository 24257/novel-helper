package io.legado.app.ui.book.read.config

import io.legado.app.help.config.ReadBookConfig
import io.legado.app.help.config.upgradeLegacyBuiltinReadPresets
import io.legado.app.utils.GSON
import io.legado.app.utils.fromJsonArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class XuanjuanComfortReadingThemeTest {

    @Test
    fun packagedThemesContainFiveComfortPresets() {
        val configs = packagedConfigs()
        assertTrue(configs.size >= 6)
        assertEquals(
            listOf("玄卷·暖纸", "玄卷·浅青", "玄卷·素白", "玄卷·夜墨", "玄卷·羊皮卷"),
            configs.take(5).map { it.name },
        )
        configs.take(5).forEach {
            assertEquals(23, it.textSize)
            assertEquals(18, it.lineSpacingExtra)
            assertEquals(6, it.paragraphSpacing)
            assertEquals(20, it.paddingLeft)
            assertEquals(20, it.paddingRight)
        }
    }

    @Test
    fun legacyGenericPresetsUpgradeWithoutTouchingNamedUserStyle() {
        val defaults = packagedConfigs()
        val legacy = listOf(
            ReadBookConfig.Config(name = "我的排版", textSize = 29),
            ReadBookConfig.Config(name = "预设1"),
            ReadBookConfig.Config(name = "预设3"),
        )
        val (upgraded, changed) = upgradeLegacyBuiltinReadPresets(legacy, defaults)
        assertTrue(changed)
        assertEquals("我的排版", upgraded[0].name)
        assertEquals(29, upgraded[0].textSize)
        assertEquals("玄卷·暖纸", upgraded[1].name)
        assertEquals("玄卷·素白", upgraded[2].name)
    }

    @Test
    fun alreadyCustomizedNamesAreNotMigrated() {
        val defaults = packagedConfigs()
        val configs = listOf(ReadBookConfig.Config(name = "预设1-我改过", textSize = 27))
        val (upgraded, changed) = upgradeLegacyBuiltinReadPresets(configs, defaults)
        assertFalse(changed)
        assertEquals(27, upgraded.single().textSize)
    }

    @Test
    fun stylePickerUsesNamedPreviewCards() {
        val layout = projectFile("src/main/res/layout/item_read_style.xml").readText()
        val dialog = projectFile("src/main/java/io/legado/app/ui/book/read/config/ReadStyleDialog.kt").readText()
        val styleLayout = projectFile("src/main/res/layout/dialog_read_book_style.xml").readText()
        assertTrue(layout.contains("@+id/tv_style_name"))
        assertTrue(layout.contains("app:text=\"Aa\""))
        assertTrue(dialog.contains("tvStyleName.text = item.name"))
        assertTrue(styleLayout.contains("@string/xuanjuan_reader_theme_section"))
    }

    private fun packagedConfigs(): List<ReadBookConfig.Config> =
        GSON.fromJsonArray<ReadBookConfig.Config>(
            projectFile("src/main/assets/defaultData/readConfig.json").readText()
        ).getOrThrow()

    private fun projectFile(path: String): File = sequenceOf(File(path), File("app/$path"))
        .first(File::isFile)
}
