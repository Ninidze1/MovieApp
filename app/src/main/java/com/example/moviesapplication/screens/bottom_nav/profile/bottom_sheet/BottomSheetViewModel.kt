package com.example.moviesapplication.screens.bottom_nav.profile.bottom_sheet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviesapplication.repository.datastore.DataStoreRep
import com.example.moviesapplication.repository.room.RoomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BottomSheetViewModel @Inject constructor(private val dataStore: DataStoreRep, private val room: RoomRepository): ViewModel() {

    fun deleteSession() {
        viewModelScope.launch {
            dataStore.deleteSession()
        }
    }

    fun clearFavourites() {
        viewModelScope.launch {
            room.deleteAll()
        }
    }
}