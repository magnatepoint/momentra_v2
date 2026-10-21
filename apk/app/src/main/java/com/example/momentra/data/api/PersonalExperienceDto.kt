package com.example.momentra.data.api

import com.google.gson.annotations.SerializedName

data class PersonalExperienceDto(
    @SerializedName("currencyCode") val currencyCode: String? = null,
    @SerializedName("expenseCount") val expenseCount: Int = 0,
    @SerializedName("cold") val cold: Boolean = true,
    @SerializedName("pulse") val pulse: PersonalPulseExperienceDto? = null,
    @SerializedName("moments") val moments: PersonalMomentsExperienceDto? = null,
    @SerializedName("life") val life: PersonalLifeExperienceDto? = null,
    @SerializedName("memory") val memory: PersonalMemoryExperienceDto? = null,
)

data class PersonalPulseExperienceDto(
    @SerializedName("intro") val intro: PersonalPulseIntroDto? = null,
    @SerializedName("recent") val recent: List<PersonalActivityRowDto>? = emptyList(),
    @SerializedName("attention") val attention: PersonalAttentionExperienceDto? = null,
    @SerializedName("snapshot") val snapshot: PersonalSnapshotDto? = null,
    @SerializedName("signals") val signals: List<PersonalSignalDto>? = emptyList(),
    @SerializedName("signalsPrompt") val signalsPrompt: String? = null,
    @SerializedName("signalsMessage") val signalsMessage: String? = null,
    @SerializedName("mood") val mood: PersonalMoodPulseDto? = null,
)

data class PersonalPulseIntroDto(
    @SerializedName("kicker") val kicker: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("subtitle") val subtitle: String = "",
)

data class PersonalActivityRowDto(
    @SerializedName("expenseId") val expenseId: String,
    @SerializedName("merchantName") val merchantName: String,
    @SerializedName("amount") val amount: String,
    @SerializedName("currencyCode") val currencyCode: String,
    @SerializedName("effectiveAt") val effectiveAt: String,
    @SerializedName("moodCode") val moodCode: String? = null,
    @SerializedName("moodLabel") val moodLabel: String? = null,
    @SerializedName("areaCode") val areaCode: String,
    @SerializedName("areaLabel") val areaLabel: String,
    @SerializedName("subcategoryCode") val subcategoryCode: String,
    @SerializedName("subcategoryLabel") val subcategoryLabel: String,
    @SerializedName("iconKey") val iconKey: String,
)

data class PersonalAttentionExperienceDto(
    @SerializedName("quiet") val quiet: Boolean = true,
    @SerializedName("issueCount") val issueCount: Int = 0,
    @SerializedName("message") val message: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("body") val body: String = "",
)

data class PersonalSnapshotDto(
    @SerializedName("periodLabel") val periodLabel: String = "",
    @SerializedName("rangeLabel") val rangeLabel: String = "",
    @SerializedName("total") val total: String = "0.00",
    @SerializedName("expenseCount") val expenseCount: Int = 0,
    @SerializedName("previousTotal") val previousTotal: String? = null,
    @SerializedName("previousLabel") val previousLabel: String? = null,
    @SerializedName("previousRangeLabel") val previousRangeLabel: String? = null,
    @SerializedName("previousExpenseCount") val previousExpenseCount: Int = 0,
    @SerializedName("comparable") val comparable: Boolean = false,
    @SerializedName("deltaAmount") val deltaAmount: String? = null,
    @SerializedName("deltaRatio") val deltaRatio: Double? = null,
    @SerializedName("comparisonNote") val comparisonNote: String = "",
    @SerializedName("topAreaLabel") val topAreaLabel: String? = null,
    @SerializedName("topAreaAmount") val topAreaAmount: String? = null,
    @SerializedName("currentShort") val currentShort: String = "",
    @SerializedName("previousShort") val previousShort: String? = null,
)

data class PersonalSignalDto(
    @SerializedName("areaLabel") val areaLabel: String,
    @SerializedName("subcategoryLabel") val subcategoryLabel: String = "",
    @SerializedName("current") val current: String,
    @SerializedName("previous") val previous: String,
    @SerializedName("currentCount") val currentCount: Int = 0,
    @SerializedName("previousCount") val previousCount: Int = 0,
    @SerializedName("direction") val direction: String,
    @SerializedName("sentence") val sentence: String = "",
)

data class PersonalMoodPulseDto(
    @SerializedName("responseCount") val responseCount: Int = 0,
    @SerializedName("expenseCount") val expenseCount: Int = 0,
    @SerializedName("coverage") val coverage: Double? = null,
    @SerializedName("insufficient") val insufficient: Boolean = true,
    @SerializedName("message") val message: String = "",
    @SerializedName("counts") val counts: List<PersonalMoodCountDto>? = emptyList(),
)

