package com.projecta

import com.tcl.libaccount.openapi.AccountBuilder
import kotlin.contracts.ReturnsNotNull

/**
 * description:
 * author chaojiong.zhang
 * data: 2021/10/20
 * copyright TCL+
 */
open class MainActivityA {
    companion object {
        @JvmStatic
        fun test() {
            println("MainActivityA")
        }
    }


    fun test1(): Boolean {
        return if (AccountBuilder.getInstance() != null) {
            true

        } else {
            false
        }
    }

}