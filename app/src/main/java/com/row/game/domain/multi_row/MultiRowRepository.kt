package com.row.game.domain.multi_row

import com.row.game.core.library.random
import com.row.game.domain.multi_row.adapter.GameItem
import kotlin.random.Random

class MultiRowRepository {
    fun generateList(): List<GameItem> {
        val listToReturn = mutableListOf<GameItem>()
        repeat(36) {
            listToReturn.add(GameItem(Random.nextInt(), 1 random 6))
        }
        listToReturn[0].imgValue = null
        listToReturn[5].imgValue = null
        listToReturn[35].imgValue = null
        listToReturn[30].imgValue = null
        return listToReturn
    }

    fun getRandomItem(): GameItem {
        return GameItem(Random.nextInt(), 1 random 6)
    }
}