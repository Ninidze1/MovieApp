package com.example.moviesapplication.paging.source

import android.util.Log.d
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.moviesapplication.entity.MoviePoster
import com.example.moviesapplication.network.NetworkService
import com.example.moviesapplication.utils.Constants.STARTING_PAGE_INDEX
import retrofit2.HttpException
import java.io.IOException

class SimilarMoviesPagingSource(
    private val apiService: NetworkService,
    private val movieId: Int
) :
    PagingSource<Int, MoviePoster>() {
    override fun getRefreshKey(state: PagingState<Int, MoviePoster>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MoviePoster> {
        val position = params.key ?: STARTING_PAGE_INDEX
        return try {
            val response = apiService.getSimilarMovies(movieId, position)
            val data = response.body()!!
            d("dataCheck", "$data")

            LoadResult.Page(
                data = data.results,
                prevKey = if (position == STARTING_PAGE_INDEX) null else position,
                nextKey = if (data.page == data.totalPages) null else position + 1
            )
        } catch (exception: IOException) {
            return LoadResult.Error(exception)
        } catch (exception: HttpException) {
            d("pagingError", exception.message())
            return LoadResult.Error(exception)
        } catch (exception: NullPointerException) {
            return LoadResult.Error(exception)
        }
    }
}