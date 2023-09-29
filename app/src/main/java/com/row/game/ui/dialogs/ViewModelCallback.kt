package com.row.game.ui.dialogs

import androidx.lifecycle.ViewModel

class ViewModelCallback: ViewModel() {
    var callback: (() -> Unit)? = null
}