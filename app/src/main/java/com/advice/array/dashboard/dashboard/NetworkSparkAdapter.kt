package com.advice.array.dashboard.dashboard

import android.graphics.RectF
import com.robinhood.spark.SparkAdapter


class NetworkSparkAdapter : SparkAdapter() {

    private val yData = ArrayList<Long>()
    private val maxData = ArrayList<Long>()

    override fun getCount() = LENGTH

    override fun getItem(index: Int): Any {
        val paddingCount = LENGTH - yData.size
        if (index - paddingCount < 0)
            return 0L
        return yData[index - paddingCount]
    }

    override fun getY(index: Int): Float {
        val item = getItem(index) as Long
        return item.toFloat()
    }

    // Fixed size of 60 data points that range from 0 to 100.
    override fun getDataBounds(): RectF {
        val maxOrNull = maxData.maxOrNull()
        val max = if (maxOrNull != null) {
            maxOrNull * 1.2f
        } else {
            100f
        }
        return RectF(0f, MIN_VALUE, LENGTH.toFloat(), max)
    }

    fun submitList(list: List<Long>, max: List<Long>) {
        yData.clear()
        maxData.clear()
        yData.addAll(list)
        maxData.addAll(max)
        notifyDataSetChanged()
    }

    companion object {
        private const val LENGTH = 60
        private const val MIN_VALUE = 0f
    }
}