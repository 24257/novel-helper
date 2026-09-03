package io.legado.app.ui.main

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class XuanjuanPolishContractTest {

    @Test
    fun exploreSourceCardUsesOneOutlineInsteadOfNestedCards() {
        val layout = projectFile("src/main/res/layout/item_find_book.xml").readText()
        assertTrue(layout.contains("android:background=\"@drawable/novel_helper_preference_card\""))
        assertFalse(layout.contains("android:background=\"@drawable/bg_find_book_group\""))
    }

    @Test
    fun exploreResultsKeepCardRowsWithoutLegacyDivider() {
        val activity = projectFile(
            "src/main/java/io/legado/app/ui/book/explore/ExploreShowActivity.kt"
        ).readText()
        val row = projectFile("src/main/res/layout/item_search.xml").readText()

        assertFalse(activity.contains("VerticalDivider"))
        assertTrue(activity.contains("binding.recyclerView.adapter = adapter"))
        assertTrue(activity.contains("viewModel.explore()"))
        assertTrue(row.contains("@drawable/xuanjuan_search_result_card"))
    }

    @Test
    fun contentSearchResultsUseXuanjuanCardsWithoutChangingSearchNavigation() {
        val activity = projectFile(
            "src/main/java/io/legado/app/ui/book/searchContent/SearchContentActivity.kt"
        ).readText()
        val adapter = projectFile(
            "src/main/java/io/legado/app/ui/book/searchContent/SearchContentAdapter.kt"
        ).readText()
        val row = projectFile("src/main/res/layout/item_search_list.xml").readText()

        assertFalse(activity.contains("VerticalDivider"))
        assertTrue(activity.contains("startContentSearch(query.trim())"))
        assertTrue(adapter.contains("callback.openSearchResult(it, holder.layoutPosition)"))
        assertTrue(row.contains("@drawable/novel_helper_preference_card"))
        assertTrue(row.contains("android:clipToOutline=\"true\""))
        assertTrue(row.contains("@+id/tv_search_result"))
    }

    @Test
    fun myPageSwitchUsesXuanjuanGoldInsteadOfLegacyAccentColor() {
        val source = projectFile(
            "src/main/java/io/legado/app/ui/main/my/NovelHelperPreferences.kt"
        ).readText()
        assertTrue(source.contains("R.color.xuanjuan_gold_soft"))
        assertTrue(source.contains("switchWidget"))
        assertTrue(source.contains("applyTint"))
    }

    @Test
    fun bookSourceSelectionCheckboxesStayVisibleOnDarkXuanjuanSurface() {
        val adapter = projectFile(
            "src/main/java/io/legado/app/ui/book/source/manage/BookSourceAdapter.kt"
        ).readText()
        val activity = projectFile(
            "src/main/java/io/legado/app/ui/book/source/manage/BookSourceActivity.kt"
        ).readText()

        assertTrue(adapter.contains("cbBookSource.applyTint"))
        assertTrue(adapter.contains("R.color.xuanjuan_gold_soft), true"))
        assertTrue(activity.contains("R.id.cb_selected_all"))
        assertTrue(activity.contains("R.color.xuanjuan_gold_soft), true"))
    }

    @Test
    fun bookSourceCardsKeepRoundedSurfaceAndExposeUsefulSecondaryInfo() {
        val activity = projectFile(
            "src/main/java/io/legado/app/ui/book/source/manage/BookSourceActivity.kt"
        ).readText()
        val adapter = projectFile(
            "src/main/java/io/legado/app/ui/book/source/manage/BookSourceAdapter.kt"
        ).readText()
        val layout = projectFile("src/main/res/layout/item_book_source.xml").readText()

        assertFalse(activity.contains("VerticalDivider"))
        assertFalse(adapter.contains("root.setBackgroundColor"))
        assertTrue(adapter.contains("tvSourceUrl.text = item.bookSourceUrl"))
        assertTrue(layout.contains("@+id/tv_source_url"))
        assertTrue(layout.contains("@+id/selection_accent"))
        assertTrue(layout.contains("@drawable/novel_helper_preference_card"))
    }

    @Test
    fun themeSettingsReuseXuanjuanCardsWithoutChangingPreferenceBehavior() {
        val fragment = projectFile(
            "src/main/java/io/legado/app/ui/config/ThemeConfigFragment.kt"
        ).readText()
        val preferenceBase = projectFile(
            "src/main/java/io/legado/app/lib/prefs/fragment/PreferenceFragment.kt"
        ).readText()
        val preferenceXml = projectFile("src/main/res/xml/pref_config_theme.xml").readText()
        val row = projectFile("src/main/res/layout/novel_helper_config_preference.xml").readText()

        assertTrue(fragment.contains("applyXuanjuanPreferenceCards(preferenceScreen)"))
        assertTrue(preferenceBase.contains("R.layout.novel_helper_config_preference"))
        assertTrue(preferenceBase.contains("R.layout.novel_helper_preference_category"))
        assertTrue(preferenceXml.contains("android:divider=\"@color/transparent\""))
        assertTrue(row.contains("@drawable/novel_helper_preference_card"))
        assertTrue(row.contains("@+id/preference_title"))
        assertTrue(row.contains("@+id/preference_desc"))
        assertTrue(row.contains("@+id/preference_widget"))
        assertFalse(row.contains("@+id/preference_icon"))
    }

    @Test
    fun otherSettingsReuseTheSameXuanjuanConfigCards() {
        val fragment = projectFile(
            "src/main/java/io/legado/app/ui/config/OtherConfigFragment.kt"
        ).readText()
        val preferenceXml = projectFile("src/main/res/xml/pref_config_other.xml").readText()
        val preferenceBase = projectFile(
            "src/main/java/io/legado/app/lib/prefs/fragment/PreferenceFragment.kt"
        ).readText()

        assertTrue(fragment.contains("applyXuanjuanPreferenceCards(preferenceScreen)"))
        assertTrue(preferenceBase.contains("protected fun applyXuanjuanPreferenceCards"))
        assertTrue(preferenceXml.contains("android:divider=\"@color/transparent\""))
        assertTrue(preferenceXml.contains("app:allowDividerBelow=\"false\""))
    }

    @Test
    fun backupSettingsReuseXuanjuanCardsWithoutChangingBackupActions() {
        val fragment = projectFile(
            "src/main/java/io/legado/app/ui/config/BackupConfigFragment.kt"
        ).readText()
        val preferenceXml = projectFile("src/main/res/xml/pref_config_backup.xml").readText()

        assertTrue(fragment.contains("applyXuanjuanPreferenceCards(preferenceScreen)"))
        assertTrue(fragment.contains("PreferKey.backupPath -> showBackupPathSelector()"))
        assertTrue(fragment.contains("\"web_dav_backup\" -> backup()"))
        assertTrue(fragment.contains("\"web_dav_restore\" -> restore()"))
        assertTrue(fragment.contains("\"lan_backup_transfer\" -> lanBackupTransfer()"))
        assertTrue(preferenceXml.contains("android:divider=\"@color/transparent\""))
        assertTrue(preferenceXml.contains("app:allowDividerBelow=\"false\""))
    }

    @Test
    fun coverSettingsReuseXuanjuanCardsWithoutChangingCoverActions() {
        val fragment = projectFile(
            "src/main/java/io/legado/app/ui/config/CoverConfigFragment.kt"
        ).readText()
        val preferenceXml = projectFile("src/main/res/xml/pref_config_cover.xml").readText()

        assertTrue(fragment.contains("applyXuanjuanPreferenceCards(preferenceScreen)"))
        assertTrue(fragment.contains("\"coverRule\" -> showDialogFragment(CoverRuleConfigDialog())"))
        assertTrue(fragment.contains("PreferKey.defaultCover ->"))
        assertTrue(fragment.contains("PreferKey.defaultCoverDark ->"))
        assertTrue(fragment.contains("findPreference<SwitchPreference>(PreferKey.coverShowAuthor)"))
        assertTrue(preferenceXml.contains("android:divider=\"@color/transparent\""))
        assertTrue(preferenceXml.contains("app:allowDividerBelow=\"false\""))
    }

    @Test
    fun welcomeSettingsUseXuanjuanCardsButKeepSpecializedSeekBarLayout() {
        val fragment = projectFile(
            "src/main/java/io/legado/app/ui/config/WelcomeConfigFragment.kt"
        ).readText()
        val preferenceXml = projectFile("src/main/res/xml/pref_config_welcome.xml").readText()
        val seekBarLayout = projectFile(
            "src/main/res/layout/view_preference_seekbar.xml"
        ).readText()

        assertTrue(fragment.contains("applyXuanjuanPreferenceCards(preferenceScreen)"))
        assertTrue(fragment.contains("findPreference<SeekBarPreference>(PreferKey.welcomeShowTime)"))
        assertTrue(fragment.contains("R.layout.view_preference_seekbar"))
        assertTrue(fragment.contains("PreferKey.welcomeImage ->"))
        assertTrue(fragment.contains("PreferKey.welcomeImageDark ->"))
        assertTrue(preferenceXml.contains("android:divider=\"@color/transparent\""))
        assertTrue(preferenceXml.contains("app:allowDividerBelow=\"false\""))
        assertTrue(seekBarLayout.contains("@drawable/novel_helper_preference_card"))
        assertTrue(seekBarLayout.contains("@+id/seek_bar"))
        assertTrue(seekBarLayout.contains("@+id/iv_seek_reduce"))
        assertTrue(seekBarLayout.contains("@+id/iv_seek_plus"))
        assertTrue(seekBarLayout.contains("@+id/tv_seek_value"))
        assertTrue(seekBarLayout.contains("@color/xuanjuan_gold_soft"))
    }

    @Test
    fun readerMoreSettingsReuseXuanjuanCardsWithoutChangingReaderActions() {
        val dialog = projectFile(
            "src/main/java/io/legado/app/ui/book/read/config/MoreConfigDialog.kt"
        ).readText()
        val preferenceXml = projectFile("src/main/res/xml/pref_config_read.xml").readText()

        assertTrue(dialog.contains("applyXuanjuanPreferenceCards(preferenceScreen)"))
        assertTrue(dialog.contains("\"customPageKey\" -> PageKeyDialog(requireContext()).show()"))
        assertTrue(dialog.contains("PreferKey.pageTouchSlop ->"))
        assertTrue(dialog.contains("PreferKey.pullBookmarkDistance ->"))
        assertTrue(dialog.contains("PreferKey.optimizeRender ->"))
        assertTrue(preferenceXml.contains("android:divider=\"@color/transparent\""))
        assertTrue(preferenceXml.contains("app:allowDividerBelow=\"false\""))
    }

    @Test
    fun readAloudSettingsReuseXuanjuanCardsWithoutChangingTtsActions() {
        val dialog = projectFile(
            "src/main/java/io/legado/app/ui/book/read/config/ReadAloudConfigDialog.kt"
        ).readText()
        val preferenceXml = projectFile("src/main/res/xml/pref_config_aloud.xml").readText()

        assertTrue(dialog.contains("applyXuanjuanPreferenceCards(preferenceScreen)"))
        assertTrue(dialog.contains("PreferKey.ttsEngine -> showDialogFragment(SpeakEngineDialog())"))
        assertTrue(dialog.contains("\"sysTtsConfig\" -> IntentHelp.openTTSSetting()"))
        assertTrue(dialog.contains("PreferKey.ignoreAudioFocus ->"))
        assertTrue(dialog.contains("BaseReadAloudService.isRun"))
        assertTrue(preferenceXml.contains("android:divider=\"@color/transparent\""))
        assertTrue(preferenceXml.contains("app:allowDividerBelow=\"false\""))
    }

    @Test
    fun bottomBarSkinTilesKeepXuanjuanSurfaceAndExistingSkinActions() {
        val activity = projectFile(
            "src/main/java/io/legado/app/ui/config/BottomBarSkinActivity.kt"
        ).readText()
        val item = projectFile("src/main/res/layout/item_bottom_bar_skin.xml").readText()

        assertTrue(item.contains("@drawable/novel_helper_preference_card"))
        assertTrue(activity.contains("R.color.xuanjuan_gold_soft"))
        assertTrue(activity.contains("R.drawable.novel_helper_preference_card"))
        assertTrue(activity.contains("BottomBarSkinManager.active ="))
        assertTrue(activity.contains("showItemMenu(item.name)"))
        assertTrue(activity.contains("exportSkin(name)"))
        assertTrue(activity.contains("shareSkin(name)"))
        assertTrue(activity.contains("confirmDelete(name)"))
    }

    @Test
    fun bookshelfManagementUsesCardRowsWithoutChangingManagementActions() {
        val activity = projectFile(
            "src/main/java/io/legado/app/ui/book/manage/BookshelfManageActivity.kt"
        ).readText()
        val adapter = projectFile(
            "src/main/java/io/legado/app/ui/book/manage/BookAdapter.kt"
        ).readText()
        val page = projectFile("src/main/res/layout/activity_arrange_book.xml").readText()
        val row = projectFile("src/main/res/layout/item_arrange_book.xml").readText()

        assertFalse(activity.contains("VerticalDivider"))
        assertFalse(adapter.contains("root.setBackgroundColor"))
        assertTrue(adapter.contains("selectedBookUrls"))
        assertTrue(adapter.contains("callBack.selectGroup(groupRequestCode, it.group)"))
        assertTrue(adapter.contains("callBack.deleteBook(it)"))
        assertTrue(adapter.contains("swapItem(srcPosition, targetPosition)"))
        assertTrue(page.contains("android:background=\"@color/background\""))
        assertTrue(page.contains("android:clipToPadding=\"false\""))
        assertTrue(row.contains("@drawable/novel_helper_preference_card"))
        assertTrue(row.contains("android:clipToOutline=\"true\""))
        assertTrue(row.contains("android:elevation=\"1dp\""))
    }

    @Test
    fun fileManagerUsesXuanjuanCardsWithoutChangingFileActions() {
        val activity = projectFile(
            "src/main/java/io/legado/app/ui/file/FileManageActivity.kt"
        ).readText()
        val fileRow = projectFile("src/main/res/layout/item_file.xml").readText()
        val pathRow = projectFile("src/main/res/layout/item_path_picker.xml").readText()

        assertFalse(activity.contains("VerticalDivider"))
        assertTrue(activity.contains("openFileUri"))
        assertTrue(activity.contains("showFileMenu(view, item)"))
        assertTrue(fileRow.contains("@drawable/novel_helper_preference_card"))
        assertTrue(fileRow.contains("@color/xuanjuan_gold_soft"))
        assertTrue(fileRow.contains("@+id/image_view"))
        assertTrue(fileRow.contains("@+id/text_view"))
        assertTrue(pathRow.contains("@drawable/novel_helper_section_outline"))
        assertTrue(pathRow.contains("@color/xuanjuan_gold_soft"))
        assertTrue(pathRow.contains("@+id/image_view"))
        assertTrue(pathRow.contains("@+id/text_view"))
    }

    @Test
    fun bookmarkPageShowsXuanjuanEmptyStateWithoutChangingBookmarkActions() {
        val activity = projectFile(
            "src/main/java/io/legado/app/ui/book/bookmark/AllBookmarkActivity.kt"
        ).readText()
        val layout = projectFile("src/main/res/layout/activity_all_bookmark.xml").readText()

        assertTrue(activity.contains("binding.tvEmptyMsg.isVisible = it.isEmpty()"))
        assertTrue(activity.contains("startActivityForBook"))
        assertTrue(activity.contains("showDialogFragment(BookmarkDialog"))
        assertTrue(layout.contains("@+id/tv_empty_msg"))
        assertTrue(layout.contains("@drawable/novel_helper_state_card"))
        assertTrue(layout.contains("android:layout_width=\"280dp\""))
        assertTrue(layout.contains("android:padding=\"20dp\""))
    }

    @Test
    fun aboutPreferencesReuseXuanjuanCardsWithoutChangingAboutActions() {
        val fragment = projectFile(
            "src/main/java/io/legado/app/ui/about/AboutFragment.kt"
        ).readText()
        val preferenceXml = projectFile("src/main/res/xml/about.xml").readText()

        assertTrue(fragment.contains("class AboutFragment : PreferenceFragment()"))
        assertTrue(fragment.contains("applyXuanjuanPreferenceCards(preferenceScreen)"))
        assertTrue(fragment.contains("\"check_update\" -> checkUpdate()"))
        assertTrue(fragment.contains("\"crashLog\" -> showDialogFragment<CrashLogsDialog>()"))
        assertTrue(fragment.contains("\"license\" -> showMdFile"))
        assertTrue(fragment.contains("\"privacyPolicy\" -> showMdFile"))
        assertTrue(preferenceXml.contains("android:divider=\"@color/transparent\""))
        assertTrue(preferenceXml.contains("app:allowDividerBelow=\"false\""))
    }

    @Test
    fun ruleManagementPagesUseXuanjuanCardsAndKeepRuleActions() {
        val cases = listOf(
            Triple(
                "src/main/java/io/legado/app/ui/autoTask/AutoTaskActivity.kt",
                "src/main/res/layout/activity_auto_task.xml",
                "src/main/res/layout/item_auto_task.xml"
            ),
            Triple(
                "src/main/java/io/legado/app/ui/book/toc/rule/TxtTocRuleActivity.kt",
                "src/main/res/layout/activity_txt_toc_rule.xml",
                "src/main/res/layout/item_txt_toc_rule.xml"
            ),
            Triple(
                "src/main/java/io/legado/app/ui/replace/ReplaceRuleActivity.kt",
                "src/main/res/layout/activity_replace_rule.xml",
                "src/main/res/layout/item_replace_rule.xml"
            ),
            Triple(
                "src/main/java/io/legado/app/ui/dict/rule/DictRuleActivity.kt",
                "src/main/res/layout/activity_dict_rule.xml",
                "src/main/res/layout/item_dict_rule.xml"
            )
        )

        cases.forEach { (activityPath, layoutPath, itemPath) ->
            val activity = projectFile(activityPath).readText()
            val layout = projectFile(layoutPath).readText()
            val item = projectFile(itemPath).readText()
            assertFalse(activity.contains("VerticalDivider"))
            assertTrue(layout.contains("android:clipToPadding=\"false\""))
            assertTrue(layout.contains("android:paddingHorizontal=\"8dp\""))
            assertTrue(item.contains("@drawable/novel_helper_preference_card"))
            assertTrue(item.contains("android:clipToOutline=\"true\""))
            assertTrue(item.contains("@color/xuanjuan_gold_soft"))
        }

        val autoTask = projectFile(
            "src/main/java/io/legado/app/ui/autoTask/AutoTaskActivity.kt"
        ).readText()
        val txt = projectFile(
            "src/main/java/io/legado/app/ui/book/toc/rule/TxtTocRuleActivity.kt"
        ).readText()
        val replace = projectFile(
            "src/main/java/io/legado/app/ui/replace/ReplaceRuleActivity.kt"
        ).readText()
        val dict = projectFile(
            "src/main/java/io/legado/app/ui/dict/rule/DictRuleActivity.kt"
        ).readText()

        assertTrue(autoTask.contains("binding.tvEmpty.isVisible = filtered.isEmpty()"))
        assertTrue(txt.contains("findViewById<android.view.View>(R.id.tv_empty).isVisible = filtered.isEmpty()"))
        assertTrue(replace.contains("findViewById<android.view.View>(R.id.tv_empty).isVisible = it.isEmpty()"))
        assertTrue(dict.contains("findViewById<android.view.View>(R.id.tv_empty).isVisible = it.isEmpty()"))
        assertTrue(autoTask.contains("AutoTask.updateEnabled(ids, enabled"))
        assertTrue(txt.contains("viewModel.enableSelection"))
        assertTrue(replace.contains("viewModel.enableSelection(adapter.selection)"))
        assertTrue(dict.contains("viewModel.enableSelection"))

        listOf(
            "src/main/java/io/legado/app/ui/book/toc/rule/TxtTocRuleAdapter.kt",
            "src/main/java/io/legado/app/ui/replace/ReplaceRuleAdapter.kt",
            "src/main/java/io/legado/app/ui/dict/rule/DictRuleAdapter.kt"
        ).forEach { adapterPath ->
            assertFalse(projectFile(adapterPath).readText().contains("root.setBackgroundColor"))
        }
    }

    @Test
    fun remoteServerPickerUsesXuanjuanCardsAndKeepsServerActions() {
        val dialog = projectFile(
            "src/main/java/io/legado/app/ui/book/import/remote/ServersDialog.kt"
        ).readText()
        val row = projectFile("src/main/res/layout/item_server_select.xml").readText()

        assertFalse(dialog.contains("VerticalDivider"))
        assertFalse(dialog.contains("root.setBackgroundColor(context.backgroundColor)"))
        assertTrue(dialog.contains("selectServerId = getItemByLayoutPosition(holder.layoutPosition)!!.id"))
        assertTrue(dialog.contains("showDialogFragment(ServerConfigDialog(server.id))"))
        assertTrue(dialog.contains("viewModel.delete(server)"))
        assertTrue(dialog.contains("AppConfig.remoteServerId = DEFAULT_WEBDAV_ID"))
        assertTrue(dialog.contains("AppConfig.remoteServerId = adapter.selectServerId"))
        assertTrue(row.contains("@drawable/novel_helper_preference_card"))
        assertTrue(row.contains("android:clipToOutline=\"true\""))
        assertTrue(row.contains("android:elevation=\"1dp\""))
        assertTrue(row.contains("android:layout_marginHorizontal=\"8dp\""))
        assertTrue(row.split("@color/xuanjuan_gold_soft").size - 1 >= 2)
    }

    @Test
    fun txtTocRulePickerUsesXuanjuanCardsAndKeepsSelectionActions() {
        val dialog = projectFile(
            "src/main/java/io/legado/app/ui/book/toc/rule/TxtTocRuleDialog.kt"
        ).readText()
        val page = projectFile("src/main/res/layout/dialog_toc_regex.xml").readText()
        val row = projectFile("src/main/res/layout/item_toc_regex.xml").readText()

        assertFalse(dialog.contains("VerticalDivider"))
        assertFalse(dialog.contains("root.setBackgroundColor(context.backgroundColor)"))
        assertTrue(dialog.contains("selectedName = getItem(holder.layoutPosition)?.name"))
        assertTrue(dialog.contains("it.enable = isChecked"))
        assertTrue(dialog.contains("showDialogFragment(TxtTocRuleEditDialog(getItem(holder.layoutPosition)?.id))"))
        assertTrue(dialog.contains("viewModel.del(item)"))
        assertTrue(dialog.contains("item.serialNumber = index + 1"))
        assertTrue(dialog.contains("callBack?.onTocRegexDialogResult"))
        assertTrue(page.contains("android:clipToPadding=\"false\""))
        assertTrue(page.contains("android:paddingHorizontal=\"8dp\""))
        assertTrue(row.contains("@drawable/novel_helper_preference_card"))
        assertTrue(row.contains("android:clipToOutline=\"true\""))
        assertTrue(row.contains("android:elevation=\"1dp\""))
        assertTrue(row.contains("@color/tv_text_summary"))
        assertTrue(row.split("@color/xuanjuan_gold_soft").size - 1 >= 2)
    }

    @Test
    fun highlightRuleManagementUsesXuanjuanCardsAndKeepsRuleActions() {
        val activity = projectFile(
            "src/main/java/io/legado/app/ui/highlight/HighlightRuleActivity.kt"
        ).readText()
        val adapter = projectFile(
            "src/main/java/io/legado/app/ui/highlight/HighlightRuleAdapter.kt"
        ).readText()
        val page = projectFile("src/main/res/layout/activity_highlight_rule.xml").readText()
        val row = projectFile("src/main/res/layout/item_highlight_rule.xml").readText()

        assertFalse(activity.contains("VerticalDivider"))
        assertTrue(activity.contains("viewModel.enableSelection(selection, true)"))
        assertTrue(activity.contains("viewModel.delete(*adapter.selection.toTypedArray())"))
        assertTrue(activity.contains("showDialogFragment(HighlightRuleEditDialog.edit(rule.id))"))
        assertTrue(adapter.contains("swapItem(srcPosition, targetPosition)"))
        assertTrue(adapter.contains("binding.swtEnabled.setOnUserCheckedChangeListener"))
        assertTrue(page.contains("android:padding=\"12dp\""))
        assertTrue(page.contains("@drawable/novel_helper_state_card"))
        assertTrue(row.contains("@drawable/novel_helper_preference_card"))
        assertTrue(row.contains("android:clipToOutline=\"true\""))
        assertTrue(row.contains("android:elevation=\"1dp\""))
        assertTrue(row.split("@color/xuanjuan_gold_soft").size - 1 >= 2)
    }

    @Test
    fun rssSourceManagementUsesXuanjuanCardsWithoutChangingSourceActions() {
        val activity = projectFile(
            "src/main/java/io/legado/app/ui/rss/source/manage/RssSourceActivity.kt"
        ).readText()
        val adapter = projectFile(
            "src/main/java/io/legado/app/ui/rss/source/manage/RssSourceAdapter.kt"
        ).readText()
        val page = projectFile("src/main/res/layout/activity_rss_source.xml").readText()
        val row = projectFile("src/main/res/layout/item_rss_source.xml").readText()

        assertFalse(activity.contains("VerticalDivider"))
        assertFalse(adapter.contains("root.setBackgroundColor"))
        assertTrue(activity.contains("DragSelectTouchHelper(adapter.dragSelectCallback)"))
        assertTrue(activity.contains("itemTouchCallback.isCanDrag = true"))
        assertTrue(adapter.contains("swtEnabled.setOnUserCheckedChangeListener"))
        assertTrue(adapter.contains("cbSource.setOnUserCheckedChangeListener"))
        assertTrue(adapter.contains("callBack.edit(it)"))
        assertTrue(adapter.contains("showMenu(ivMenuMore, holder.layoutPosition)"))
        assertTrue(page.contains("android:background=\"@color/background\""))
        assertTrue(page.contains("android:clipToPadding=\"false\""))
        assertTrue(row.contains("@drawable/novel_helper_preference_card"))
        assertTrue(row.contains("android:clipToOutline=\"true\""))
        assertTrue(row.contains("android:elevation=\"1dp\""))
        assertTrue(row.split("@color/xuanjuan_gold_soft").size - 1 >= 2)
    }

    @Test
    fun rssArticleAndFavoritesListsUseXuanjuanCardsWithoutChangingReadingActions() {
        val articles = projectFile(
            "src/main/java/io/legado/app/ui/rss/article/RssArticlesFragment.kt"
        ).readText()
        val favorites = projectFile(
            "src/main/java/io/legado/app/ui/rss/favorites/RssFavoritesFragment.kt"
        ).readText()
        val page = projectFile("src/main/res/layout/fragment_rss_articles.xml").readText()
        val row = projectFile("src/main/res/layout/item_rss_article.xml").readText()

        assertFalse(articles.contains("VerticalDivider"))
        assertFalse(favorites.contains("VerticalDivider"))
        assertTrue(articles.contains("RssArticlesAdapter1"))
        assertTrue(articles.contains("RssArticlesAdapter2"))
        assertTrue(articles.contains("RssArticlesAdapter3"))
        assertTrue(articles.contains("RssArticlesAdapter4"))
        assertTrue(articles.contains("ReadRss.readRss(this, rssArticle, activityViewModel.rssSource)"))
        assertTrue(articles.contains("viewModel.loadMore(rssSource)"))
        assertTrue(favorites.contains("ReadRss.readRss(this, rssStar.toRssArticle())"))
        assertTrue(favorites.contains("appDb.rssStarDao.delete(rssStar.origin, rssStar.link)"))
        assertTrue(page.contains("android:background=\"@color/background\""))
        assertTrue(row.contains("@drawable/novel_helper_preference_card"))
        assertTrue(row.contains("android:clipToOutline=\"true\""))
        assertTrue(row.contains("android:elevation=\"1dp\""))
        assertTrue(row.contains("@color/tv_text_summary"))
    }

    @Test
    fun rssReadRecordDialogUsesXuanjuanCardsWithoutChangingRecordActions() {
        val dialog = projectFile(
            "src/main/java/io/legado/app/ui/rss/article/ReadRecordDialog.kt"
        ).readText()
        val row = projectFile("src/main/res/layout/item_rss_read_record.xml").readText()

        assertTrue(dialog.contains("ReadRss.readRss(activity as AppCompatActivity, it)"))
        assertTrue(dialog.contains("viewModel.deleteAllRecord(origin)"))
        assertTrue(dialog.contains("adapter.clearItems()"))
        assertTrue(dialog.contains("binding.textTitle.setOnClickListener"))
        assertTrue(row.contains("@drawable/novel_helper_preference_card"))
        assertTrue(row.contains("android:clipToOutline=\"true\""))
        assertTrue(row.contains("android:elevation=\"1dp\""))
        assertTrue(row.contains("android:foreground=\"?android:attr/selectableItemBackground\""))
        assertTrue(row.contains("@color/tv_text_summary"))
    }

    @Test
    fun tintedDialogsUseThemeAwareXuanjuanSurfaceWithoutBreakingEInkDialogs() {
        val dialogExtensions = projectFile(
            "src/main/java/io/legado/app/utils/DialogExtensions.kt"
        ).readText()
        val materialValues = projectFile(
            "src/main/java/io/legado/app/lib/theme/MaterialValueHelper.kt"
        ).readText()
        val alertBuilder = projectFile(
            "src/main/java/io/legado/app/lib/dialogs/AndroidAlertBuilder.kt"
        ).readText()

        assertTrue(materialValues.contains("val Context.xuanjuanDialogBackground"))
        assertTrue(materialValues.contains("background.setColor(bottomBackground)"))
        assertTrue(materialValues.contains("R.color.xuanjuan_gold_outline"))
        assertTrue(dialogExtensions.contains("context.xuanjuanDialogBackground"))
        assertTrue(dialogExtensions.contains("R.drawable.bg_eink_border_dialog"))
        assertTrue(alertBuilder.contains("if (AppConfig.isEInkMode)"))
        assertTrue(alertBuilder.contains("R.drawable.bg_eink_border_dialog"))
        assertFalse(alertBuilder.contains("R.drawable.xuanjuan_reader_dialog_bg"))
    }

    private fun projectFile(path: String): File = sequenceOf(File(path), File("app/$path"))
        .first(File::isFile)
}
