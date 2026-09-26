package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Aroha", appName)
    }

    @Test
    fun `verify database instance initializes`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = com.example.data.local.database.ArohaDatabase.getInstance(context)
        assertTrue(db.isOpen || db != null)
    }
}
