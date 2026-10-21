package com.huyi.recap.store

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Item::class, Folder::class, ItemFts::class], version = 9, exportSchema = true)   // schema ghi vào app/schemas (ksp arg room.schemaLocation) để đối chiếu khi đổi version
abstract class Db : RoomDatabase() {
    abstract fun dao(): KhoDao

    companion object {
        @Volatile private var inst: Db? = null
        fun get(c: Context): Db = inst ?: synchronized(this) {
            inst ?: Room.databaseBuilder(c.applicationContext, Db::class.java, "kho.db")
                .addMigrations(
                    object : androidx.room.migration.Migration(1, 2) {
                        override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) { db.execSQL("ALTER TABLE Item ADD COLUMN remindAt INTEGER") }
                    },
                    object : androidx.room.migration.Migration(2, 3) {
                        override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                            db.execSQL("ALTER TABLE Item ADD COLUMN status TEXT NOT NULL DEFAULT 'open'")
                            db.execSQL("ALTER TABLE Item ADD COLUMN remindCount INTEGER NOT NULL DEFAULT 0")
                        }
                    },
                    object : androidx.room.migration.Migration(3, 4) {
                        override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) { db.execSQL("ALTER TABLE Item ADD COLUMN kind TEXT NOT NULL DEFAULT ''") }
                    },
                    object : androidx.room.migration.Migration(4, 5) {
                        override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) { db.execSQL("ALTER TABLE Item ADD COLUMN deletedAt INTEGER") }
                    },
                    object : androidx.room.migration.Migration(5, 6) {
                        override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                            db.execSQL("ALTER TABLE Item ADD COLUMN uid TEXT NOT NULL DEFAULT ''")
                            db.execSQL("ALTER TABLE Item ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                            db.execSQL("UPDATE Item SET uid = lower(hex(randomblob(16))), updatedAt = createdAt")
                            touchTrigger(db)
                        }
                    },
                    object : androidx.room.migration.Migration(6, 7) {
                        override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) { db.execSQL("DROP TRIGGER IF EXISTS item_touch"); touchTrigger(db) }
                    },
                    object : androidx.room.migration.Migration(7, 8) {
                        override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) { db.execSQL("ALTER TABLE Item ADD COLUMN audioPath TEXT") }
                    },
                    object : androidx.room.migration.Migration(8, 9) {
                        override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                            db.execSQL("ALTER TABLE Item ADD COLUMN title TEXT NOT NULL DEFAULT ''")
                            db.execSQL("ALTER TABLE Item ADD COLUMN summary TEXT NOT NULL DEFAULT ''")
                        }
                    },
                ).addCallback(object : Callback() { override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) = touchTrigger(db) })
                .build().also { inst = it }
        }
    }
}

private fun touchTrigger(db: androidx.sqlite.db.SupportSQLiteDatabase) = db.execSQL(
    "CREATE TRIGGER IF NOT EXISTS item_touch AFTER UPDATE ON Item WHEN NEW.updatedAt = OLD.updatedAt " +
        "BEGIN UPDATE Item SET updatedAt = MAX(OLD.updatedAt + 1, CAST(strftime('%s','now') AS INTEGER) * 1000) WHERE id = NEW.id; END")  // luôn khác OLD ⇒ không tự kích lại (trước đây cùng giây ⇒ đệ quy vô hạn, crash khi xoá thẻ)
