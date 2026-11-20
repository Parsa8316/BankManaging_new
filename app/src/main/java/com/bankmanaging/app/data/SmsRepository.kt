package com.bankmanaging.app.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Telephony
import androidx.core.content.ContextCompat
import com.bankmanaging.app.model.BankTransaction
import com.bankmanaging.app.model.SmsPattern
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class RawSmsMessage(
    val id: Long,
    val address: String,
    val body: String,
    val timestampMillis: Long
)

class SmsRepository(private val context: Context) {

    fun hasSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    suspend fun readBankTransactions(
        senders: List<String>,
        patterns: List<SmsPattern>
    ): List<BankTransaction> = withContext(Dispatchers.IO) {
        if (!hasSmsPermission()) return@withContext emptyList()

        // Collect all active sender filters: explicit user senders + senders defined on enabled custom patterns
        val patternSenders = patterns
            .filter { it.isEnabled && !it.isBuiltIn }
            .flatMap { it.senderKeywords }
        val effectiveSenders = (senders + patternSenders)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()

        if (effectiveSenders.isEmpty()) return@withContext emptyList()

        val rawMessages = mutableListOf<RawSmsMessage>()
        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE
        )

        runCatching {
            context.contentResolver.query(
                Telephony.Sms.Inbox.CONTENT_URI,
                projection,
                null,
                null,
                "${Telephony.Sms.DATE} DESC"
            )?.use { cursor ->
                val idIdx = cursor.getColumnIndex(Telephony.Sms._ID)
                val addressIdx = cursor.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIdx = cursor.getColumnIndex(Telephony.Sms.BODY)
                val dateIdx = cursor.getColumnIndex(Telephony.Sms.DATE)

                while (cursor.moveToNext()) {
                    val address = if (addressIdx >= 0) cursor.getString(addressIdx).orEmpty() else ""
                    if (address.isBlank()) continue

                    if (effectiveSenders.any { sender -> SmsParser.senderMatches(address, sender) }) {
                        val id = if (idIdx >= 0) cursor.getLong(idIdx) else rawMessages.size.toLong()
                        val body = if (bodyIdx >= 0) cursor.getString(bodyIdx).orEmpty() else ""
                        val timestamp = if (dateIdx >= 0) cursor.getLong(dateIdx) else System.currentTimeMillis()
                        if (body.isNotBlank()) {
                            rawMessages.add(RawSmsMessage(id, address, body, timestamp))
                        }
                    }
                }
            }
        }

        rawMessages.map { raw ->
            SmsParser.parseMessage(
                id = raw.id,
                rawMessage = raw.body,
                senderAddress = raw.address,
                timestampMillis = raw.timestampMillis,
                patterns = patterns
            )
        }
    }
}
