package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.AnonymizedUser
import com.example.data.model.DeliveryOrder
import com.example.data.model.MedicationLog
import com.example.data.model.MedicationReminder
import com.example.data.model.Product
import com.example.data.model.PeriodLog
import com.example.data.repository.CareLinkRepository
import com.example.data.api.GeminiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID

data class QuizQuestion(
    val id: Int,
    val text: String,
    val options: List<String>,
    val weights: List<Int> // weight corresponding to each option
)

class CareLinkViewModel(private val repository: CareLinkRepository) : ViewModel() {

    // --- Profile & Anonymization ---
    val anonymizedProfile: StateFlow<AnonymizedUser?> = repository.anonymizedProfile
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // --- Medication Tracker ---
    val allMedicationLogs: StateFlow<List<MedicationLog>> = repository.allMedicationLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allReminders: StateFlow<List<MedicationReminder>> = repository.allReminders
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Storefront & Orders ---
    val allOrders: StateFlow<List<DeliveryOrder>> = repository.allOrders
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allPeriodLogs: StateFlow<List<PeriodLog>> = repository.allPeriodLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Quiz States ---
    val quizQuestions = listOf(
        QuizQuestion(
            id = 1,
            text = "In the past 6 months, have you engaged in sexual activity with a partner whose HIV status was unknown to you, or with partners of the same sex, in settings where protection (condoms) wasn't used?",
            options = listOf("Yes", "No", "Prefer not to say"),
            weights = listOf(40, 0, 20)
        ),
        QuizQuestion(
            id = 2,
            text = "In the past 12 months, have you been diagnosed with, or treated for, any sexually transmitted infection (such as Syphilis, Gonorrhea, or Chlamydia)?",
            options = listOf("Yes", "No", "Unsure"),
            weights = listOf(30, 0, 15)
        ),
        QuizQuestion(
            id = 3,
            text = "Do you or your partner have multiple sexual partners, or has your partner recently had sexual contact with someone whose HIV status is unknown?",
            options = listOf("Yes", "No", "Prefer not to say"),
            weights = listOf(30, 0, 15)
        ),
        QuizQuestion(
            id = 4,
            text = "Have you recently experienced clinical symptoms such as persistent unexplained fever, night sweats, or rapid weight loss?",
            options = listOf("Yes", "No", "Unsure"),
            weights = listOf(20, 0, 10)
        ),
        QuizQuestion(
            id = 5,
            text = "Have you had a professional medical HIV check or laboratory blood screening in the last 12 months?",
            options = listOf("Yes, I tested negative", "No, I haven't tested recently", "Prefer not to say"),
            weights = listOf(0, 10, 5)
        )
    )

    private val _quizCurrentIndex = MutableStateFlow(0)
    val quizCurrentIndex = _quizCurrentIndex.asStateFlow()

    private val _quizAnswers = MutableStateFlow<Map<Int, Int>>(emptyMap()) // map of question ID to option index chosen
    val quizAnswers = _quizAnswers.asStateFlow()

    private val _isQuizCompleted = MutableStateFlow(false)
    val isQuizCompleted = _isQuizCompleted.asStateFlow()

    private val _quizRiskScore = MutableStateFlow(-1)
    val quizRiskScore = _quizRiskScore.asStateFlow()

    private val _quizRiskLevel = MutableStateFlow("Not Assessed")
    val quizRiskLevel = _quizRiskLevel.asStateFlow()

    // --- Chat/RAG States ---
    private val _chatHistory = MutableStateFlow<List<Pair<String, String>>>(
        listOf(
            "model" to "Hello! I am the CareLink AI Educator. I can provide accurate, empathetic, and strictly confidential answers regarding HIV prevention, medication adherence (PrEP/PEP/ARVs), testing, and sexual health in Ghana. How can I help you today?"
        )
    )
    val chatHistory = _chatHistory.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading = _isChatLoading.asStateFlow()

    // --- E-commerce/Cart States ---
    private val _cart = MutableStateFlow<Map<Product, Int>>(emptyMap())
    val cart = _cart.asStateFlow()

    private val _prescriptionPhotoPath = MutableStateFlow<String?>(null)
    val prescriptionPhotoPath = _prescriptionPhotoPath.asStateFlow()

    // Simulated active selection for Prescription flow
    private val _selectedProductForRxUpload = MutableStateFlow<Product?>(null)
    val selectedProductForRxUpload = _selectedProductForRxUpload.asStateFlow()

    init {
        // Pre-populate data on first view if empty
        viewModelScope.launch {
            repository.anonymizedProfile.collect { profile ->
                if (profile == null) {
                    val defaultAlias = "CareLink-Partner-" + (1000..9999).random()
                    val defaultHash = generateSha256(UUID.randomUUID().toString()).take(10)
                    repository.saveProfile(
                        AnonymizedUser(
                            aliasName = defaultAlias,
                            hashedId = "cl:$defaultHash",
                            currentRiskScore = -1,
                            riskAssessmentLevel = "Pending"
                        )
                    )
                    prepopulateMockData()
                }
            }
        }
    }

