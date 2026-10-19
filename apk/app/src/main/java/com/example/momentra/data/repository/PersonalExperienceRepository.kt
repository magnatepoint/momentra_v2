package com.example.momentra.data.repository

import com.example.momentra.data.api.ApiClient
import com.example.momentra.data.api.ApiService
import com.example.momentra.data.api.PersonalExperienceDto
import com.example.momentra.data.api.PersonalExpenseConfirmationDto
import com.example.momentra.data.api.PersonalLifeExpenseBody
import com.example.momentra.data.api.PersonalLifeExpenseCorrectionBody
import com.example.momentra.data.api.PersonalLifeRefundBody
import java.util.UUID

class PersonalExperienceRepository(
    private val api: ApiService = ApiClient.apiService,
) {
    suspend fun experience(): PersonalExperienceDto = api.getPersonalExperience().data

    suspend fun create(body: PersonalLifeExpenseBody): PersonalExpenseConfirmationDto =
        api.createPersonalLifeExpense(UUID.randomUUID().toString(), body).data

    suspend fun correct(expenseId: String, body: PersonalLifeExpenseCorrectionBody): PersonalExpenseConfirmationDto =
        api.correctPersonalLifeExpense(expenseId, UUID.randomUUID().toString(), body).data

    suspend fun refund(expenseId: String): Unit {
        api.refundPersonalLifeExpense(expenseId, UUID.randomUUID().toString(), PersonalLifeRefundBody())
    }

    suspend fun voidExpense(expenseId: String): Unit {
        api.voidPersonalLifeExpense(expenseId, UUID.randomUUID().toString(), PersonalLifeRefundBody())
    }
}
