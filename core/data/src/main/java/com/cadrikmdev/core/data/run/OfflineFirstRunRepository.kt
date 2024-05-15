package com.cadrikmdev.core.data.run

import com.cadrikmdev.core.domain.run.LocalRunDataSource
import com.cadrikmdev.core.domain.run.RemoteRunDataSource
import com.cadrikmdev.core.domain.run.Run
import com.cadrikmdev.core.domain.run.RunId
import com.cadrikmdev.core.domain.run.RunRepository
import com.cadrikmdev.core.domain.util.DataError
import com.cadrikmdev.core.domain.util.EmptyResult
import com.cadrikmdev.core.domain.util.Result
import com.cadrikmdev.core.domain.util.asEmptyDataResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow

class OfflineFirstRunRepository(
    private val localRunDataSource: LocalRunDataSource,
    private val remoteRunDataSource: RemoteRunDataSource,
    private val applicationScope: CoroutineScope,
) : RunRepository {
    override fun getRuns(): Flow<List<Run>> {
        return localRunDataSource.getRuns()
    }

    override suspend fun fetchRuns(): EmptyResult<DataError> {
        return when (val result = remoteRunDataSource.getRuns()) {
            is Result.Error -> result.asEmptyDataResult()
            is Result.Success -> {
                // to keep database updated in case coroutine has been cancelled, right after getting results and starting to update DB
                applicationScope.async {
                    localRunDataSource.upsertRuns(result.data).asEmptyDataResult()
                }.await()
            }
        }
    }

    override suspend fun upsertRun(run: Run, mapPicture: ByteArray): EmptyResult<DataError> {
        val localResult = localRunDataSource.upsertRun(run)
        if (localResult !is Result.Success) {
            return localResult.asEmptyDataResult()
        }
        val runWithId = run.copy(id = localResult.data)
        val remoteResult = remoteRunDataSource.postRun(
            run = runWithId,
            mapPicture = mapPicture
        )
        return when (remoteResult) {
            is Result.Error -> {
                Result.Success(Unit) // todo: finish later
            }

            is Result.Success -> {
                applicationScope.async {
                    localRunDataSource.upsertRun(remoteResult.data).asEmptyDataResult()
                }.await()
            }
        }
    }

    override suspend fun deleteRun(id: RunId): EmptyResult<DataError> {
        localRunDataSource.deleteRun(id)

        return applicationScope.async {
            remoteRunDataSource.deleteRun(id)
        }.await()
    }
}