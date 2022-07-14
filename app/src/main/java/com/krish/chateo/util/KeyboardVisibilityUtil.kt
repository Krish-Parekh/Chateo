package com.krish.chateo.util

import android.graphics.Rect
import android.view.View

class KeyboardVisibilityUtil(contentView: View, onKeyBoardShown: (Boolean) -> Unit) {

    private var currentKeyBoardState: Boolean = false

    val visibilityListener = {
        val rectangle = Rect()
        contentView.getWindowVisibleDisplayFrame(rectangle)
        val screenHeight = contentView.rootView.height
        val keypadHeight = screenHeight.minus(rectangle.bottom)
        val isKeyBoardNowVisible = keypadHeight > screenHeight * 0.15

        if (currentKeyBoardState != isKeyBoardNowVisible) {
            if (isKeyBoardNowVisible) {
                onKeyBoardShown(false)
            } else {
                onKeyBoardShown(true)
            }
        }
        currentKeyBoardState = isKeyBoardNowVisible
    }

}