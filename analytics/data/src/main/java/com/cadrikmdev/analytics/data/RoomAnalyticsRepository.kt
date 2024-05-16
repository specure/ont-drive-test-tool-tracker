package com.cadrikmdev.analytics.data

import com.cadrikmdev.analytics.domain.AnalyticsRepository
import com.cadrikmdev.analytics.domain.AnalyticsValues
import com.cadrikmdev.core.database.dao.AnalyticsDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

class RoomAnalyticsRepository(
    private val analyticsDao: AnalyticsDao
) : AnalyticsRepository {
    override suspend fun getAnalyticsValues(): AnalyticsValues {
        return withContext(Dispatchers.IO) {
            val totalDistance = async { analyticsDao.getTotalDistance() }
            val totalTimeMillis = async { analyticsDao.getTotalTimeRun() }
            val maxRunSpeed = async { analyticsDao.getMaxRunSpeed() }
            val avgDistanceRun = async { analyticsDao.getAvgDistancePerRun() }
            val avgPaceRun = async { analyticsDao.getAvgPacePerRun() }

            AnalyticsValues(
                totalDistanceRun = totalDistance.await(),
                totalTimeRun = totalTimeMillis.await().milliseconds,
                fastestEverRun = maxRunSpeed.await(),
                avgDistancePerRun = avgDistanceRun.await(),
                avgPacePerRun = avgPaceRun.await()
            )
        }
    }
}