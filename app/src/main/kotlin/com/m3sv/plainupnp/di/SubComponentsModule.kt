package com.m3sv.plainupnp.di

import com.m3sv.plainupnp.di.activity.MainActivitySubComponent
import dagger.Module

@Module(subcomponents = [MainActivitySubComponent::class])
interface SubComponentsModule
