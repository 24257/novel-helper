package io.legado.app.ui

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class XuanjuanBrandContractTest {

    @Test
    fun brandNameAndInAppBrandMarkUseXuanjuanAssets() {
        val defaults = projectFile("src/main/res/values/strings.xml").readText()
        val zh = projectFile("src/main/res/values-zh/strings.xml").readText()
        val zhCnBrand = projectFile("src/main/res/values-zh-rCN/novel_helper_strings.xml").readText()
        val layout = projectFile("src/main/res/layout/activity_welcome.xml").readText()
        val brandMark = projectFile("src/main/res/drawable/novel_helper_brand_mark.xml").readText()

        assertTrue(defaults.contains("<string name=\"app_name\">玄卷</string>"))
        assertTrue(zh.contains("<string name=\"app_name\">玄卷</string>"))
        assertTrue(zhCnBrand.contains("<string name=\"welcome_tagline\">万千故事，皆藏卷中</string>"))
        assertTrue(!layout.contains("iv_brand_splash"))
        assertTrue(!layout.contains("xuanjuan_start"))
        assertTrue(layout.contains("@+id/iv_book"))
        assertTrue(layout.contains("@+id/tv_legado"))
        assertTrue(brandMark.contains("@drawable/xuanjuan_logo"))
        assertTrue(projectFile("src/main/res/drawable-nodpi/xuanjuan_logo.png").length() > 100_000)
        assertTrue(!projectPath("src/main/res/drawable-nodpi/xuanjuan_start.png").exists())
    }

    @Test
    fun launchersAndAndroid12SplashUseXuanjuanBranding() {
        val manifest = projectFile("src/main/AndroidManifest.xml").readText()
        val adaptive = projectFile("src/main/res/mipmap-anydpi-v26/novel_helper_launcher.xml").readText()
        val legacy = projectFile("src/main/res/mipmap-anydpi/novel_helper_launcher.xml").readText()
        val v31 = projectFile("src/main/res/values-v31/styles.xml").readText()
        val welcome = projectFile("src/main/java/io/legado/app/ui/welcome/WelcomeActivity.kt").readText()

        assertTrue(manifest.contains("android:theme=\"@style/AppTheme.Welcome.Starting\""))
        assertTrue(manifest.contains("android:icon=\"@mipmap/novel_helper_launcher\""))
        assertTrue(adaptive.contains("@drawable/xuanjuan_launcher_foreground"))
        assertTrue(legacy.contains("@drawable/xuanjuan_logo"))
        assertTrue(v31.contains("android:windowSplashScreenAnimatedIcon"))
        assertTrue(v31.contains("@drawable/xuanjuan_launcher_foreground"))
        assertTrue(welcome.contains("else if (!getPrefBoolean(PreferKey.customWelcome))"))
        assertTrue(!welcome.contains("ivBrandSplash"))
    }

    private fun projectFile(pathInApp: String): File {
        return projectPath(pathInApp).also { file ->
            require(file.isFile) { "Missing project file: $pathInApp" }
        }
    }

    private fun projectPath(pathInApp: String): File {
        val userDir = requireNotNull(System.getProperty("user.dir"))
        val appDir = generateSequence(File(userDir)) { it.parentFile }
            .map { File(it, "app") }
            .first(File::isDirectory)
        return File(appDir, pathInApp)
    }
}