data class PersonalMoodCountDto(
    @SerializedName("moodCode") val moodCode: String,
    @SerializedName("label") val label: String,
    @SerializedName("count") val count: Int,
)

data class PersonalMomentCardDto(
    @SerializedName("id") val id: String,
    @SerializedName("state") val state: String,
    @SerializedName("title") val title: String,
    @SerializedName("areaCode") val areaCode: String,
    @SerializedName("areaLabel") val areaLabel: String,
    @SerializedName("subcategoryCode") val subcategoryCode: String,
    @SerializedName("subcategoryLabel") val subcategoryLabel: String,
    @SerializedName("spend") val spend: String,
    @SerializedName("activityCount") val activityCount: Int,
    @SerializedName("lastActivityAt") val lastActivityAt: String,
    @SerializedName("statusLabel") val statusLabel: String = "",
    @SerializedName("summary") val summary: String = "",
    @SerializedName("rangeLabel") val rangeLabel: String = "",
    @SerializedName("artworkKey") val artworkKey: String? = null,
    @SerializedName("tentative") val tentative: Boolean = false,
)

data class PersonalMomentsExperienceDto(
    @SerializedName("intro") val intro: PersonalPulseIntroDto? = null,
    @SerializedName("heroMonth") val heroMonth: String? = null,
    @SerializedName("heroEvidence") val heroEvidence: String? = null,
    @SerializedName("activeSubtitle") val activeSubtitle: String? = null,
    @SerializedName("emergingSubtitle") val emergingSubtitle: String? = null,
    @SerializedName("progressSubtitle") val progressSubtitle: String? = null,
    @SerializedName("recentSubtitle") val recentSubtitle: String? = null,
    @SerializedName("active") val active: List<PersonalMomentCardDto>? = emptyList(),
    @SerializedName("emerging") val emerging: List<PersonalMomentCardDto>? = emptyList(),
    @SerializedName("progress") val progress: PersonalMomentProgressDto? = null,
    @SerializedName("recent") val recent: List<PersonalMomentCardDto>? = emptyList(),
    @SerializedName("insight") val insight: PersonalInsightDto? = null,
)

data class PersonalMomentProgressDto(
    @SerializedName("title") val title: String,
    @SerializedName("areaLabel") val areaLabel: String = "",
    @SerializedName("subcategoryLabel") val subcategoryLabel: String = "",
    @SerializedName("bars") val bars: List<PersonalProgressBarDto>? = emptyList(),
    @SerializedName("cumulativeLabel") val cumulativeLabel: String = "",
    @SerializedName("footnote") val footnote: String = "",
    @SerializedName("message") val message: String = "",
)

data class PersonalProgressBarDto(
    @SerializedName("effectiveAt") val effectiveAt: String,
    @SerializedName("amount") val amount: String,
    @SerializedName("label") val label: String = "",
)

data class PersonalInsightDto(
    @SerializedName("title") val title: String? = null,
    @SerializedName("text") val text: String = "",
    @SerializedName("sufficient") val sufficient: Boolean = false,
    @SerializedName("actionLabel") val actionLabel: String? = null,
    @SerializedName("footnote") val footnote: String = "",
)

data class PersonalLifeIntroDto(
    @SerializedName("kicker") val kicker: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("subtitle") val subtitle: String = "",
)

data class PersonalLifeAreaSpendDto(
    @SerializedName("areaCode") val areaCode: String,
    @SerializedName("label") val label: String,
    @SerializedName("layer") val layer: String,
    @SerializedName("iconKey") val iconKey: String,
    @SerializedName("spend") val spend: String,
    @SerializedName("activityCount") val activityCount: Int,
    @SerializedName("share") val share: Double = 0.0,
)

data class PersonalNowBuildDto(
    @SerializedName("now") val now: String = "0.00",
    @SerializedName("build") val build: String = "0.00",
    @SerializedName("nowShare") val nowShare: Double? = null,
    @SerializedName("buildShare") val buildShare: Double? = null,
)

data class PersonalMoneySliceDto(
    @SerializedName("subcategoryCode") val subcategoryCode: String,
    @SerializedName("label") val label: String,
    @SerializedName("areaLabel") val areaLabel: String = "",
    @SerializedName("spend") val spend: String,
    @SerializedName("share") val share: Double,
)

