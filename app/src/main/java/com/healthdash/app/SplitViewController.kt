package com.healthdash.app

/** 스플릿 뷰 상/하 비율 계산 (상단 AI 패널 25% ~ 75%) */
object SplitViewController {
    const val MIN_RATIO = 0.25f
    const val MAX_RATIO = 0.75f
    const val DEFAULT_RATIO = 0.40f

    fun applyDrag(currentRatio: Float, dragPx: Float, containerHeightPx: Float): Float {
        if (containerHeightPx <= 0f) return currentRatio
        return (currentRatio + dragPx / containerHeightPx).coerceIn(MIN_RATIO, MAX_RATIO)
    }
}
