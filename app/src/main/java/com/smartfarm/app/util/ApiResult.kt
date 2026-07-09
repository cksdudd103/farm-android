package com.smartfarm.app.util

/** Simple sealed result wrapper for repository/API calls. */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String) : ApiResult<Nothing>()
}

suspend fun <T> safeApiCall(block: suspend () -> retrofit2.Response<com.smartfarm.app.data.model.ApiResponse<T>>): ApiResult<T> {
    return try {
        val response = block()
        val body = response.body()
        if (response.isSuccessful && body?.ok == true) {
            @Suppress("UNCHECKED_CAST")
            ApiResult.Success(body.data as T)
        } else {
            ApiResult.Error(body?.msg ?: "요청 처리 중 오류가 발생했습니다. (${response.code()})")
        }
    } catch (e: java.io.IOException) {
        ApiResult.Error("서버에 연결할 수 없습니다. 네트워크 상태와 백엔드 주소를 확인하세요.")
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "알 수 없는 오류가 발생했습니다.")
    }
}
