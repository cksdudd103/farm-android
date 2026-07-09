package com.smartfarm.app.data.repository

import com.smartfarm.app.data.AppContainer
import com.smartfarm.app.data.local.CropEntity
import com.smartfarm.app.data.local.JournalEntity
import com.smartfarm.app.data.local.TaskEntity
import com.smartfarm.app.data.model.*
import com.smartfarm.app.util.ApiResult
import com.smartfarm.app.util.safeApiCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

/**
 * Repository for all farm-domain data (crops, journals, tasks, inventory,
 * shipments, community posts, AI diagnoses, RDA notices, market/weather/
 * pesticide/support/safety reference data, dashboard summary, and admin
 * user management). Wraps the [ApiService] and offers Room-backed offline
 * caches for crops/journals/tasks.
 */
class FarmRepository(private val container: AppContainer) {

    private fun api() = container.currentApi()
    private fun db() = container.database

    private fun textPart(value: String?): okhttp3.RequestBody =
        (value ?: "").toRequestBody("text/plain".toMediaTypeOrNull())

    private fun imagePart(file: File?): MultipartBody.Part? {
        if (file == null || !file.exists()) return null
        val body = file.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("image", file.name, body)
    }

    // ---------------- Crops ----------------
    suspend fun fetchCrops(): ApiResult<List<Crop>> {
        val result = safeApiCall { api().listCrops() }
        if (result is ApiResult.Success) {
            db().cropDao().replaceAll(result.data.map { it.toEntity() })
        }
        return result
    }

    fun observeCachedCrops(): Flow<List<Crop>> =
        db().cropDao().observeAll().map { list -> list.map { it.toModel() } }

    suspend fun createCrop(
        name: String, variety: String, fieldLocation: String, area: String,
        plantingDate: String, expectedHarvestDate: String, status: String, memo: String,
        imageFile: File?,
    ): ApiResult<Crop> = safeApiCall {
        api().createCrop(
            mapOf(
                "name" to textPart(name), "variety" to textPart(variety),
                "field_location" to textPart(fieldLocation), "area" to textPart(area),
                "planting_date" to textPart(plantingDate),
                "expected_harvest_date" to textPart(expectedHarvestDate),
                "status" to textPart(status), "memo" to textPart(memo),
            ),
            imagePart(imageFile),
        )
    }

    suspend fun updateCrop(
        id: Int, name: String, variety: String, fieldLocation: String, area: String,
        plantingDate: String, expectedHarvestDate: String, status: String, memo: String,
        imageFile: File?,
    ): ApiResult<Crop> = safeApiCall {
        api().updateCrop(
            id,
            mapOf(
                "name" to textPart(name), "variety" to textPart(variety),
                "field_location" to textPart(fieldLocation), "area" to textPart(area),
                "planting_date" to textPart(plantingDate),
                "expected_harvest_date" to textPart(expectedHarvestDate),
                "status" to textPart(status), "memo" to textPart(memo),
            ),
            imagePart(imageFile),
        )
    }

    suspend fun deleteCrop(id: Int): ApiResult<Unit> = safeApiCall { api().deleteCrop(id) }

    // ---------------- Journals ----------------
    suspend fun fetchJournals(): ApiResult<List<Journal>> {
        val result = safeApiCall { api().listJournals() }
        if (result is ApiResult.Success) {
            db().journalDao().replaceAll(result.data.map { it.toEntity() })
        }
        return result
    }

    fun observeCachedJournals(): Flow<List<Journal>> =
        db().journalDao().observeAll().map { list -> list.map { it.toModel() } }

    suspend fun createJournal(
        cropId: Int?, date: String, workType: String, weather: String, content: String,
        imageFile: File?,
    ): ApiResult<Journal> = safeApiCall {
        api().createJournal(
            mapOf(
                "crop_id" to textPart(cropId?.toString()), "date" to textPart(date),
                "work_type" to textPart(workType), "weather" to textPart(weather),
                "content" to textPart(content),
            ),
            imagePart(imageFile),
        )
    }

    suspend fun updateJournal(
        id: Int, cropId: Int?, date: String, workType: String, weather: String, content: String,
        imageFile: File?,
    ): ApiResult<Journal> = safeApiCall {
        api().updateJournal(
            id,
            mapOf(
                "crop_id" to textPart(cropId?.toString()), "date" to textPart(date),
                "work_type" to textPart(workType), "weather" to textPart(weather),
                "content" to textPart(content),
            ),
            imagePart(imageFile),
        )
    }

    suspend fun deleteJournal(id: Int): ApiResult<Unit> = safeApiCall { api().deleteJournal(id) }

    // ---------------- Tasks ----------------
    suspend fun fetchTasks(): ApiResult<List<Task>> {
        val result = safeApiCall { api().listTasks() }
        if (result is ApiResult.Success) {
            db().taskDao().replaceAll(result.data.map { it.toEntity() })
        }
        return result
    }

    fun observeCachedTasks(): Flow<List<Task>> =
        db().taskDao().observeAll().map { list -> list.map { it.toModel() } }