    private suspend fun prepopulateMockData() {
        val dayInMs = 24 * 60 * 60 * 1000L
        val currentTime = System.currentTimeMillis()

        // Add standard medication reminders & pickups
        repository.addReminder(
            MedicationReminder(
                title = "Daily PrEP Preventive Dose",
                timeString = "08:00 AM",
                type = "DAILY_DOSE",
                notes = "Take with water. Safe and effective."
            )
        )
        repository.addReminder(
            MedicationReminder(
                title = "Clinic Refill Pickup",
                timeString = "July 18, 2026",
                type = "CLINIC_PICKUP",
                notes = "Korle Bu Teaching Hospital Pharmacy Dept."
            )
        )

        // Add some mock period logs for women tracking their menstrual wellness
        repository.addPeriodLog(
            PeriodLog(
                dateMillis = currentTime - dayInMs * 28,
                flowIntensity = "Heavy",
                symptoms = "Cramps, Fatigue",
                mood = "Fatigued",
                notes = "Cycle started right on schedule."
            )
        )
        repository.addPeriodLog(
            PeriodLog(
                dateMillis = currentTime - dayInMs * 27,
                flowIntensity = "Medium",
                symptoms = "Cramps",
                mood = "Anxious",
                notes = "Drank raspberry leaf tea."
            )
        )
        repository.addPeriodLog(
            PeriodLog(
                dateMillis = currentTime - dayInMs * 26,
                flowIntensity = "Light",
                symptoms = "None",
                mood = "Happy",
                notes = "Energy levels returning!"
            )
        )

        repository.logMedication(MedicationLog(medType = "PrEP", dateMillis = currentTime - dayInMs * 3, taken = true))
        repository.logMedication(MedicationLog(medType = "PrEP", dateMillis = currentTime - dayInMs * 2, taken = true))
        repository.logMedication(MedicationLog(medType = "PrEP", dateMillis = currentTime - dayInMs * 1, taken = false))

        // Add a mock delivery order in transit
        repository.placeOrder(
            DeliveryOrder(
                itemName = "HIV Self-Test Kit (Discreet Pack)",
                priceGhs = 45.00,
                quantity = 1,
                isPrescriptionRequired = false,
                trackingStatus = "In Transit",
                orderDateMillis = currentTime - dayInMs
            )
        )
    }

