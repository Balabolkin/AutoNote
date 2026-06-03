package ru.diploma.autocareledger.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ru.diploma.autocareledger.data.model.CarEntity
import ru.diploma.autocareledger.data.model.ExpenseEntity
import ru.diploma.autocareledger.data.model.ReminderEntity

@Database(
    entities = [CarEntity::class, ExpenseEntity::class, ReminderEntity::class],
    version = 8,
    exportSchema = false
)
abstract class AutoCareDatabase : RoomDatabase() {
    abstract fun garageDao(): GarageDao

    companion object {
        private val Migration2To3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE cars ADD COLUMN generation TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE cars ADD COLUMN restyling TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE cars ADD COLUMN trim TEXT NOT NULL DEFAULT ''")
            }
        }

        private val Migration3To4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE cars ADD COLUMN tankVolumeLiters REAL NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE expenses ADD COLUMN fuelLiters REAL")
            }
        }

        private val Migration4To5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE cars ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val Migration5To6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE reminders ADD COLUMN category TEXT NOT NULL DEFAULT 'Service'")
                db.execSQL("ALTER TABLE reminders ADD COLUMN repeatMileageInterval INTEGER")
            }
        }

        private val Migration6To7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE expenses ADD COLUMN workCost REAL")
                db.execSQL("ALTER TABLE expenses ADD COLUMN partsCost REAL")
                db.execSQL("ALTER TABLE expenses ADD COLUMN shopName TEXT")
                db.execSQL("ALTER TABLE expenses ADD COLUMN partName TEXT")
                db.execSQL("ALTER TABLE expenses ADD COLUMN partNumber TEXT")
                db.execSQL("ALTER TABLE expenses ADD COLUMN partBrand TEXT")
                db.execSQL("ALTER TABLE expenses ADD COLUMN assembly TEXT")
            }
        }

        private val Migration7To8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE reminders ADD COLUMN repeatIntervalMonths INTEGER")
            }
        }

        fun create(context: Context): AutoCareDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                AutoCareDatabase::class.java,
                "auto-care-ledger.db"
            )
                .addMigrations(Migration2To3, Migration3To4, Migration4To5, Migration5To6, Migration6To7, Migration7To8)
                .fallbackToDestructiveMigration()
                .build()
    }
}
