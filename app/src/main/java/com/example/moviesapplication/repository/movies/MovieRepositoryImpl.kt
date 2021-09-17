package com.example.moviesapplication.repository.movies

import com.example.moviesapplication.entity.GenreResponse
import com.example.moviesapplication.entity.ResponseItems
import com.example.moviesapplication.network.NetworkService
import com.example.moviesapplication.network.Resource
import com.example.moviesapplication.utils.Constants.LANG_ENG
import com.example.moviesapplication.utils.Constants.NETWORK_PAGE_SIZE
import javax.inject.Inject

class MovieRepositoryImpl @Inject constructor(private val apiService: NetworkService): MovieRepository {
    override suspend fun getPopularMovies(): Resource<ResponseItems> {
                return try {
            val response = apiService.getPopularMovies(LANG_ENG, NETWORK_PAGE_SIZE)
            if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.error(response.message())
            }
        } catch (e: Exception) {
            Resource.error(e.message.toString())
        }
    }

    override suspend fun getLatestMovies(): Resource<ResponseItems> {
        return try {
            val response = apiService.getLatestMovies(LANG_ENG, NETWORK_PAGE_SIZE)
            if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.error(response.message())
            }
        } catch (e: Exception) {
            Resource.error(e.message.toString())
        }
    }

    override suspend fun getGenres(): Resource<GenreResponse> {
        return try {
            val response = apiService.getGenres()
            if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.error(response.message())
            }
        } catch (e: Exception) {
            Resource.error(e.message.toString())
        }
    }

    override suspend fun searchMovie(query: String): Resource<ResponseItems> {
        return try {
            val response = apiService.searchMovie(
                query,
                LANG_ENG,
                NETWORK_PAGE_SIZE
            )
            if (response.isSuccessful) {
                Resource.success(response.body()!!)
            } else {
                Resource.error(response.message())
            }
        } catch (e: Exception) {
            Resource.error(e.message.toString())
        }
    }
}