    suspend fun createTask(
        cropId: Int?, title: String, memo: String, dueDate: String, priority: String, status: String,
    ): ApiResult<Task> = safeApiCall {
        api().createTask(cropId, title, memo, dueDate, priority, status)
    }

    suspend fun updateTask(
        id: Int, title: String? = null, memo: String? = null, dueDate: String? = null,
        priority: String? = null, status: String? = null,
    ): ApiResult<Task> = safeApiCall {
        api().updateTask(id, title, memo, dueDate, priority, status)
    }

    suspend fun deleteTask(id: Int): ApiResult<Unit> = safeApiCall { api().deleteTask(id) }

    // ---------------- Inventory ----------------
    suspend fun fetchInventory(): ApiResult<List<InventoryItem>> = safeApiCall { api().listInventory() }

    suspend fun createInventory(
        name: String, category: String, quantity: Double, unit: String, location: String,
        expiryDate: String, memo: String,
    ): ApiResult<InventoryItem> = safeApiCall {
        api().createInventory(name, category, quantity, unit, location, expiryDate, memo)
    }

    suspend fun updateInventory(
        id: Int, name: String? = null, category: String? = null, quantity: Double? = null,
        unit: String? = null, location: String? = null, expiryDate: String? = null, memo: String? = null,
    ): ApiResult<InventoryItem> = safeApiCall {
        api().updateInventory(id, name, category, quantity, unit, location, expiryDate, memo)
    }

    suspend fun deleteInventory(id: Int): ApiResult<Unit> = safeApiCall { api().deleteInventory(id) }

    // ---------------- Shipments ----------------
    suspend fun fetchShipments(): ApiResult<List<Shipment>> = safeApiCall { api().listShipments() }

    suspend fun createShipment(
        cropId: Int?, buyer: String, quantity: Double, unit: String, unitPrice: Double,
        shipmentDate: String, status: String, memo: String,
    ): ApiResult<Shipment> = safeApiCall {
        api().createShipment(cropId, buyer, quantity, unit, unitPrice, shipmentDate, status, memo)
    }

    suspend fun updateShipment(
        id: Int, buyer: String? = null, quantity: Double? = null, unit: String? = null,
        unitPrice: Double? = null, shipmentDate: String? = null, status: String? = null, memo: String? = null,
    ): ApiResult<Shipment> = safeApiCall {
        api().updateShipment(id, buyer, quantity, unit, unitPrice, shipmentDate, status, memo)
    }

    suspend fun deleteShipment(id: Int): ApiResult<Unit> = safeApiCall { api().deleteShipment(id) }

    // ---------------- Community Posts ----------------
    suspend fun fetchPosts(): ApiResult<List<Post>> = safeApiCall { api().listPosts() }

    suspend fun fetchPost(id: Int): ApiResult<Post> = safeApiCall { api().getPost(id) }

    suspend fun createPost(
        category: String, title: String, content: String, imageFile: File?,
    ): ApiResult<Post> = safeApiCall {
        api().createPost(
            mapOf(
                "category" to textPart(category), "title" to textPart(title),
                "content" to textPart(content),
            ),
            imagePart(imageFile),
        )
    }

    suspend fun deletePost(id: Int): ApiResult<Unit> = safeApiCall { api().deletePost(id) }

    // ---------------- AI Diagnosis ----------------
    suspend fun fetchDiagnoses(): ApiResult<List<Diagnosis>> = safeApiCall { api().listDiagnoses() }

    suspend fun createDiagnosis(cropName: String, imageFile: File): ApiResult<Diagnosis> = safeApiCall {
        val body = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("image", imageFile.name, body)
        api().createDiagnosis(textPart(cropName), part)
    }

    suspend fun deleteDiagnosis(id: Int): ApiResult<Unit> = safeApiCall { api().deleteDiagnosis(id) }

    // ---------------- RDA Notices ----------------
    suspend fun fetchRdaNotices(): ApiResult<List<RdaNotice>> = safeApiCall { api().listRdaNotices() }

    suspend fun deleteRdaNotice(id: Int): ApiResult<Unit> = safeApiCall { api().deleteRdaNotice(id) }

