package com.fintech.lxf.ui.activity.init

import com.fintech.lxf.R
import com.fintech.lxf.ui.widget.Label
import com.lxf.recyclerhelper.BaseQuickAdapter
import com.lxf.recyclerhelper.BaseViewHolder


class SingleAmountAdapter(layoutId: Int, data: List<String>? = null) : BaseQuickAdapter<String, BaseViewHolder>(layoutId, data) {
    override fun convert(holder: BaseViewHolder?, item: String?) {
        holder?.getView<Label>(R.id.label)?.apply {
            text = item ?: ""
            onRemoveListener = object : Label.OnRemoveListener {
                override fun onRemove(content: String) {
                    data.remove(content)
                    notifyDataSetChanged()
                }
            }
        }
    }
}