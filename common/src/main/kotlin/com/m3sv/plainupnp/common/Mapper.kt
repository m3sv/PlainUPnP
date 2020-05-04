package com.m3sv.plainupnp.common

interface Mapper<I, O> {
    fun map(input: I): O
}