data class PersonalTrendDto(
    @SerializedName("label") val label: String,
    @SerializedName("areaLabel") val areaLabel: String = "",
    @SerializedName("direction") val direction: String,
    @SerializedName("detail") val detail: String,
    @SerializedName("previous") val previous: String = "0.00",
    @SerializedName("current") val current: String = "0.00",
    @SerializedName("previousCount") val previousCount: Int = 0,
    @SerializedName("currentCount") val currentCount: Int = 0,
    @SerializedName("previousShort") val previousShort: String = "",
    @SerializedName("currentShort") val currentShort: String = "",
)

data class PersonalAreaMoodDto(
    @SerializedName("areaLabel") val areaLabel: String,
    @SerializedName("moodLabel") val moodLabel: String? = null,
    @SerializedName("responseCount") val responseCount: Int = 0,
    @SerializedName("expenseCount") val expenseCount: Int = 0,
    @SerializedName("noMoodRecorded") val noMoodRecorded: Boolean = false,
    @SerializedName("responses") val responses: List<String>? = emptyList(),
)

data class PersonalLifeExperienceDto(
    @SerializedName("intro") val intro: PersonalLifeIntroDto? = null,
    @SerializedName("total") val total: String = "0.00",
    @SerializedName("expenseCount") val expenseCount: Int = 0,
    @SerializedName("monthLabel") val monthLabel: String = "",
    @SerializedName("rangeLabel") val rangeLabel: String = "",
    @SerializedName("coverageLabel") val coverageLabel: String = "",
    @SerializedName("highlightedAreaCode") val highlightedAreaCode: String? = null,
    @SerializedName("areas") val areas: List<PersonalLifeAreaSpendDto>? = emptyList(),
    @SerializedName("nowBuild") val nowBuild: PersonalNowBuildDto? = null,
    @SerializedName("moneyGoes") val moneyGoes: List<PersonalMoneySliceDto>? = emptyList(),
    @SerializedName("otherCount") val otherCount: Int = 0,
    @SerializedName("otherSpend") val otherSpend: String = "0.00",
    @SerializedName("otherShare") val otherShare: Double = 0.0,
    @SerializedName("topFiveSpend") val topFiveSpend: String = "0.00",
    @SerializedName("topFiveShare") val topFiveShare: Double = 0.0,
    @SerializedName("trends") val trends: List<PersonalTrendDto>? = emptyList(),
    @SerializedName("trendsMessage") val trendsMessage: String? = null,
    @SerializedName("trendsFootnote") val trendsFootnote: String? = null,
    @SerializedName("moodSummary") val moodSummary: String = "",
    @SerializedName("moodCoverageLabel") val moodCoverageLabel: String = "",
    @SerializedName("moodFootnote") val moodFootnote: String = "",
    @SerializedName("moodByArea") val moodByArea: List<PersonalAreaMoodDto>? = emptyList(),
)

data class PersonalTimelineItemDto(
    @SerializedName("expenseId") val expenseId: String,
    @SerializedName("title") val title: String,
    @SerializedName("effectiveAt") val effectiveAt: String,
    @SerializedName("detail") val detail: String,
    @SerializedName("kicker") val kicker: String = "",
    @SerializedName("period") val period: String = "",
)

data class PersonalMonthlyMemoryDto(
    @SerializedName("label") val label: String,
    @SerializedName("shortLabel") val shortLabel: String = "",
    @SerializedName("year") val year: Int = 0,
    @SerializedName("closedLabel") val closedLabel: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("spend") val spend: String,
    @SerializedName("expenseCount") val expenseCount: Int,
    @SerializedName("completedCount") val completedCount: Int = 0,
    @SerializedName("moodCount") val moodCount: Int = 0,
    @SerializedName("moodCoverage") val moodCoverage: Double = 0.0,
    @SerializedName("periodCaption") val periodCaption: String = "",
    @SerializedName("summary") val summary: String,
    @SerializedName("openLabel") val openLabel: String = "",
)

data class PersonalMilestoneDto(
    @SerializedName("title") val title: String,
    @SerializedName("effectiveAt") val effectiveAt: String,
    @SerializedName("detail") val detail: String,
    @SerializedName("areaLabel") val areaLabel: String = "",
    @SerializedName("subcategoryLabel") val subcategoryLabel: String = "",
    @SerializedName("spend") val spend: String = "",
    @SerializedName("activityCount") val activityCount: Int = 0,
    @SerializedName("confirmedLabel") val confirmedLabel: String = "",
)

data class PersonalLookBackDto(
    @SerializedName("title") val title: String,
    @SerializedName("detail") val detail: String,
    @SerializedName("effectiveAt") val effectiveAt: String,
    @SerializedName("kicker") val kicker: String = "",
)

