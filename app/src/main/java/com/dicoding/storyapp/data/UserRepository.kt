package com.dicoding.storyapp.data

import androidx.lifecycle.LiveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.dicoding.storyapp.data.api.ApiService
import com.dicoding.storyapp.data.api.StoryRemoteMediator
import com.dicoding.storyapp.data.api.response.DetailStoryResponse
import com.dicoding.storyapp.data.api.response.ListStoryItem
import com.dicoding.storyapp.data.api.response.LoginResponse
import com.dicoding.storyapp.data.api.response.RegisterResponse
import com.dicoding.storyapp.data.api.response.StoryResponse
import com.dicoding.storyapp.data.database.StoryDatabase
import com.dicoding.storyapp.data.pref.UserModel
import com.dicoding.storyapp.data.pref.UserPreference
import com.dicoding.storyapp.utils.wrapEspressoIdlingResource

class UserRepository(
    private val apiService: ApiService,
    private val storyDatabase: StoryDatabase,
    private val userPreference: UserPreference
) {

    @OptIn(ExperimentalPagingApi::class)
    fun getStoriesAll(): LiveData<PagingData<ListStoryItem>> {
        wrapEspressoIdlingResource {
            return Pager(
                config = PagingConfig(
                    pageSize = 5
                ),
                remoteMediator = StoryRemoteMediator(storyDatabase, apiService),
                pagingSourceFactory = {
                    storyDatabase.storyDao().getAllStories()
                }
            ).liveData
        }
    }

    suspend fun getStoriesWithLocation(): StoryResponse {
        return apiService.getStoriesWithLocation(location = 1)
    }

    suspend fun getStoryDetail(storyId: String): DetailStoryResponse {
        return apiService.getStoryDetail(storyId)
    }

    fun getSession() = userPreference.getSession()

    suspend fun logout() {
        userPreference.logout()
    }

    suspend fun register(name: String, email: String, password: String): RegisterResponse {
        return apiService.register(name, email, password)
    }

    suspend fun login(email: String, password: String): LoginResponse {
        val response = apiService.login(email, password)
        if (!response.error!!) {
            response.loginResult?.token?.let { token ->
                userPreference.saveSession(UserModel(email, token, true))
            }
        }
        return response
    }

    suspend fun saveSession(userModel: UserModel) {
        userPreference.saveSession(userModel)
    }

    companion object {
        fun getInstance(
            apiService: ApiService,
            storyDatabase: StoryDatabase,
            userPreference: UserPreference
        ) = UserRepository(apiService, storyDatabase, userPreference)
    }
}
