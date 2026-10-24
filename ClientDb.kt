package com.clientsaathi.hdfclife

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class Client(
    val id: Long, val name: String, val phone: String, val policy: String,
    val reason: String, val note: String, val dueAt: Long, val done: Boolean
)

class ClientDb(context: Context) : SQLiteOpenHelper(context, "clientsaathi.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""CREATE TABLE clients (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL, phone TEXT, policy TEXT, reason TEXT, note TEXT,
            dueAt INTEGER NOT NULL, done INTEGER NOT NULL DEFAULT 0
        )""")
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    fun all(): List<Client> {
        val result = mutableListOf<Client>()
        readableDatabase.rawQuery("SELECT * FROM clients ORDER BY done ASC, dueAt ASC", null).use { c ->
            while (c.moveToNext()) result.add(Client(
                c.getLong(c.getColumnIndexOrThrow("id")),
                c.getString(c.getColumnIndexOrThrow("name")),
                c.getString(c.getColumnIndexOrThrow("phone")) ?: "",
                c.getString(c.getColumnIndexOrThrow("policy")) ?: "",
                c.getString(c.getColumnIndexOrThrow("reason")) ?: "",
                c.getString(c.getColumnIndexOrThrow("note")) ?: "",
                c.getLong(c.getColumnIndexOrThrow("dueAt")),
                c.getInt(c.getColumnIndexOrThrow("done")) == 1
            ))
        }
        return result
    }
    fun get(id: Long): Client? = all().firstOrNull { it.id == id }
    fun save(client: Client): Long {
        val values = ContentValues().apply {
            put("name", client.name); put("phone", client.phone); put("policy", client.policy)
            put("reason", client.reason); put("note", client.note); put("dueAt", client.dueAt)
            put("done", if (client.done) 1 else 0)
        }
        return if (client.id == 0L) writableDatabase.insert("clients", null, values)
        else { writableDatabase.update("clients", values, "id=?", arrayOf(client.id.toString())); client.id }
    }
    fun delete(id: Long) { writableDatabase.delete("clients", "id=?", arrayOf(id.toString())) }
}
