package com.fintech.lxf.ui.activity.init

import android.graphics.Color
import com.fintech.lxf.R
import com.fintech.lxf.bean.MoreUsedBean
import com.fintech.lxf.ui.widget.Label
import com.lxf.recyclerhelper.BaseQuickAdapter
import com.lxf.recyclerhelper.BaseViewHolder


class MoreUsedAdapter(layoutId: Int, data: List<MoreUsedBean>? = null) : BaseQuickAdapter<MoreUsedBean, BaseViewHolder>(layoutId, data) {
    override fun convert(holder: BaseViewHolder?, item: MoreUsedBean?) {
        holder?.getView<Label>(R.id.label)?.apply {
            text = item?.amount.toString()
            closeEnabled = false
            setBackgroundColor(if (item?.complete == true) Color.parseColor("#818181") else Color.parseColor("#B8DB6D"))
        }
    }
}