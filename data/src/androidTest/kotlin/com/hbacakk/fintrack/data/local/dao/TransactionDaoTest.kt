package com.hbacakk.fintrack.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hbacakk.fintrack.data.local.database.FinTrackDatabase
import com.hbacakk.fintrack.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Room veritabanını GERÇEK bir Android ortamında test ediyoruz.
 *
 * inMemoryDatabaseBuilder: testler için diske yazmayan,
 * sadece bellekte yaşayan bir veritabanı oluşturur.
 * Her test sonunda temizlenir, testler birbirini etkilemez.
 *
 * Not: Bu test JUnit4 kullanıyor (JUnit5 değil) çünkü
 * AndroidJUnit4 runner'ı henüz JUnit5 ile resmi destek sunmuyor —
 * bu, Android instrumented testlerinde yaygın bir kısıtlamadır.
 */
@RunWith(AndroidJUnit4::class)
class TransactionDaoTest {

    private lateinit var database: FinTrackDatabase
    private lateinit var dao: TransactionDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(context, FinTrackDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.transactionDao()
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndGetById_returnsCorrectTransaction() = runBlocking {
        val entity = fakeEntity()
        dao.insert(entity)

        val result = dao.getById(entity.id)

        assertEquals(entity.id, result?.id)
        assertEquals(entity.amount, result?.amount)
    }

    @Test
    fun observeAll_emitsInsertedTransactions() = runBlocking {
        val entity = fakeEntity()
        dao.insert(entity)

        val result = dao.observeAll().first()

        assertEquals(1, result.size)
        assertEquals(entity.id, result.first().id)
    }

    @Test
    fun deleteById_removesTransaction() = runBlocking {
        val entity = fakeEntity()
        dao.insert(entity)

        dao.deleteById(entity.id)

        val result = dao.getById(entity.id)
        assertEquals(null, result)
    }

    @Test
    fun getUnsynced_returnsOnlyUnsyncedTransactions() = runBlocking {
        val synced = fakeEntity(id = "tx-1").copy(isSynced = true)
        val unsynced = fakeEntity(id = "tx-2").copy(isSynced = false)
        dao.insert(synced)
        dao.insert(unsynced)

        val result = dao.getUnsynced()

        assertEquals(1, result.size)
        assertEquals("tx-2", result.first().id)
    }

    @Test
    fun markAsSynced_updatesIsSyncedFlag() = runBlocking {
        val entity = fakeEntity()
        dao.insert(entity)

        dao.markAsSynced(entity.id)

        val result = dao.getById(entity.id)
        assertTrue(result?.isSynced == true)
    }

    private fun fakeEntity(id: String = "tx-test") = TransactionEntity(
        id = id,
        amount = 150.0,
        type = "EXPENSE",
        category = "FOOD",
        description = "Test işlemi",
        date = 1718000000000L,
        accountId = "acc-test",
        isRecurring = false,
        isSynced = false,
    )
}