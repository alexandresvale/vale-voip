package com.valevoip.core.domain.usecase

import com.valevoip.core.domain.model.CallDirection
import com.valevoip.core.domain.model.CallHistoryItem
import com.valevoip.core.domain.model.CallHistoryStatus
import com.valevoip.core.domain.repository.CallHistoryRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CallHistoryUseCasesTest {

    private lateinit var fakeRepository: FakeCallHistoryRepository
    private lateinit var clearCallHistoryUseCase: ClearCallHistoryUseCase
    private lateinit var getCallHistoryUseCase: GetCallHistoryUseCase
    private lateinit var deleteCallHistoryItemUseCase: DeleteCallHistoryItemUseCase

    @Before
    fun setUp() {
        fakeRepository = FakeCallHistoryRepository()
        clearCallHistoryUseCase = ClearCallHistoryUseCase(fakeRepository)
        getCallHistoryUseCase = GetCallHistoryUseCase(fakeRepository)
        deleteCallHistoryItemUseCase = DeleteCallHistoryItemUseCase(fakeRepository)
    }

    @Test
    fun `getCallHistory returns items from repository`() = runTest {
        fakeRepository.items.add(
            CallHistoryItem(
                id = "call-1",
                remoteAddress = "1001",
                displayName = "Suporte",
                direction = CallDirection.INCOMING,
                status = CallHistoryStatus.SUCCESS,
                timestamp = 1000L,
                durationSeconds = 60
            )
        )

        val result = getCallHistoryUseCase()

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("1001", result.getOrNull()?.first()?.remoteAddress)
    }

    @Test
    fun `clearCallHistory clears all items in repository`() = runTest {
        fakeRepository.items.addAll(
            listOf(
                CallHistoryItem("1", "1001", null, CallDirection.INCOMING, CallHistoryStatus.SUCCESS, 1000L, 30),
                CallHistoryItem("2", "1002", null, CallDirection.OUTGOING, CallHistoryStatus.MISSED, 2000L, 0)
            )
        )

        val result = clearCallHistoryUseCase()

        assertTrue(result.isSuccess)
        assertTrue(fakeRepository.items.isEmpty())
    }

    @Test
    fun `deleteCallHistoryItem removes specific item`() = runTest {
        fakeRepository.items.addAll(
            listOf(
                CallHistoryItem("1", "1001", null, CallDirection.INCOMING, CallHistoryStatus.SUCCESS, 1000L, 30),
                CallHistoryItem("2", "1002", null, CallDirection.OUTGOING, CallHistoryStatus.MISSED, 2000L, 0)
            )
        )

        val result = deleteCallHistoryItemUseCase("1")

        assertTrue(result.isSuccess)
        assertEquals(1, fakeRepository.items.size)
        assertEquals("2", fakeRepository.items.first().id)
    }

    private class FakeCallHistoryRepository : CallHistoryRepository {
        val items = mutableListOf<CallHistoryItem>()

        override suspend fun getCallHistory(): Result<List<CallHistoryItem>> {
            return Result.success(items.toList())
        }

        override suspend fun deleteCallHistoryItem(id: String): Result<Unit> {
            val removed = items.removeAll { it.id == id }
            return if (removed) Result.success(Unit) else Result.failure(NoSuchElementException())
        }

        override suspend fun clearCallHistory(): Result<Unit> {
            items.clear()
            return Result.success(Unit)
        }
    }
}