    // ---------------- Reference data ----------------
    suspend fun fetchMarketPrices(): ApiResult<List<MarketItem>> {
        return try {
            val response = api().getMarketPrices()
            val body = response.body()
            if (response.isSuccessful && body?.ok == true) ApiResult.Success(body.data)
            else ApiResult.Error("시세 정보를 불러오지 못했습니다.")
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "네트워크 오류")
        }
    }

    suspend fun fetchWeather(region: String = "전국"): ApiResult<List<WeatherDay>> {
        return try {
            val response = api().getWeather(region)
            val body = response.body()
            if (response.isSuccessful && body?.ok == true) ApiResult.Success(body.data)
            else ApiResult.Error("날씨 정보를 불러오지 못했습니다.")
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "네트워크 오류")
        }
    }

    suspend fun fetchPesticides(query: String = ""): ApiResult<List<PesticideInfo>> =
        safeApiCall { api().getPesticides(query) }

    suspend fun fetchSupportPrograms(): ApiResult<List<SupportProgram>> =
        safeApiCall { api().getSupportPrograms() }

    suspend fun fetchSafetyGuides(): ApiResult<List<SafetyGuide>> =
        safeApiCall { api().getSafetyGuides() }

    // ---------------- Dashboard ----------------
    suspend fun fetchDashboardSummary(): ApiResult<DashboardSummary> {
        return try {
            val response = api().getDashboardSummary()
            val body = response.body()
            if (response.isSuccessful && body?.ok == true && body.data != null) {
                ApiResult.Success(body.data)
            } else ApiResult.Error("대시보드 정보를 불러오지 못했습니다.")
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "네트워크 오류")
        }
    }

    // ---------------- Admin: Users ----------------
    suspend fun fetchUsers(): ApiResult<List<User>> = safeApiCall { api().listUsers() }

    suspend fun updateUserRole(id: Int, role: String): ApiResult<User> =
        safeApiCall { api().updateUser(id, role = role) }

    suspend fun updateUserActive(id: Int, isActive: Boolean): ApiResult<User> =
        safeApiCall { api().updateUser(id, isActive = isActive) }

    suspend fun deleteUser(id: Int): ApiResult<Unit> = safeApiCall { api().deleteUser(id) }

    suspend fun updateUserGrade(id: Int, gradeId: Int?): ApiResult<User> =
        safeApiCall { api().updateUserGrade(id, gradeId) }

    // ---------------- Plans ----------------
    suspend fun fetchPlans(): ApiResult<List<Plan>> = safeApiCall { api().listPlans() }

    suspend fun createPlan(
        code: String, name: String, priceMonthly: Int, priceAnnual: Int,
        isActive: Boolean, displayOrder: Int,
    ): ApiResult<Plan> = safeApiCall {
        api().createPlan(code, name, priceMonthly, priceAnnual, isActive, displayOrder)
    }

    suspend fun updatePlan(
        id: Int, name: String? = null, priceMonthly: Int? = null, priceAnnual: Int? = null,
        isActive: Boolean? = null, displayOrder: Int? = null,
    ): ApiResult<Plan> = safeApiCall {
        api().updatePlan(id, name, priceMonthly, priceAnnual, isActive, displayOrder)
    }

    suspend fun deletePlan(id: Int): ApiResult<Unit> = safeApiCall { api().deletePlan(id) }

    // ---------------- Subscriptions ----------------
    suspend fun fetchMySubscription(): ApiResult<Subscription> = safeApiCall { api().getMySubscription() }

    suspend fun upgradeSubscription(planId: Int, billingCycle: String, promoCode: String? = null): ApiResult<Subscription> =
        safeApiCall { api().upgradeSubscription(planId, billingCycle, promoCode) }

    // ---------------- Grades ----------------
    suspend fun fetchGrades(): ApiResult<List<Grade>> = safeApiCall { api().listGrades() }

    suspend fun createGrade(
        code: String, name: String, discountPercent: Int, minSpend: Int,
        color: String, displayOrder: Int, description: String?,
    ): ApiResult<Grade> = safeApiCall {
        api().createGrade(code, name, discountPercent, minSpend, color, displayOrder, description)
    }

    suspend fun updateGrade(
        id: Int, name: String? = null, discountPercent: Int? = null, minSpend: Int? = null,
        color: String? = null, displayOrder: Int? = null, description: String? = null,
    ): ApiResult<Grade> = safeApiCall {
        api().updateGrade(id, name, discountPercent, minSpend, color, displayOrder, description)
    }

    suspend fun deleteGrade(id: Int): ApiResult<Unit> = safeApiCall { api().deleteGrade(id) }
}

// ---------------- Entity <-> Model mappers ----------------

private fun Crop.toEntity() = CropEntity(
    id = id, userId = userId, name = name, variety = variety, fieldLocation = fieldLocation,
    area = area, plantingDate = plantingDate, expectedHarvestDate = expectedHarvestDate,
    status = status, memo = memo, image = image, createdAt = createdAt,
)

private fun CropEntity.toModel() = Crop(
    id = id, userId = userId, name = name, variety = variety, fieldLocation = fieldLocation,
    area = area, plantingDate = plantingDate, expectedHarvestDate = expectedHarvestDate,
    status = status, memo = memo, image = image, createdAt = createdAt,
)

private fun Journal.toEntity() = JournalEntity(
    id = id, userId = userId, cropId = cropId, cropName = cropName, date = date,
    workType = workType, weather = weather, content = content, image = image, createdAt = createdAt,
)

private fun JournalEntity.toModel() = Journal(
    id = id, userId = userId, cropId = cropId, cropName = cropName, date = date,
    workType = workType, weather = weather, content = content, image = image, createdAt = createdAt,
)

private fun Task.toEntity() = TaskEntity(
    id = id, userId = userId, cropId = cropId, cropName = cropName, title = title, memo = memo,
    dueDate = dueDate, priority = priority, status = status, createdAt = createdAt,
)

private fun TaskEntity.toModel() = Task(
    id = id, userId = userId, cropId = cropId, cropName = cropName, title = title, memo = memo,
    dueDate = dueDate, priority = priority, status = status, createdAt = createdAt,
)
