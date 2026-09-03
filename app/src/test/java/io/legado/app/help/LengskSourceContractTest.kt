package io.legado.app.help

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class LengskSourceContractTest {

    @Test
    fun searchUsesSingleGetRequestAndTitleAnchor() {
        val source = projectFile("src/main/assets/defaultData/bookSources/lengsk.js")
            .readText()
            .replace("\r\n", "\n")
        val search = source.substringAfter("function search(key,page)")
            .substringBefore("function getBookInfo")

        assertTrue(search.contains("/search/?searchkey="))
        assertTrue(search.contains("java.get(u,hd,20000)"))
        assertTrue(search.contains("h6 a[href*=\"/leng/\"]"))
        assertFalse(search.contains("java.post(config.bookSourceUrl+\"/search/\""))
        assertFalse(search.contains("java.ajax(u,20000)"))
    }

    private fun projectFile(pathInApp: String): File =
        listOf(File(pathInApp), File("app/$pathInApp"))
            .firstOrNull { it.isFile }
            ?: error("Missing project file: $pathInApp")
}
