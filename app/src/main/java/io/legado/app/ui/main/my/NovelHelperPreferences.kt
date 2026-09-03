package io.legado.app.ui.main.my

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.SwitchCompat
import androidx.preference.PreferenceViewHolder
import io.legado.app.R
import io.legado.app.lib.prefs.NameListPreference
import io.legado.app.lib.prefs.Preference
import io.legado.app.lib.prefs.PreferenceCategory
import io.legado.app.lib.prefs.SwitchPreference
import io.legado.app.utils.applyTint

class NovelHelperPreference(context: Context, attrs: AttributeSet) :
    Preference(context, attrs) {
    init {
        layoutResource = R.layout.novel_helper_preference
    }
}

class NovelHelperSwitchPreference(context: Context, attrs: AttributeSet) :
    SwitchPreference(context, attrs) {
    init {
        layoutResource = R.layout.novel_helper_preference
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        holder.findViewById(androidx.preference.R.id.switchWidget)
            ?.let { it as? SwitchCompat }
            ?.applyTint(context.getColor(R.color.xuanjuan_gold_soft))
    }
}

class NovelHelperNameListPreference(context: Context, attrs: AttributeSet) :
    NameListPreference(context, attrs) {
    init {
        layoutResource = R.layout.novel_helper_preference
        widgetLayoutResource = R.layout.novel_helper_preference_value
    }
}

class NovelHelperPreferenceCategory(context: Context, attrs: AttributeSet) :
    PreferenceCategory(context, attrs) {
    init {
        layoutResource = R.layout.novel_helper_preference_category
    }
}
