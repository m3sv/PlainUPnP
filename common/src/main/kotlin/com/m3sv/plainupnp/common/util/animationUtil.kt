package com.m3sv.plainupnp.common.util

import android.animation.Animator
import android.animation.ObjectAnimator

fun ObjectAnimator.onAnimationStart(block: (Animator?) -> Unit): ObjectAnimator {
    addListener(object : Animator.AnimatorListener {
        override fun onAnimationRepeat(animation: Animator?) {
        }

        override fun onAnimationEnd(animation: Animator?) {
        }

        override fun onAnimationCancel(animation: Animator?) {
        }

        override fun onAnimationStart(animation: Animator?) {
            block(animation)
        }
    })
    return this
}

fun ObjectAnimator.onAnimationEnd(block: (Animator?) -> Unit): ObjectAnimator {
    addListener(object : Animator.AnimatorListener {
        override fun onAnimationRepeat(animation: Animator?) {

        }

        override fun onAnimationEnd(animation: Animator?) {
            block(animation)
        }

        override fun onAnimationCancel(animation: Animator?) {
        }

        override fun onAnimationStart(animation: Animator?) {
        }
    })

    return this
}
