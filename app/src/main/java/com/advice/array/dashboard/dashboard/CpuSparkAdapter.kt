package com.advice.array.dashboard.dashboard

import android.graphics.RectF
import com.robinhood.spark.SparkAdapter


class CpuSparkAdapter : SparkAdapter() {

    private val yData = ArrayList<Int>()

    override fun getCount() = LENGTH

    override fun getItem(index: Int): Any {
        val paddingCount = LENGTH - yData.size
        if (index - paddingCount < 0)
            return 0
        return yData[index - paddingCount]
    }

    override fun getY(index: Int): Float {
        val item = getItem(index) as Int
        return item.toFloat()
    }

    // Fixed size of 60 data points that range from 0 to 100.
    override fun getDataBounds() = RectF(0f, MIN_VALUE, LENGTH.toFloat(), MAX_VALUE)

    fun submitList(list: List<Int>) {
        yData.clear()
        yData.addAll(list)
        notifyDataSetChanged()
    }

    companion object {
        private const val LENGTH = 60
        private const val MIN_VALUE = 0f
        private const val MAX_VALUE = 100f
    }
}