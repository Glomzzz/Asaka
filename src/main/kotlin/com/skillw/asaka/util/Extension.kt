package com.skillw.asaka.util

fun <T> unsafeLazy(initializer: () -> T): Lazy<T> = lazy(LazyThreadSafetyMode.NONE, initializer)

inline fun Boolean.ifThen(todo: () -> Unit) = if (this) {
    todo(); true
} else false
