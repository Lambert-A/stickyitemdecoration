/*
 * Copyright (C) 2021 panpf <panpfpanpf@outlook.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.panpf.recycler.sticky.sample.item

import android.annotation.SuppressLint
import android.content.Context
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import com.github.panpf.assemblyadapter.BindingItemFactory
import com.github.panpf.recycler.sticky.sample.R
import com.github.panpf.recycler.sticky.sample.bean.AppInfo
import com.github.panpf.recycler.sticky.sample.databinding.ItemAppHorizontalBinding
import me.panpf.sketch.shaper.RoundRectImageShaper
import me.panpf.sketch.uri.AppIconUriModel

class AppHorizontalItemFactory : BindingItemFactory<AppInfo, ItemAppHorizontalBinding>(AppInfo::class) {

    override fun createItemViewBinding(
        context: Context, inflater: LayoutInflater, parent: ViewGroup
    ): ItemAppHorizontalBinding {
        return ItemAppHorizontalBinding.inflate(inflater, parent, false)
    }

    override fun initItem(
        context: Context, binding: ItemAppHorizontalBinding, item: BindingItem<AppInfo, ItemAppHorizontalBinding>
    ) {
        binding.root.setOnClickListener {
            val data = item.dataOrThrow
            val launchIntent =
                context.packageManager.getLaunchIntentForPackage(data.packageName)
            if (launchIntent != null) {
                context.startActivity(launchIntent)
            }
        }

        binding.appHorizontalItemIconImage.options.shaper =
            RoundRectImageShaper(context.resources.getDimension(R.dimen.app_icon_corner_radius)).apply {
                setStroke(
                    ResourcesCompat.getColor(context.resources, R.color.app_icon_stroke, null),
                    context.resources.getDimensionPixelSize(R.dimen.app_icon_stroke_width)
                )
            }
    }

    @SuppressLint("SetTextI18n")
    override fun bindItemData(
        context: Context,
        binding: ItemAppHorizontalBinding,
        item: BindingItem<AppInfo, ItemAppHorizontalBinding>,
        bindingAdapterPosition: Int,
        absoluteAdapterPosition: Int,
        data: AppInfo
    ) {
        val appIconUri = AppIconUriModel.makeUri(data.packageName, data.versionCode)
        binding.appHorizontalItemIconImage.displayImage(appIconUri)
        binding.appHorizontalItemNameText.text = data.name
        binding.appHorizontalItemVersionText.text = "v${data.versionName}"
        binding.appHorizontalItemSizeText.text = Formatter.formatFileSize(context, data.apkSize)
    }
}