data class PersonalMemoryExperienceDto(
    @SerializedName("intro") val intro: PersonalLifeIntroDto? = null,
    @SerializedName("openMonthShort") val openMonthShort: String = "",
    @SerializedName("openMonthYear") val openMonthYear: Int = 0,
    @SerializedName("openMonthChip") val openMonthChip: String = "",
    @SerializedName("openRecap") val openRecap: String = "",
    @SerializedName("openMonthNote") val openMonthNote: String = "",
    @SerializedName("timelineOpenKicker") val timelineOpenKicker: String = "",
    @SerializedName("currentFilterLabel") val currentFilterLabel: String = "",
    @SerializedName("timeline") val timeline: List<PersonalTimelineItemDto>? = emptyList(),
    @SerializedName("completed") val completed: List<PersonalMomentCardDto>? = emptyList(),
    @SerializedName("monthly") val monthly: PersonalMonthlyMemoryDto? = null,
    @SerializedName("milestones") val milestones: List<PersonalMilestoneDto>? = emptyList(),
    @SerializedName("lookBack") val lookBack: PersonalLookBackDto? = null,
    @SerializedName("lookBackMessage") val lookBackMessage: String? = null,
)

data class PersonalLifeExpenseBody(
    @SerializedName("amount") val amount: String,
    @SerializedName("currencyCode") val currencyCode: String,
    @SerializedName("merchantName") val merchantName: String?,
    @SerializedName("effectiveAt") val effectiveAt: String?,
    @SerializedName("note") val note: String?,
    @SerializedName("lifeAreaCode") val lifeAreaCode: String,
    @SerializedName("lifeSubcategoryCode") val lifeSubcategoryCode: String,
    @SerializedName("moodCode") val moodCode: String?,
    @SerializedName("classificationSource") val classificationSource: String = "manual",
    @SerializedName("origin") val origin: String = "QUICK_ADD",
    @SerializedName("paymentSource") val paymentSource: String?,
    @SerializedName("economicKind") val economicKind: String = "EXPENSE",
    @SerializedName("photo") val photo: PersonalExpensePhotoBody? = null,
)

data class PersonalExpensePhotoBody(
    @SerializedName("contentType") val contentType: String,
    @SerializedName("dataBase64") val dataBase64: String,
)

data class PersonalLifeExpenseCorrectionBody(
    @SerializedName("lifeAreaCode") val lifeAreaCode: String,
    @SerializedName("lifeSubcategoryCode") val lifeSubcategoryCode: String,
    @SerializedName("moodCode") val moodCode: String?,
    @SerializedName("merchantName") val merchantName: String?,
    @SerializedName("photo") val photo: PersonalExpensePhotoBody? = null,
)

data class PersonalExpenseConfirmationDto(
    @SerializedName("expenseId") val expenseId: String,
    @SerializedName("momentId") val momentId: String,
    @SerializedName("amount") val amount: String,
    @SerializedName("currencyCode") val currencyCode: String,
    @SerializedName("merchantName") val merchantName: String? = null,
    @SerializedName("effectiveAt") val effectiveAt: String,
    @SerializedName("lifeAreaCode") val lifeAreaCode: String,
    @SerializedName("lifeAreaLabel") val lifeAreaLabel: String,
    @SerializedName("lifeSubcategoryCode") val lifeSubcategoryCode: String,
    @SerializedName("lifeSubcategoryLabel") val lifeSubcategoryLabel: String,
    @SerializedName("layer") val layer: String,
    @SerializedName("moodCode") val moodCode: String? = null,
    @SerializedName("moodLabel") val moodLabel: String? = null,
    @SerializedName("monthTotal") val monthTotal: String,
    @SerializedName("monthExpenseCount") val monthExpenseCount: Int,
    @SerializedName("areaMonthTotal") val areaMonthTotal: String,
    @SerializedName("areaMonthCount") val areaMonthCount: Int,
    @SerializedName("moodResponses") val moodResponses: Int,
    @SerializedName("moodCoverage") val moodCoverage: Double? = null,
    @SerializedName("version") val version: Long = 1,
    @SerializedName("photoUploadId") val photoUploadId: String? = null,
)

data class PersonalLifeRefundBody(
    @SerializedName("amount") val amount: String? = null,
)

data class PersonalLifeRefundDto(
    @SerializedName("expenseId") val expenseId: String,
    @SerializedName("reversedExpenseId") val reversedExpenseId: String,
    @SerializedName("amount") val amount: String,
)

data class PersonalLifeVoidDto(
    @SerializedName("expenseId") val expenseId: String,
    @SerializedName("status") val status: String,
)
