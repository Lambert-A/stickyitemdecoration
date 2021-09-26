package com.github.panpf.recycler.sticky.internal

import android.graphics.Canvas
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.github.panpf.recycler.sticky.BaseStickyItemDecoration

class ContainerStickyItemDraw(
    baseStickyItemDecoration: BaseStickyItemDecoration,
    private val stickyItemContainer: ViewGroup
) : BaseStickyItemDraw(baseStickyItemDecoration) {

    private var lastStickyItemPosition: Int? = null
    private var lastStickyItemView: View? = null

    init {
        stickyItemContainer.isClickable = true
    }

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val adapter = getAdapter(parent)?.takeIf { it.itemCount > 0 } ?: return
        val firstVisibleItemPosition = findFirstVisibleItemPosition(parent) ?: return

        val logBuilder: StringBuilder? =
            if (BaseStickyItemDecoration.debugMode) StringBuilder() else null
        logBuilder?.append("onDraw. FirstItem: $firstVisibleItemPosition")

        val stickyItemPosition = findStickyItemPositionBackward(firstVisibleItemPosition)
        if (stickyItemPosition != null) {
            logBuilder?.append(". StickyItem=$stickyItemPosition")
            val stickyItemView = showStickyItemView(stickyItemPosition, adapter, logBuilder)
            offsetStickyItemView(
                parent, firstVisibleItemPosition, stickyItemPosition, stickyItemView, logBuilder
            )
        } else {
            lastStickyItemPosition = null
            lastStickyItemView = null
            stickyItemContainer.apply {
                if (childCount > 0) {
                    removeAllViews()
                }
                visibility = View.INVISIBLE
            }
            logBuilder?.append(". NoStickyItem")
        }
        hiddenOriginItemView(parent, firstVisibleItemPosition, stickyItemPosition)

        logBuilder?.apply {
            Log.d("ContainerStickyItemDraw", this.toString())
        }
    }

    private fun showStickyItemView(
        stickItemPosition: Int,
        adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>,
        logBuilder: StringBuilder?
    ): View {
        val lastStickyItemView = lastStickyItemView
        val lastStickItemPosition = lastStickyItemPosition
        return if (lastStickyItemView == null || stickItemPosition != lastStickItemPosition) {
            val stickyItemType = adapter.getItemViewType(stickItemPosition)
            val stickyItemViewHolder = viewHolderCachePool[stickyItemType]
                ?: adapter.createViewHolder(stickyItemContainer, stickyItemType).apply {
                    viewHolderCachePool.put(stickyItemType, this)
                }
            adapter.bindViewHolder(stickyItemViewHolder, stickItemPosition)
            val stickyItemView = stickyItemViewHolder.itemView
            stickyItemContainer.apply {
                if (childCount > 0) {
                    removeAllViews()
                }
                addView(stickyItemView)
                visibility = View.VISIBLE
            }

            this@ContainerStickyItemDraw.lastStickyItemPosition = stickItemPosition
            this@ContainerStickyItemDraw.lastStickyItemView = stickyItemView
            logBuilder?.append(". New")
            stickyItemView
        } else {
            logBuilder?.append(". NoChange")
            lastStickyItemView
        }
    }

    private fun offsetStickyItemView(
        parent: RecyclerView,
        firstVisibleItemPosition: Int,
        stickyItemPosition: Int,
        stickyItemView: View,
        logBuilder: StringBuilder?
    ) {
        if (disabledScrollUpStickyItem) {
            logBuilder?.append(". DisabledOffset")
            return
        }

        val nextStickItemPosition =
            findStickyItemPositionForward(parent, firstVisibleItemPosition)
        if (nextStickItemPosition == null || nextStickItemPosition <= stickyItemPosition) {
            logBuilder?.append(". NoNextStickyItem")
            return
        }

        val nextStickyItemView =
            parent.layoutManager?.findViewByPosition(nextStickItemPosition)
        if (nextStickyItemView == null) {
            logBuilder?.append(". NoNextStickyItemView")
            return
        }

        val nextStickyViewTop = nextStickyItemView.top
        val stickyContainerHeight = stickyItemContainer.height
        val stickyItemViewOffset = if (nextStickyViewTop in 0..stickyContainerHeight) {
            nextStickyViewTop - stickyContainerHeight
        } else {
            0
        }
        val oldStickyViewTop = stickyItemView.top
        ViewCompat.offsetTopAndBottom(stickyItemView, stickyItemViewOffset - oldStickyViewTop)

        logBuilder?.append(". Offset=${stickyItemViewOffset}")
    }

    override fun reset() {
        super.reset()
        lastStickyItemView = null
        lastStickyItemPosition = null
        if (stickyItemContainer.childCount > 0) {
            stickyItemContainer.removeAllViews()
        }
        stickyItemContainer.visibility = View.INVISIBLE
    }
}