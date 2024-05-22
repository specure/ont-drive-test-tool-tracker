package com.cadrikmdev.core.domain.networking.mobile

enum class PrimaryDataSubscription(

    /**
     * Value that inform us about subscription ID type
     */
    val value: String
) {
    /**
     * Cell belongs to subscription ID which is primary data subscription
     */
    TRUE("true"),

    /**
     * Cell belongs to subscription ID which is not primary data subscription
     */
    FALSE("false"),

    /**
     * Unable to retrieve valid information about subscription ID or primary data subscription ID
     */
    UNKNOWN("unknown");

    companion object {

        private const val INVALID_SUBSCRIPTION_ID = -1
        fun fromString(type: String): PrimaryDataSubscription {
            values().forEach {
                if (it.value == type) {
                    return it
                }
            }
            return UNKNOWN
        }

        fun resolvePrimaryDataSubscriptionID(
            dataSubscriptionId: Int,
            cellSubscriptionId: Int?
        ): PrimaryDataSubscription {
            return when {
                dataSubscriptionId == INVALID_SUBSCRIPTION_ID || cellSubscriptionId == INVALID_SUBSCRIPTION_ID -> PrimaryDataSubscription.UNKNOWN
                dataSubscriptionId != INVALID_SUBSCRIPTION_ID && cellSubscriptionId == dataSubscriptionId -> PrimaryDataSubscription.TRUE
                else -> PrimaryDataSubscription.FALSE
            }
        }
    }
}