    private fun generateSha256(input: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(input.toByteArray(Charsets.UTF_8))
            hash.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            input.hashCode().toString()
        }
    }

    // --- Profile actions ---
    fun updateAlias(newAlias: String) {
        viewModelScope.launch {
            val current = anonymizedProfile.value ?: return@launch
            repository.saveProfile(current.copy(aliasName = newAlias))
        }
    }

    // --- Quiz actions ---
    fun selectQuizAnswer(questionIndex: Int, optionIndex: Int) {
        val question = quizQuestions[questionIndex]
        val updatedAnswers = _quizAnswers.value.toMutableMap()
        updatedAnswers[question.id] = optionIndex
        _quizAnswers.value = updatedAnswers
    }

    fun nextQuizQuestion() {
        if (_quizCurrentIndex.value < quizQuestions.size - 1) {
            _quizCurrentIndex.value += 1
        } else {
            calculateAndSubmitQuiz()
        }
    }

    fun previousQuizQuestion() {
        if (_quizCurrentIndex.value > 0) {
            _quizCurrentIndex.value -= 1
        }
    }

    fun resetQuiz() {
        _quizCurrentIndex.value = 0
        _quizAnswers.value = emptyMap()
        _isQuizCompleted.value = false
        _quizRiskScore.value = -1
        _quizRiskLevel.value = "Not Assessed"
    }

    private fun calculateAndSubmitQuiz() {
        val answers = _quizAnswers.value
        var scoreSum = 0
        quizQuestions.forEach { question ->
            val chosenOptionIndex = answers[question.id] ?: 1 // default to no
            val weight = question.weights.getOrNull(chosenOptionIndex) ?: 0
            scoreSum += weight
        }

        // Cap score at 100
        val finalScore = scoreSum.coerceAtMost(100)
        val finalLevel = when {
            finalScore <= 30 -> "Low Risk"
            finalScore <= 65 -> "Medium Risk"
            else -> "High Risk"
        }

        _quizRiskScore.value = finalScore
        _quizRiskLevel.value = finalLevel
        _isQuizCompleted.value = true

        // Update profile
        viewModelScope.launch {
            val current = anonymizedProfile.value ?: return@launch
            repository.saveProfile(
                current.copy(
                    currentRiskScore = finalScore,
                    riskAssessmentLevel = finalLevel,
                    quizCompletedAt = System.currentTimeMillis()
                )
            )
        }
    }

    // --- Chat actions ---
    fun sendChatPrompt(prompt: String) {
        if (prompt.isBlank() || _isChatLoading.value) return

        // Add user message immediately
        val currentHistory = _chatHistory.value.toMutableList()
        currentHistory.add("user" to prompt)
        _chatHistory.value = currentHistory

        _isChatLoading.value = true

        viewModelScope.launch {
            // Get reply from service (this handles the system prompt under the hood)
            val reply = GeminiService.getChatResponse(prompt, currentHistory.dropLast(1))
            
            val updatedHistory = _chatHistory.value.toMutableList()
            updatedHistory.add("model" to reply)
            _chatHistory.value = updatedHistory
            _isChatLoading.value = false
        }
    }

    fun clearChat() {
        _chatHistory.value = listOf(
            "model" to "Hello! I am the CareLink AI Educator. Ask me any question confidentially about sexual health, medication adherence, or prevention in Ghana."
        )
    }

    // --- Cart & Store Actions ---
    fun setProductForRxUpload(product: Product?) {
        _selectedProductForRxUpload.value = product
        if (product == null) {
            _prescriptionPhotoPath.value = null
        }
    }

    fun uploadPrescriptionSimulated(fileName: String = "GHS_Rx_${(1000..9999).random()}.jpg") {
        _prescriptionPhotoPath.value = "/simulated/prescription/$fileName"
    }

    fun addProductToCart(product: Product) {
        val currentCart = _cart.value.toMutableMap()
        val count = currentCart[product] ?: 0
        currentCart[product] = count + 1
        _cart.value = currentCart
    }

    fun removeProductFromCart(product: Product) {
        val currentCart = _cart.value.toMutableMap()
        val count = currentCart[product] ?: 0
        if (count <= 1) {
            currentCart.remove(product)
        } else {
            currentCart[product] = count - 1
        }
        _cart.value = currentCart
    }

    fun clearCart() {
        _cart.value = emptyMap()
        _prescriptionPhotoPath.value = null
        _selectedProductForRxUpload.value = null
    }

    fun checkoutCart(disguiseName: String = "Standard Unbranded Box", disguisePrice: Double = 0.0) {
        val currentCart = _cart.value
        if (currentCart.isEmpty()) return

        viewModelScope.launch {
            currentCart.forEach { (product, qty) ->
                repository.placeOrder(
                    DeliveryOrder(
                        itemName = "${product.name} ($disguiseName)",
                        priceGhs = product.priceGhs,
                        quantity = qty,
                        isPrescriptionRequired = product.isPrescriptionRequired,
                        prescriptionPhotoPath = if (product.isPrescriptionRequired) _prescriptionPhotoPath.value ?: "Prescription Verified Online" else null,
                        trackingStatus = "Order Placed"
                    )
                )
            }
            if (disguisePrice > 0.0) {
                repository.placeOrder(
                    DeliveryOrder(
                        itemName = "$disguiseName Packaging Disguise",
                        priceGhs = disguisePrice,
                        quantity = 1,
                        isPrescriptionRequired = false,
                        trackingStatus = "Order Placed"
                    )
                )
            }
            clearCart()
        }
    }

    // --- Medication Reminders Actions ---
    fun addReminder(title: String, timeString: String, type: String, notes: String) {
        viewModelScope.launch {
            repository.addReminder(
                MedicationReminder(
                    title = title,
                    timeString = timeString,
                    type = type,
                    notes = notes,
                    isCompleted = false
                )
            )
        }
    }

    fun updateReminderStatus(id: Int, completed: Boolean) {
        viewModelScope.launch {
            repository.updateReminderStatus(id, completed)
        }
    }

    fun deleteReminder(id: Int) {
        viewModelScope.launch {
            repository.deleteReminder(id)
        }
    }

    fun logMedicationIntake(medType: String, dateMillis: Long, taken: Boolean) {
        viewModelScope.launch {
            repository.logMedication(
                MedicationLog(
                    medType = medType,
                    dateMillis = dateMillis,
                    taken = taken
                )
            )
        }
    }

    // --- Theme & Disguise Actions ---
    fun updateSelectedTheme(themeName: String) {
        viewModelScope.launch {
            val current = anonymizedProfile.value ?: return@launch
            repository.saveProfile(current.copy(selectedTheme = themeName))
        }
    }

    fun toggleDisguiseMode(isDisguised: Boolean) {
        viewModelScope.launch {
            val current = anonymizedProfile.value ?: return@launch
            repository.saveProfile(current.copy(isDisguised = isDisguised))
        }
    }

    fun updateSelectedIcon(iconName: String) {
        viewModelScope.launch {
            val current = anonymizedProfile.value ?: return@launch
            repository.saveProfile(current.copy(selectedIcon = iconName))
        }
    }

    // --- Period Tracker Actions ---
    fun addPeriodLog(dateMillis: Long, flowIntensity: String, symptoms: String, mood: String, notes: String) {
        viewModelScope.launch {
            repository.addPeriodLog(
                PeriodLog(
                    dateMillis = dateMillis,
                    flowIntensity = flowIntensity,
                    symptoms = symptoms,
                    mood = mood,
                    notes = notes
                )
            )
        }
    }

    fun deletePeriodLog(id: Int) {
        viewModelScope.launch {
            repository.deletePeriodLog(id)
        }
    }
}

class CareLinkViewModelFactory(private val repository: CareLinkRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CareLinkViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CareLinkViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
