package com.example.aura.di

import com.example.aura.feature.favorites.FavoritesViewModel
import com.example.aura.feature.settings.SettingsViewModel
import com.example.aura.feature.videos.detail.VideoDetailViewModel
import com.example.aura.feature.videos.list.VideosViewModel
import com.example.aura.feature.wallpaper.detail.WallpaperViewModel
import com.example.aura.feature.wallpaper.list.WallpaperListViewModel
import com.example.aura.shared.data.downloader.ImageDownloader
import com.example.aura.shared.data.downloader.VideoDownloader
import com.example.aura.shared.navigation.AppNavigator
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module


val appModule = module {
    single {
        ImageDownloader(androidContext())
    }
    single {
        VideoDownloader(androidContext())
    }
    single {
        AppNavigator()
    }

    viewModelOf(::WallpaperListViewModel)
    viewModelOf(::WallpaperViewModel)
    viewModelOf(::FavoritesViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::VideosViewModel)
    viewModelOf(::VideoDetailViewModel)
}
