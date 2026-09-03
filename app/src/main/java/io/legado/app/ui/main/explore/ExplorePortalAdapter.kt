package io.legado.app.ui.main.explore

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.legado.app.R
import io.legado.app.base.adapter.ItemViewHolder
import io.legado.app.base.adapter.RecyclerAdapter
import io.legado.app.databinding.ItemExplorePortalBinding

data class ExplorePortal(
    val sourceUrl: String,
    val sourceName: String,
    val title: String,
    val exploreUrl: String,
    val aggregate: Boolean = false,
)

class ExplorePortalAdapter(
    context: Context,
    private val onPortalClick: (ExplorePortal) -> Unit,
) : RecyclerAdapter<ExplorePortal, ItemExplorePortalBinding>(context) {

    override fun getViewBinding(parent: ViewGroup): ItemExplorePortalBinding {
        return ItemExplorePortalBinding.inflate(inflater, parent, false)
    }

    override fun convert(
        holder: ItemViewHolder,
        binding: ItemExplorePortalBinding,
        item: ExplorePortal,
        payloads: MutableList<Any>,
    ) {
        binding.tvBadge.text = context.getString(
            if (item.aggregate) R.string.xuanjuan_aggregate_badge else R.string.xuanjuan_explore_badge
        )
        binding.tvTitle.text = item.title
        binding.tvSource.text = if (item.aggregate) {
            item.sourceName
        } else {
            context.getString(R.string.xuanjuan_explore_source, item.sourceName)
        }
    }

    override fun registerListener(holder: ItemViewHolder, binding: ItemExplorePortalBinding) {
        binding.root.setOnClickListener {
            val position = holder.bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                getItem(position)?.let(onPortalClick)
            }
        }
    }

    fun compressExplore(): Boolean = false

    fun onPause() = Unit
}
