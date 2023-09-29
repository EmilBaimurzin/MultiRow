package com.row.game.ui.multi_row

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.row.game.core.library.l
import com.row.game.domain.multi_row.MultiRowRepository
import com.row.game.domain.multi_row.adapter.GameItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MultiRowViewModel : ViewModel() {
    private val repository = MultiRowRepository()
    private val _task1 = MutableLiveData(0)
    val task1: LiveData<Int> = _task1

    private val _task2 = MutableLiveData(0)
    val task2: LiveData<Int> = _task2

    private val _task3 = MutableLiveData(0)
    val task3: LiveData<Int> = _task3

    private val _task4 = MutableLiveData(0)
    val task4: LiveData<Int> = _task4

    private val _moves = MutableLiveData(25)
    val moves: LiveData<Int> = _moves

    var gameState = true
    var pauseState = false
    private var gameScope = CoroutineScope(Dispatchers.Default)
    private val _list = MutableLiveData(repository.generateList())
    val list: LiveData<List<GameItem>> = _list

    private val _timer = MutableLiveData(0)
    val timer: LiveData<Int> = _timer


    fun startTimer() {
        gameScope = CoroutineScope(Dispatchers.Default)
        gameScope.launch {
            while (true) {
                delay(1000)
                _timer.postValue(_timer.value!! + 1)
            }
        }
    }

    fun stopTimer() {
        gameScope.cancel()
    }

    fun itemClick(position: Int) {
        viewModelScope.launch {
            val currentList = _list.value!!.toMutableList()
            val findItem = currentList.find { it.isSelected }
            if (findItem == null) {
                currentList[position].isSelected = true
                _list.postValue(currentList)
            } else {
                if (currentList[position].isSelected) {
                    currentList[position].isSelected = false
                    _list.postValue(currentList)
                } else {
                    val selectedItem = currentList.find { it.isSelected }
                    val selectedItemPosition = currentList.indexOf(selectedItem)
                    val clickedItem = currentList[position]

                    val selectedItemImage = selectedItem!!.imgValue
                    val clickedItemImage = clickedItem.imgValue

                    val limitLeft = listOf(0, 6, 12, 18, 24, 30)
                    val limitLeftPlusOne = listOf(1, 7, 12, 19, 25, 31)

                    val limitRight = listOf(5, 11, 17, 23, 29, 35)
                    val limitRightMinusOne = listOf(4, 10, 16, 22, 28, 34)

                    val indexLeft = if (limitLeftPlusOne.contains(selectedItemPosition)) {
                        limitLeftPlusOne.indexOf(selectedItemPosition)
                    } else {
                        -1
                    }

                    val indexRight = if (limitRightMinusOne.contains(selectedItemPosition)) {
                        limitRightMinusOne.indexOf(selectedItemPosition)
                    } else {
                        -1
                    }

                    val canLeft = if (indexLeft != -1) {
                        selectedItemPosition - 1 == position
                    } else false

                    val canRight = if (indexRight != -1) {
                        selectedItemPosition + 1 == position
                    } else false

                    val finalCheck =
                        if (position in limitLeft) canLeft else if (position in limitRight) canRight else if (selectedItemPosition - 1 == position) true else selectedItemPosition + 1 == position


                    if (position == selectedItemPosition - 6 || position == selectedItemPosition + 6 || finalCheck) {

                        val newList = _list.value!!.toMutableList()
                        newList[position].imgValue = selectedItemImage
                        newList[selectedItemPosition].imgValue = clickedItemImage

                        newList.map { it.isSelected = false }
                        _list.postValue(newList)
                        firstCheckout(position, selectedItemPosition)
                    } else {
                        currentList.map { it.isSelected = false }
                        _list.postValue(currentList)
                    }
                }
            }
        }
    }

    private fun firstCheckout(selectedItemPosition: Int, clickedItemPosition: Int) {
        viewModelScope.launch {
            val currentList = _list.value!!

            _moves.postValue(_moves.value!! - 1)

            val limitLeft = listOf(0, 6, 12, 18, 24, 30)
            val limitRight = listOf(5, 11, 17, 23, 29, 35)
            val limitTop = listOf(0, 1, 2, 3, 4, 5)
            val limitBottom = listOf(35, 34, 33, 32, 31, 30)

            val selectedItem = currentList[selectedItemPosition]
            val clickedItem = currentList[clickedItemPosition]

            val selectedItemRow = when (selectedItemPosition) {
                in (0..5) -> 1
                in (6..11) -> 2
                in (12..17) -> 3
                in (18..23) -> 4
                in (24..29) -> 5
                else -> 6
            }

            val selectedItemColumn = when (selectedItemPosition - 6 * (selectedItemRow - 1)) {
                0 -> 1
                1 -> 2
                2 -> 3
                3 -> 4
                4 -> 5
                else -> 6
            }

            var repeatLeft = true
            var repeatRight = true
            var repeatTop = true
            var repeatBottom = true

            var indexesToRemove = mutableListOf<Int>()

            val removedTop = mutableListOf<Int>()
            val removedBottom = mutableListOf<Int>()
            val removedLeft = mutableListOf<Int>()
            val removedRight = mutableListOf<Int>()

            //right
            repeat(5) {
                if (selectedItemPosition + it + 1 <= limitRight[selectedItemRow - 1] && repeatRight) {
                    if (currentList[selectedItemPosition + it + 1].imgValue == selectedItem.imgValue) {
                        val index = currentList.indexOf(currentList[selectedItemPosition + it + 1])
                        indexesToRemove.add(index)
                        removedRight.add(index)
                    } else {
                        repeatRight = false
                    }
                }
            }

            //left
            repeat(5) {
                if (selectedItemPosition - (it + 1) >= limitLeft[selectedItemRow - 1] && repeatLeft) {
                    if (currentList[selectedItemPosition - (it + 1)].imgValue == selectedItem.imgValue) {
                        val index =
                            currentList.indexOf(currentList[selectedItemPosition - (it + 1)])
                        indexesToRemove.add(index)
                        removedLeft.add(index)
                    } else {
                        repeatLeft = false
                    }
                }
            }

            //top
            repeat(5) {
                if (selectedItemPosition - 6 * (it + 1) >= limitTop[selectedItemColumn - 1] && repeatTop) {
                    if (currentList[selectedItemPosition - 6 * (it + 1)].imgValue == selectedItem.imgValue) {
                        val index =
                            currentList.indexOf(currentList[selectedItemPosition - 6 * (it + 1)])
                        indexesToRemove.add(index)
                        removedTop.add(index)
                    } else {
                        repeatTop = false
                    }
                }
            }

            //bottom
            repeat(5) {
                if (selectedItemPosition + 6 * (it + 1) <= limitBottom[selectedItemColumn - 1] && repeatBottom) {
                    if (currentList[selectedItemPosition + 6 * (it + 1)].imgValue == selectedItem.imgValue) {
                        val index =
                            currentList.indexOf(currentList[selectedItemPosition + 6 * (it + 1)])
                        indexesToRemove.add(index)
                        removedBottom.add(index)
                    } else {
                        repeatBottom = false
                    }
                }
            }

            if (removedTop.size + removedBottom.size < 2) {
                removedTop.forEach { index ->
                    indexesToRemove.removeAll { it == index }
                }
                removedBottom.forEach { index ->
                    indexesToRemove.removeAll { it == index }
                }
            }

            if (removedLeft.size + removedRight.size < 2) {
                removedLeft.forEach { index ->
                    indexesToRemove.removeAll { it == index }
                }
                removedRight.forEach { index ->
                    indexesToRemove.removeAll { it == index }
                }
            }

            if (indexesToRemove.isNotEmpty() && !indexesToRemove.contains(selectedItemPosition)) {
                indexesToRemove.add(selectedItemPosition)
            }

            //__________________________________________________

            val indexesToRemove2 = mutableListOf<Int>()

            val selectedItemRow2 = when (clickedItemPosition) {
                in (0..5) -> 1
                in (6..11) -> 2
                in (12..17) -> 3
                in (18..23) -> 4
                in (24..29) -> 5
                else -> 6
            }

            val selectedItemColumn2 = when (clickedItemPosition - 6 * (selectedItemRow2 - 1)) {
                0 -> 1
                1 -> 2
                2 -> 3
                3 -> 4
                4 -> 5
                else -> 6
            }

            var repeatLeft2 = true
            var repeatRight2 = true
            var repeatTop2 = true
            var repeatBottom2 = true

            val removedTop2 = mutableListOf<Int>()
            val removedBottom2 = mutableListOf<Int>()
            val removedLeft2 = mutableListOf<Int>()
            val removedRight2 = mutableListOf<Int>()

            //right
            repeat(5) {
                if (clickedItemPosition + it + 1 <= limitRight[selectedItemRow2 - 1] && repeatRight2) {
                    if (currentList[clickedItemPosition + it + 1].imgValue == clickedItem.imgValue) {
                        val index = currentList.indexOf(currentList[clickedItemPosition + it + 1])

                        indexesToRemove2.add(index)
                        removedRight2.add(index)

                    } else {
                        repeatRight2 = false
                    }
                }
            }

            //left
            repeat(5) {
                if (clickedItemPosition - (it + 1) >= limitLeft[selectedItemRow2 - 1] && repeatLeft2) {
                    if (currentList[clickedItemPosition - (it + 1)].imgValue == clickedItem.imgValue) {
                        val index = currentList.indexOf(currentList[clickedItemPosition - (it + 1)])

                        indexesToRemove2.add(index)
                        removedLeft2.add(index)

                    } else {
                        repeatLeft2 = false
                    }
                }
            }

            //top
            repeat(5) {
                if (clickedItemPosition - 6 * (it + 1) >= limitTop[selectedItemColumn2 - 1] && repeatTop2) {
                    if (currentList[clickedItemPosition - 6 * (it + 1)].imgValue == clickedItem.imgValue) {
                        val index =
                            currentList.indexOf(currentList[clickedItemPosition - 6 * (it + 1)])

                        indexesToRemove2.add(index)
                        removedTop2.add(index)

                    } else {
                        repeatTop2 = false
                    }
                }
            }

            //bottom
            repeat(5) {
                if (clickedItemPosition + 6 * (it + 1) <= limitBottom[selectedItemColumn2 - 1] && repeatBottom2) {
                    if (currentList[clickedItemPosition + 6 * (it + 1)].imgValue == clickedItem.imgValue) {
                        val index =
                            currentList.indexOf(currentList[clickedItemPosition + 6 * (it + 1)])

                        indexesToRemove2.add(index)
                        removedBottom2.add(index)

                    } else {
                        repeatBottom2 = false
                    }
                }
            }

            if (removedTop2.size + removedBottom2.size < 2) {
                removedTop2.forEach { index ->
                    indexesToRemove2.removeAll { it == index }
                }
                removedBottom2.forEach { index ->
                    indexesToRemove2.removeAll { it == index }
                }
            }

            if (removedLeft2.size + removedRight2.size < 2) {
                removedLeft2.forEach { index ->
                    indexesToRemove2.removeAll { it == index }
                }
                removedRight2.forEach { index ->
                    indexesToRemove2.removeAll { it == index }
                }
            }

            if (indexesToRemove2.isNotEmpty() && !indexesToRemove2.contains(clickedItemPosition)) {
                indexesToRemove2.add(clickedItemPosition)
            }

            indexesToRemove.distinct()
            indexesToRemove2.distinct()

            if (indexesToRemove.isNotEmpty() && currentList[selectedItemPosition].imgValue != currentList[clickedItemPosition].imgValue) {
                val imgValue = currentList[selectedItemPosition].imgValue

                if (imgValue == 3) {
                    _task1.postValue(if (_task1.value!! + indexesToRemove.size <= 5) _task1.value!! + indexesToRemove.size else 5)
                }

                if (imgValue == 1) {
                    _task2.postValue(if (_task2.value!! + indexesToRemove.size <= 10) _task2.value!! + indexesToRemove.size else 10)
                }

                if (imgValue == 6) {
                    _task3.postValue(if (_task3.value!! + indexesToRemove.size <= 8) _task3.value!! + indexesToRemove.size else 8)
                }

                if (imgValue == 4) {
                    _task4.postValue(if (_task4.value!! + indexesToRemove.size <= 8) _task4.value!! + indexesToRemove.size else 8)
                }
            }

            if (indexesToRemove2.isNotEmpty() && currentList[selectedItemPosition].imgValue != currentList[clickedItemPosition].imgValue) {
                val imgValue = currentList[clickedItemPosition].imgValue
                if (imgValue == 3) {
                    _task1.postValue(if (_task1.value!! + indexesToRemove2.size <= 5) _task1.value!! + indexesToRemove2.size else 5)
                }

                if (imgValue == 1) {
                    _task2.postValue(if (_task2.value!! + indexesToRemove2.size <= 10) _task2.value!! + indexesToRemove2.size else 10)
                }

                if (imgValue == 6) {
                    _task3.postValue(if (_task3.value!! + indexesToRemove2.size <= 8) _task3.value!! + indexesToRemove2.size else 8)
                }

                if (imgValue == 4) {
                    _task4.postValue(if (_task4.value!! + indexesToRemove2.size <= 8) _task4.value!! + indexesToRemove2.size else 8)
                }
            }

            if (indexesToRemove2.isNotEmpty() && indexesToRemove.isNotEmpty() && currentList[selectedItemPosition].imgValue == currentList[clickedItemPosition].imgValue) {
                val newIndexes = (indexesToRemove2 + indexesToRemove).toMutableList()
                newIndexes.distinct()
                val imgValue = currentList[clickedItemPosition].imgValue
                if (imgValue == 3) {
                    _task1.postValue(if (_task1.value!! + newIndexes.size <= 5) _task1.value!! + newIndexes.size else 5)
                }

                if (imgValue == 1) {
                    _task2.postValue(if (_task2.value!! + newIndexes.size <= 10) _task2.value!! + newIndexes.size else 10)
                }

                if (imgValue == 6) {
                    _task3.postValue(if (_task3.value!! + newIndexes.size <= 8) _task3.value!! + newIndexes.size else 8)
                }

                if (imgValue == 4) {
                    _task4.postValue(if (_task4.value!! + newIndexes.size <= 8) _task4.value!! + newIndexes.size else 8)
                }
            }

            indexesToRemove = (indexesToRemove2 + indexesToRemove).toMutableList()

            indexesToRemove.forEach {
                currentList[it].imgValue = null
            }
            _list.postValue(currentList)

            updateList()
        }
    }

    private fun updateList() {
        viewModelScope.launch {
            delay(10)
            val currentList = _list.value!!.toMutableList()
            val newList = _list.value!!.toMutableList()
            repeat(6) { times ->
                val firstColumn = mutableListOf(
                    currentList[0 + times],
                    currentList[6 + times],
                    currentList[12 + times],
                    currentList[18 + times],
                    currentList[24 + times],
                    currentList[30 + times],
                )

                val newFirstColumn = mutableListOf<GameItem>()

                firstColumn.forEach {
                    if (it.imgValue != null) {
                        newFirstColumn.add(it)
                    }
                }

                if (times == 0 || times == 5) {
                    newFirstColumn.add(0, repository.getRandomItem().apply { imgValue = null })
                    newFirstColumn.add(repository.getRandomItem().apply { imgValue = null })
                }

                repeat(6 - newFirstColumn.size) {
                    newFirstColumn.add(
                        if (times == 0 || times == 5) 1 else 0,
                        repository.getRandomItem()
                    )
                }

                l(newFirstColumn.toString())

                newFirstColumn.forEachIndexed { index, i ->
                    val canI = if (times != 0 && times != 5) true else index != 0 && index != 5
                    if (canI) {
                        newList[(index) * 6 + times] = i
                    }
                }
            }
            _list.postValue(newList)
        }
    }
}