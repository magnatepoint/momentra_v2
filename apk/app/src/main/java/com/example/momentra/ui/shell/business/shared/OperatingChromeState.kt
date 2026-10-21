package com.example.momentra.ui.shell.business.shared

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.momentra.data.api.OperatingActivityDto
import com.example.momentra.data.api.OperatingPaymentDto

object OperatingChromeState {
    var participantNames by mutableStateOf<List<String>>(emptyList())
    var editing by mutableStateOf<OperatingActivityDto?>(null)
    var settling by mutableStateOf<OperatingPaymentDto?>(null)
    var record: (suspend (OperatingCaptureDraft) -> Unit)? = null
}
