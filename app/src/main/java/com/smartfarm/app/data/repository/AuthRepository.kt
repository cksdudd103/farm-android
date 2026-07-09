package com.smartfarm.app.data.repository

import com.smartfarm.app.data.AppContainer
import com.smartfarm.app.data.model.User
import com.smartfarm.app.util.ApiResult

class AuthRepository(private val container: AppContainer) {

    private val prefs get() = container.preferencesManager

    val sessionUserFlow get() = prefs.loggedInUserFlow

    suspend fun login(email: String, password: String, rememberMe: Boolean = true): ApiResult<User> {
        return try {
            val response = container.currentApi().login(email, password)
            val body = response.body()
            if (response.isSuccessful && body?.ok == true && body.user != null) {
                val u = body.user
                prefs.saveSession(u.id, u.name, u.email, u.role)
                prefs.setRememberMe(rememberMe)
                ApiResult.Success(u)
            } else {
                ApiResult.Error(body?.msg ?: "로그인에 실패했습니다. (${response.code()})")
            }
        } catch (e: java.io.IOException) {
            ApiResult.Error("서버에 연결할 수 없습니다. 백엔드 주소(BASE_URL)를 확인하세요.")
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "알 수 없는 오류가 발생했습니다.")
        }
    }

    /**
     * Registers a new account. The Flask backend logs the user into a
     * session cookie immediately after registration (see api_register in
     * app.py), but per product requirements the app should show a success
     * message and return to the login screen (not auto-enter the app), so
     * this call logs the freshly-created session back out on success before
     * returning - the local DataStore session is never saved here.
     */
    suspend fun register(
        name: String,
        email: String,
        password: String,
        phone: String,
        farmName: String,
        region: String,
    ): ApiResult<User> {
        return try {
            val response = container.currentApi().register(name, email, password, phone, farmName, region)
            val body = response.body()
            if (response.isSuccessful && body?.ok == true && body.user != null) {
                try {
                    container.currentApi().logout()
                } catch (_: Exception) {
                    // ignore; local session was never saved regardless
                }
                ApiResult.Success(body.user)
            } else {
                ApiResult.Error(body?.msg ?: "회원가입에 실패했습니다. (${response.code()})")
            }
        } catch (e: java.io.IOException) {
            ApiResult.Error("서버에 연결할 수 없습니다. 백엔드 주소(BASE_URL)를 확인하세요.")
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "알 수 없는 오류가 발생했습니다.")
        }
    }

    /**
     * "아이디/비밀번호 찾기" (find ID / password). There is no email server
     * configured on the backend, so this simply verifies whether the given
     * identifier (email or name) is a registered account (via a lightweight
     * lookup against /api/login with a deliberately-wrong password is not
     * viable, so we treat this as a client-side placeholder: any non-blank
     * input returns a "발송되었습니다" style placeholder message).
     */
    suspend fun findAccountPlaceholder(query: String): ApiResult<String> {
        return if (query.isBlank()) {
            ApiResult.Error("이메일 또는 이름을 입력해주세요.")
        } else {
            ApiResult.Success("입력하신 정보로 가입된 계정이 있다면, 등록된 이메일로 안내 메일이 발송되었습니다.")
        }
    }

    suspend fun logout() {
        try {
            container.currentApi().logout()
        } catch (_: Exception) {
            // ignore network errors on logout; clear local session regardless
        }
        prefs.clearSession()
        prefs.setRememberMe(false)
    }

    suspend fun restoreSession(): ApiResult<User> {
        return try {
            val response = container.currentApi().me()
            val body = response.body()
            if (response.isSuccessful && body?.ok == true && body.user != null) {
                ApiResult.Success(body.user)
            } else {
                ApiResult.Error(body?.msg ?: "세션이 만료되었습니다.")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "세션 확인 실패")
        }
    }
}
