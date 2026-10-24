package com.clientsaathi.hdfclife

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.*
import java.text.DateFormat
import java.util.Calendar
import java.util.Date

class MainActivity : Activity() {
    private lateinit var db: ClientDb
    private lateinit var root: LinearLayout
    private var searchText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = ClientDb(this)
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 41)
        }
        showHome()
    }

    private fun setup(title: String) {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            setBackgroundColor(0xFFF7F8FC.toInt())
        }
        root.addView(TextView(this).apply {
            text = title; textSize = 25f; setTextColor(0xFF163A70.toInt())
            gravity = Gravity.CENTER_VERTICAL; setPadding(0, 0, 0, 18)
        })
        val scroll = ScrollView(this)
        scroll.addView(root)
        setContentView(scroll)
    }

    private fun button(text: String, action: () -> Unit): Button = Button(this).apply {
        this.text = text; isAllCaps = false
        setOnClickListener { action() }
    }

    private fun addField(hintText: String, value: String = ""): EditText {
        val e = EditText(this).apply {
            hint = hintText; setText(value); textSize = 16f
            setSingleLine(hintText != "Notes")
            if (hintText == "Notes") minLines = 2
        }
        root.addView(e, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 8 })
        return e
    }

    private fun showHome() {
        setup("Client Saathi • HDFC Life")
        root.addView(TextView(this).apply {
            text = "Save client details on this phone and receive follow-up reminders."
            textSize = 15f; setTextColor(0xFF444444.toInt())
        })
        root.addView(button("＋ Add Client / Follow-up") { showEditor(null) })
        root.addView(button("Reminder permissions") { openReminderSettings() })
        val search = addField("Search name, phone or policy")
        search.setText(searchText)
        root.addView(button("Search") { searchText = search.text.toString().trim(); showHome() })
        val now = System.currentTimeMillis()
        val clients = db.all().filter {
            val q = searchText.lowercase()
            q.isBlank() || it.name.lowercase().contains(q) || it.phone.contains(q) || it.policy.lowercase().contains(q)
        }
        val due = clients.count { !it.done && it.dueAt <= now }
        root.addView(TextView(this).apply {
            text = "Clients: ${clients.size}   •   Due / overdue: $due"
            textSize = 16f; setPadding(0, 12, 0, 12)
        })
        if (clients.isEmpty()) root.addView(TextView(this).apply {
            text = "No clients yet. Tap Add Client to begin."; textSize = 16f
        })
        clients.forEach { c ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL; setPadding(14, 10, 14, 10)
                setBackgroundColor(if (c.done) 0xFFE4F5E8.toInt() else 0xFFFFFFFF.toInt())
            }
            card.addView(TextView(this).apply {
                text = c.name + if (c.done) "  ✓ Completed" else if (c.dueAt <= now) "  • OVERDUE" else ""
                textSize = 18f; setTextColor(0xFF222222.toInt())
            })
            card.addView(TextView(this).apply {
                text = "${c.phone}\nPolicy: ${c.policy.ifBlank { "—" }}\n${c.reason}\nFollow-up: ${DateFormat.getDateTimeInstance().format(Date(c.dueAt))}"
                textSize = 14f; setTextColor(0xFF555555.toInt())
            })
            val actions = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            actions.addView(button("Edit") { showEditor(c) })
            if (c.phone.isNotBlank()) actions.addView(button("Call") {
                startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(c.phone)}")))
            })
            actions.addView(button(if (c.done) "Reopen" else "Done") {
                val updated = c.copy(done = !c.done)
                db.save(updated)
                if (updated.done) ReminderScheduler.cancel(this, c.id) else ReminderScheduler.schedule(this, updated)
                showHome()
            })
            actions.addView(button("Delete") {
                ReminderScheduler.cancel(this, c.id); db.delete(c.id); showHome()
            })
            card.addView(actions)
            root.addView(card, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 12 })
        }
    }

    private fun showEditor(existing: Client?) {
        var dueAt = existing?.dueAt ?: (System.currentTimeMillis() + 60 * 60 * 1000L)
        setup(if (existing == null) "Add Client" else "Edit Client")
        val name = addField("Client name", existing?.name ?: "")
        val phone = addField("Mobile number", existing?.phone ?: "")
        val policy = addField("Policy / reference number", existing?.policy ?: "")
        val reason = addField("Follow-up reason", existing?.reason ?: "")
        val note = addField("Notes", existing?.note ?: "")
        val dueLabel = TextView(this).apply { textSize = 16f; setPadding(0, 10, 0, 10) }
        fun updateDue() { dueLabel.text = "Reminder: " + DateFormat.getDateTimeInstance().format(Date(dueAt)) }
        updateDue()
        root.addView(button("Choose date") {
            val cal = Calendar.getInstance().apply { timeInMillis = dueAt }
            DatePickerDialog(this, { _, y, m, d ->
                cal.set(Calendar.YEAR, y); cal.set(Calendar.MONTH, m); cal.set(Calendar.DAY_OF_MONTH, d)
                dueAt = cal.timeInMillis; updateDue()
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        })
        root.addView(button("Choose time") {
            val cal = Calendar.getInstance().apply { timeInMillis = dueAt }
            TimePickerDialog(this, { _, h, m ->
                cal.set(Calendar.HOUR_OF_DAY, h); cal.set(Calendar.MINUTE, m); cal.set(Calendar.SECOND, 0)
                dueAt = cal.timeInMillis; updateDue()
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        })
        root.addView(dueLabel)
        root.addView(button("Save client & reminder") {
            if (name.text.toString().trim().isBlank()) {
                Toast.makeText(this, "Please enter the client name", Toast.LENGTH_SHORT).show()
                return@button
            }
            val saved = Client(existing?.id ?: 0L, name.text.toString().trim(), phone.text.toString().trim(),
                policy.text.toString().trim(), reason.text.toString().trim(), note.text.toString().trim(),
                dueAt, existing?.done ?: false)
            val id = db.save(saved)
            db.get(id)?.let { ReminderScheduler.schedule(this, it) }
            Toast.makeText(this, "Client saved", Toast.LENGTH_SHORT).show()
            showHome()
        })
        root.addView(button("Cancel") { showHome() })
    }

    private fun openReminderSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName")))
            } catch (_: Exception) {
                startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")))
            }
        } else startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")))
    }
}
