package com.smartfarm.app.data.remote

import com.smartfarm.app.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit interface mirroring the Flask backend routes defined in
 * farm-webapp/app.py. Session auth is handled transparently by OkHttp's
 * CookieJar (Flask-Login uses a session cookie set on /api/login).
 */
interface ApiService {

    // ---------------- Auth ----------------
    @FormUrlEncoded
    @POST("api/login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String,
    ): Response<AuthResponse>

    @FormUrlEncoded
    @POST("api/register")
    suspend fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("phone") phone: String,
        @Field("farm_name") farmName: String,
        @Field("region") region: String,
    ): Response<AuthResponse>

    @POST("api/logout")
    suspend fun logout(): Response<ApiResponse<Unit>>

    @GET("api/me")
    suspend fun me(): Response<AuthResponse>

    // ---------------- Admin: Users ----------------
    @GET("api/users")
    suspend fun listUsers(): Response<ApiResponse<List<User>>>

    @FormUrlEncoded
    @PUT("api/users/{id}")
    suspend fun updateUser(
        @Path("id") id: Int,
        @Field("role") role: String? = null,
        @Field("is_active_user") isActive: Boolean? = null,
        @Field("name") name: String? = null,
    ): Response<ApiResponse<User>>

    @DELETE("api/users/{id}")
    suspend fun deleteUser(@Path("id") id: Int): Response<ApiResponse<Unit>>

    // ---------------- Crops ----------------
    @GET("api/crops")
    suspend fun listCrops(): Response<ApiResponse<List<Crop>>>

    @Multipart
    @POST("api/crops")
    suspend fun createCrop(
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part image: MultipartBody.Part?,
    ): Response<ApiResponse<Crop>>

    @Multipart
    @PUT("api/crops/{id}")
    suspend fun updateCrop(
        @Path("id") id: Int,
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part image: MultipartBody.Part?,
    ): Response<ApiResponse<Crop>>

    @DELETE("api/crops/{id}")
    suspend fun deleteCrop(@Path("id") id: Int): Response<ApiResponse<Unit>>

    // ---------------- Journals ----------------
    @GET("api/journals")
    suspend fun listJournals(): Response<ApiResponse<List<Journal>>>

    @Multipart
    @POST("api/journals")
    suspend fun createJournal(
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part image: MultipartBody.Part?,
    ): Response<ApiResponse<Journal>>

    @Multipart
    @PUT("api/journals/{id}")
    suspend fun updateJournal(
        @Path("id") id: Int,
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part image: MultipartBody.Part?,
    ): Response<ApiResponse<Journal>>

    @DELETE("api/journals/{id}")
    suspend fun deleteJournal(@Path("id") id: Int): Response<ApiResponse<Unit>>

    // ---------------- Tasks ----------------
    @GET("api/tasks")
    suspend fun listTasks(): Response<ApiResponse<List<Task>>>

    @FormUrlEncoded
    @POST("api/tasks")
    suspend fun createTask(
        @Field("crop_id") cropId: Int?,
        @Field("title") title: String,
        @Field("memo") memo: String?,
        @Field("due_date") dueDate: String?,
        @Field("priority") priority: String,
        @Field("status") status: String,
    ): Response<ApiResponse<Task>>

    @FormUrlEncoded
    @PUT("api/tasks/{id}")
    suspend fun updateTask(
        @Path("id") id: Int,
        @Field("title") title: String? = null,
        @Field("memo") memo: String? = null,
        @Field("due_date") dueDate: String? = null,
        @Field("priority") priority: String? = null,
        @Field("status") status: String? = null,
    ): Response<ApiResponse<Task>>

    @DELETE("api/tasks/{id}")
    suspend fun deleteTask(@Path("id") id: Int): Response<ApiResponse<Unit>>

    // ---------------- Inventory ----------------
    @GET("api/inventory")
    suspend fun listInventory(): Response<ApiResponse<List<InventoryItem>>>

    @FormUrlEncoded
    @POST("api/inventory")
    suspend fun createInventory(
        @Field("name") name: String,
        @Field("category") category: String?,
        @Field("quantity") quantity: Double,
        @Field("unit") unit: String?,
        @Field("location") location: String?,
        @Field("expiry_date") expiryDate: String?,
        @Field("memo") memo: String?,
    ): Response<ApiResponse<InventoryItem>>

    @FormUrlEncoded
    @PUT("api/inventory/{id}")
    suspend fun updateInventory(
        @Path("id") id: Int,
        @Field("name") name: String? = null,
        @Field("category") category: String? = null,
        @Field("quantity") quantity: Double? = null,
        @Field("unit") unit: String? = null,
        @Field("location") location: String? = null,
        @Field("expiry_date") expiryDate: String? = null,
        @Field("memo") memo: String? = null,
    ): Response<ApiResponse<InventoryItem>>

    @DELETE("api/inventory/{id}")
    suspend fun deleteInventory(@Path("id") id: Int): Response<ApiResponse<Unit>>

    // ---------------- Shipments ----------------
    @GET("api/shipments")
    suspend fun listShipments(): Response<ApiResponse<List<Shipment>>>

    @FormUrlEncoded
    @POST("api/shipments")
    suspend fun createShipment(
        @Field("crop_id") cropId: Int?,
        @Field("buyer") buyer: String?,
        @Field("quantity") quantity: Double,
        @Field("unit") unit: String?,
        @Field("unit_price") unitPrice: Double,
        @Field("shipment_date") shipmentDate: String?,
        @Field("status") status: String,
        @Field("memo") memo: String?,
    ): Response<ApiResponse<Shipment>>

    @FormUrlEncoded
    @PUT("api/shipments/{id}")
    suspend fun updateShipment(
        @Path("id") id: Int,
        @Field("buyer") buyer: String? = null,
        @Field("quantity") quantity: Double? = null,
        @Field("unit") unit: String? = null,
        @Field("unit_price") unitPrice: Double? = null,
        @Field("shipment_date") shipmentDate: String? = null,
        @Field("status") status: String? = null,
        @Field("memo") memo: String? = null,
    ): Response<ApiResponse<Shipment>>

    @DELETE("api/shipments/{id}")
    suspend fun deleteShipment(@Path("id") id: Int): Response<ApiResponse<Unit>>

    // ---------------- Community Posts ----------------
    @GET("api/posts")
    suspend fun listPosts(): Response<ApiResponse<List<Post>>>

    @GET("api/posts/{id}")
    suspend fun getPost(@Path("id") id: Int): Response<ApiResponse<Post>>

    @Multipart
    @POST("api/posts")
    suspend fun createPost(
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part image: MultipartBody.Part?,
    ): Response<ApiResponse<Post>>

    @FormUrlEncoded
    @PUT("api/posts/{id}")
    suspend fun updatePost(
        @Path("id") id: Int,
        @Field("category") category: String? = null,
        @Field("title") title: String? = null,
        @Field("content") content: String? = null,
    ): Response<ApiResponse<Post>>

    @DELETE("api/posts/{id}")
    suspend fun deletePost(@Path("id") id: Int): Response<ApiResponse<Unit>>

    // ---------------- AI Diagnosis ----------------
    @GET("api/diagnoses")
    suspend fun listDiagnoses(): Response<ApiResponse<List<Diagnosis>>>

    @Multipart
    @POST("api/diagnoses")
    suspend fun createDiagnosis(
        @Part("crop_name") cropName: RequestBody,
        @Part image: MultipartBody.Part,
    ): Response<ApiResponse<Diagnosis>>

    @DELETE("api/diagnoses/{id}")
    suspend fun deleteDiagnosis(@Path("id") id: Int): Response<ApiResponse<Unit>>

    // ---------------- RDA Notices ----------------
    @GET("api/rda")
    suspend fun listRdaNotices(): Response<ApiResponse<List<RdaNotice>>>

    @FormUrlEncoded
    @POST("api/rda")
    suspend fun createRdaNotice(
        @Field("title") title: String,
        @Field("content") content: String?,
        @Field("category") category: String,
        @Field("notice_date") noticeDate: String?,
        @Field("source_url") sourceUrl: String?,
    ): Response<ApiResponse<RdaNotice>>

    @DELETE("api/rda/{id}")
    suspend fun deleteRdaNotice(@Path("id") id: Int): Response<ApiResponse<Unit>>

    // ---------------- Market / Weather / Pesticide / Support / Safety ----------------
    @GET("api/market")
    suspend fun getMarketPrices(): Response<MarketResponse>

    @GET("api/weather")
    suspend fun getWeather(@Query("region") region: String = "전국"): Response<WeatherResponse>

    @GET("api/pesticides")
    suspend fun getPesticides(@Query("q") query: String = ""): Response<ApiResponse<List<PesticideInfo>>>

    @GET("api/support-programs")
    suspend fun getSupportPrograms(): Response<ApiResponse<List<SupportProgram>>>

    @GET("api/safety")
    suspend fun getSafetyGuides(): Response<ApiResponse<List<SafetyGuide>>>

    // ---------------- Dashboard ----------------
    @GET("api/dashboard/summary")
    suspend fun getDashboardSummary(): Response<DashboardResponse>

    // ---------------- Plans ----------------
    @GET("api/plans")
    suspend fun listPlans(): Response<ApiResponse<List<Plan>>>

    @FormUrlEncoded
    @POST("api/plans")
    suspend fun createPlan(
        @Field("code") code: String,
        @Field("name") name: String,
        @Field("price_monthly") priceMonthly: Int,
        @Field("price_annual") priceAnnual: Int,
        @Field("is_active") isActive: Boolean,
        @Field("display_order") displayOrder: Int,
    ): Response<ApiResponse<Plan>>

    @FormUrlEncoded
    @PUT("api/plans/{id}")
    suspend fun updatePlan(
        @Path("id") id: Int,
        @Field("name") name: String? = null,
        @Field("price_monthly") priceMonthly: Int? = null,
        @Field("price_annual") priceAnnual: Int? = null,
        @Field("is_active") isActive: Boolean? = null,
        @Field("display_order") displayOrder: Int? = null,
    ): Response<ApiResponse<Plan>>

    @DELETE("api/plans/{id}")
    suspend fun deletePlan(@Path("id") id: Int): Response<ApiResponse<Unit>>

    // ---------------- Subscriptions ----------------
    @GET("api/subscriptions/me")
    suspend fun getMySubscription(): Response<ApiResponse<Subscription>>

    @FormUrlEncoded
    @POST("api/subscriptions/upgrade")
    suspend fun upgradeSubscription(
        @Field("plan_id") planId: Int,
        @Field("billing_cycle") billingCycle: String,
        @Field("promo_code") promoCode: String? = null,
    ): Response<ApiResponse<Subscription>>

    // ---------------- Grades (Admin) ----------------
    @GET("api/grades")
    suspend fun listGrades(): Response<ApiResponse<List<Grade>>>

    @FormUrlEncoded
    @POST("api/grades")
    suspend fun createGrade(
        @Field("code") code: String,
        @Field("name") name: String,
        @Field("discount_percent") discountPercent: Int,
        @Field("min_spend") minSpend: Int,
        @Field("color") color: String,
        @Field("display_order") displayOrder: Int,
        @Field("description") description: String?,
    ): Response<ApiResponse<Grade>>

    @FormUrlEncoded
    @PUT("api/grades/{id}")
    suspend fun updateGrade(
        @Path("id") id: Int,
        @Field("name") name: String? = null,
        @Field("discount_percent") discountPercent: Int? = null,
        @Field("min_spend") minSpend: Int? = null,
        @Field("color") color: String? = null,
        @Field("display_order") displayOrder: Int? = null,
        @Field("description") description: String? = null,
    ): Response<ApiResponse<Grade>>

    @DELETE("api/grades/{id}")
    suspend fun deleteGrade(@Path("id") id: Int): Response<ApiResponse<Unit>>

    @FormUrlEncoded
    @PUT("api/users/{id}")
    suspend fun updateUserGrade(
        @Path("id") id: Int,
        @Field("grade_id") gradeId: Int?,
    ): Response<ApiResponse<User>>
}
