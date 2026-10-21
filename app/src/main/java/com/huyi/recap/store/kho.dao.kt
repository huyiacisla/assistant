package com.huyi.recap.store

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface KhoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(i: Item): Long
    @Update suspend fun update(i: Item)
    @Delete suspend fun delete(i: Item)
    @Query("SELECT * FROM Item WHERE id = :id") suspend fun byId(id: Long): Item?
    @Query("SELECT * FROM Item WHERE deletedAt IS NULL AND (remindAt > :now OR (remindCount = 0 AND remindAt > :now - 86400000)) AND status IN ('open','snoozed')") suspend fun upcoming(now: Long): List<Item>
    @Query("UPDATE Item SET remindCount = remindCount + 1 WHERE id = :id") suspend fun bumpRemind(id: Long)

    // Ghi theo cột: không ghi đè cả Item bằng bản chụp cũ (lost update khi AI/popup/sửa tay chạm cùng lúc).
    @Query("UPDATE Item SET status = :status WHERE id = :id") suspend fun setStatus(id: Long, status: String)
    @Query("UPDATE Item SET status = :status, remindAt = :at WHERE id = :id") suspend fun snooze(id: Long, status: String, at: Long)
    @Query("UPDATE Item SET kind = :kind, aiStatus = :aiStatus WHERE id = :id") suspend fun setAi(id: Long, kind: String, aiStatus: String)
    @Query("UPDATE Item SET folderId = :folderId WHERE id IN (:ids)") suspend fun setFolder(ids: List<Long>, folderId: Long)
    @Query("UPDATE Item SET text = :text, tags = :tags, imagePath = :img, title = :title, remindAt = :remindAt, folderId = :folderId, status = CASE WHEN :reopen THEN 'open' ELSE status END WHERE id = :id")
    suspend fun saveDetail(id: Long, text: String, tags: String, img: String?, title: String, remindAt: Long?, folderId: Long?, reopen: Boolean)

    @Query("UPDATE Item SET deletedAt = :ts WHERE id = :id") suspend fun softDelete(id: Long, ts: Long)
    @Query("UPDATE Item SET deletedAt = NULL WHERE id = :id") suspend fun restore(id: Long)
    @Query("SELECT * FROM Item WHERE deletedAt IS NOT NULL ORDER BY deletedAt DESC") fun trash(): Flow<List<Item>>
    @Query("SELECT * FROM Item WHERE deletedAt IS NOT NULL AND deletedAt < :before") suspend fun expired(before: Long): List<Item>

    @Query("SELECT * FROM Item WHERE deletedAt IS NULL ORDER BY createdAt DESC")
    fun all(): Flow<List<Item>>

    @Query("SELECT * FROM Item WHERE deletedAt IS NULL AND folderId = :folderId ORDER BY createdAt DESC")
    fun inFolder(folderId: Long): Flow<List<Item>>

    @Query("SELECT * FROM Item") suspend fun allRows(): List<Item>
    @Query("SELECT uid FROM Item") suspend fun uids(): List<String>
    @Query("SELECT createdAt || ':' || text FROM Item") suspend fun keys(): List<String>

    @Query("SELECT id FROM Folder WHERE name = :name LIMIT 1") suspend fun folderIdByName(name: String): Long?
    @Query("UPDATE Item SET folderId = :folderId WHERE source = 'clipboard' AND folderId IS NULL")
    suspend fun adoptClipboard(folderId: Long)

    @Query("SELECT * FROM Item WHERE deletedAt IS NULL AND folderId IS NULL ORDER BY createdAt DESC")
    fun unsorted(): Flow<List<Item>>

    @Query("SELECT Item.* FROM Item JOIN ItemFts ON Item.rowid = ItemFts.rowid WHERE Item.deletedAt IS NULL AND ItemFts MATCH :q ORDER BY Item.createdAt DESC")
    fun search(q: String): Flow<List<Item>>

    @Query("SELECT * FROM Folder ORDER BY name")
    fun folders(): Flow<List<Folder>>
    @Insert suspend fun insertFolder(f: Folder): Long
    @Update suspend fun updateFolder(f: Folder)
    @Delete suspend fun deleteFolder(f: Folder)
    @Query("UPDATE Item SET folderId = NULL WHERE folderId = :folderId")
    suspend fun detachFolder(folderId: Long)
}
/** Folder thật "Clipboard": tạo nếu chưa có, gom luôn các mục clipboard cũ chưa có folder. */
suspend fun KhoDao.clipFolder(): Long {
    val id = folderIdByName("Clipboard") ?: insertFolder(Folder(name = "Clipboard"))
    adoptClipboard(id)
    return id
}
