package com.example.watertracker
import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.room.Room
import com.example.watertracker.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainApplication : Application() {
    var totalWater: Int = 0
    var cupChecked = mutableListOf(false, false, false, false, false, false, false, false)

    val db: AppDatabase by lazy {
        Room.databaseBuilder(applicationContext, AppDatabase::class.java, "AppDatabase").build()
    }
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun syncWithDatabase(onComplete: () -> Unit = {}) {
        applicationScope.launch {
            val waterDao = db.waterlogDao()
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val savedDate = waterDao.getDate()

            if (savedDate != currentDate) {
                totalWater = 0
                waterDao.resetAllCups()
                for (i in 0..7) {
                    cupChecked[i] = false
                }
                waterDao.updateDate(currentDate)
            } else {
                totalWater = waterDao.getCount()
                for (i in 0..7) {
                    cupChecked[i] = waterDao.getVal(i + 1)
                }
            }

            Handler(Looper.getMainLooper()).post {
                onComplete()
            }
        }
    }
}
