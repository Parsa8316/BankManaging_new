package com.bankmanaging.app.model

import kotlinx.serialization.Serializable

@Serializable
enum class ExtractionMode {
    AUTO_KEYWORD,
    LINE_INDEX,
    REGEX
}

@Serializable
enum class CurrencyUnit {
    RIAL,
    TOMAN
}

@Serializable
data class SmsPattern(
    val id: String,
    val name: String,
    val bankDisplayName: String = "",
    val senderKeywords: List<String> = emptyList(),
    val bodyIdentifierKeywords: List<String> = emptyList(),
    val isBuiltIn: Boolean = false,
    val isEnabled: Boolean = true,
    val currencyUnit: CurrencyUnit = CurrencyUnit.RIAL,
    val incomeKeywords: List<String> = emptyList(),
    val outcomeKeywords: List<String> = emptyList(),
    val ignoreKeywords: List<String> = emptyList(),
    val amountMode: ExtractionMode = ExtractionMode.AUTO_KEYWORD,
    val amountLineIndex: Int = 0,
    val amountKeywords: List<String> = emptyList(),
    val amountRegex: String = "",
    val balanceMode: ExtractionMode = ExtractionMode.AUTO_KEYWORD,
    val balanceLineIndex: Int = 0,
    val balanceKeywords: List<String> = emptyList(),
    val balanceRegex: String = "",
    val typeLineIndex: Int = 0,
    val bankNameLineIndex: Int = 0,
    val dateLineIndex: Int = 0,
    val timeLineIndex: Int = 0,
    val sampleSms: String = ""
)

object DefaultSmsPatterns {
    val bluBank = SmsPattern(
        id = "builtin_blubank",
        name = "بلوبانک (BluBank)",
        bankDisplayName = "بلوبانک",
        senderKeywords = listOf("BluBank", "Blu", "982000400", "2000400"),
        bodyIdentifierKeywords = listOf("بلو", "بلوبانک", "Blu", "آن باکس", "این باکس", "بفرمایید رمز پویا"),
        isBuiltIn = true,
        isEnabled = true,
        currencyUnit = CurrencyUnit.RIAL,
        incomeKeywords = listOf("آن باکس", "واریز پول", "سود شما", "بازگشت وجه", "واریز"),
        outcomeKeywords = listOf("برداشت پول", "این باکس", "پرداخت قبض", "شارژ شدی", "آنلاین شدی", "خرید", "انتقال پول"),
        ignoreKeywords = listOf("بفرمایید رمز پویا", "رمز پویا", "رمز دوم", "کد تایید"),
        amountMode = ExtractionMode.LINE_INDEX,
        amountLineIndex = 3,
        amountKeywords = listOf("مبلغ", "ریال", "تومان"),
        amountRegex = "",
        balanceMode = ExtractionMode.AUTO_KEYWORD,
        balanceLineIndex = 4,
        balanceKeywords = listOf("موجودی", "موجودی حساب", "مانده"),
        balanceRegex = "",
        typeLineIndex = 2,
        bankNameLineIndex = 1,
        dateLineIndex = 0,
        timeLineIndex = 0,
        sampleSms = """
            بلوبانک
            واریز پول
            مبلغ 15,000,000 ریال به حسابتان واریز شد
            موجودی حساب: 42,500,000 ریال
            14:25
            1404.08.15
        """.trimIndent()
    )

    val mellatBank = SmsPattern(
        id = "builtin_mellat",
        name = "بانک ملت",
        bankDisplayName = "بانک ملت",
        senderKeywords = listOf("BankMellat", "Mellat", "2000333", "30003303"),
        bodyIdentifierKeywords = listOf("بانک ملت", "ملت", "حساب"),
        isBuiltIn = true,
        isEnabled = true,
        currencyUnit = CurrencyUnit.RIAL,
        incomeKeywords = listOf("واریز", "بستانکار", "انتقال به", "+"),
        outcomeKeywords = listOf("برداشت", "بدهکار", "خرید", "انتقال از", "پرداخت", "-"),
        ignoreKeywords = listOf("رمز پویا", "رمز یکبار مصرف", "کد تایید"),
        amountMode = ExtractionMode.AUTO_KEYWORD,
        amountLineIndex = 0,
        amountKeywords = listOf("واریز", "برداشت", "مبلغ", "خرید", "انتقال"),
        amountRegex = "",
        balanceMode = ExtractionMode.AUTO_KEYWORD,
        balanceLineIndex = 0,
        balanceKeywords = listOf("مانده", "موجودی"),
        balanceRegex = "",
        typeLineIndex = 0,
        bankNameLineIndex = 0,
        dateLineIndex = 0,
        timeLineIndex = 0,
        sampleSms = """
            بانک ملت
            حساب 12345678
            برداشت: 2,450,000 ریال
            مانده: 18,320,000 ریال
            1404/08/15 11:40
        """.trimIndent()
    )

    val melliBank = SmsPattern(
        id = "builtin_melli",
        name = "بانک ملی ایران",
        bankDisplayName = "بانک ملی",
        senderKeywords = listOf("BMI", "BankMelli", "980017", "700717"),
        bodyIdentifierKeywords = listOf("بانک ملی", "بام", "ملی"),
        isBuiltIn = true,
        isEnabled = true,
        currencyUnit = CurrencyUnit.RIAL,
        incomeKeywords = listOf("واریز", "انتقال به", "سود"),
        outcomeKeywords = listOf("برداشت", "انتقال از", "خرید", "پرداخت"),
        ignoreKeywords = listOf("رمز پویا", "رمز دوم", "کد ورود"),
        amountMode = ExtractionMode.AUTO_KEYWORD,
        amountLineIndex = 0,
        amountKeywords = listOf("انتقال", "واریز", "برداشت", "مبلغ"),
        amountRegex = "",
        balanceMode = ExtractionMode.AUTO_KEYWORD,
        balanceLineIndex = 0,
        balanceKeywords = listOf("مانده", "موجودی"),
        balanceRegex = "",
        typeLineIndex = 0,
        bankNameLineIndex = 0,
        dateLineIndex = 0,
        timeLineIndex = 0,
        sampleSms = """
            بانک ملی ایران
            انتقال وجه
            مبلغ: 8,000,000- ریال
            مانده: 54,100,000 ریال
            04/08/15-16:05
        """.trimIndent()
    )

    val samanBank = SmsPattern(
        id = "builtin_saman",
        name = "بانک سامان / پاسارگاد",
        bankDisplayName = "بانک سامان",
        senderKeywords = listOf("Saman", "Pasargad", "BPI", "2000800"),
        bodyIdentifierKeywords = listOf("سامان", "پاسارگاد"),
        isBuiltIn = true,
        isEnabled = true,
        currencyUnit = CurrencyUnit.RIAL,
        incomeKeywords = listOf("واریز", "افزایش", "سود", "بستانکار"),
        outcomeKeywords = listOf("برداشت", "کاهش", "خرید", "پرداخت", "کارمزد"),
        ignoreKeywords = listOf("رمز پویا", "رمز دوم", "کد تایید"),
        amountMode = ExtractionMode.AUTO_KEYWORD,
        amountLineIndex = 0,
        amountKeywords = listOf("مبلغ", "واریز", "برداشت"),
        amountRegex = "",
        balanceMode = ExtractionMode.AUTO_KEYWORD,
        balanceLineIndex = 0,
        balanceKeywords = listOf("مانده", "موجودی"),
        balanceRegex = "",
        typeLineIndex = 0,
        bankNameLineIndex = 1,
        dateLineIndex = 0,
        timeLineIndex = 0,
        sampleSms = """
            بانک سامان
            واریز به حساب
            مبلغ 35,000,000 ریال
            مانده 61,200,000 ریال
            1404/08/14 09:15
        """.trimIndent()
    )

    val generalIranBank = SmsPattern(
        id = "builtin_general",
        name = "الگوی هوشمند عمومی بانک‌ها",
        bankDisplayName = "",
        senderKeywords = emptyList(),
        bodyIdentifierKeywords = emptyList(),
        isBuiltIn = true,
        isEnabled = true,
        currencyUnit = CurrencyUnit.RIAL,
        incomeKeywords = listOf("واریز", "بستانکار", "سود", "بازگشت وجه", "آن باکس", "افزایش"),
        outcomeKeywords = listOf("برداشت", "بدهکار", "خرید", "پرداخت", "کسر", "انتقال از", "این باکس", "شارژ", "قبض", "کارمزد"),
        ignoreKeywords = listOf("رمز پویا", "رمز دوم", "رمز یکبار مصرف", "کد تایید", "کد ورود"),
        amountMode = ExtractionMode.AUTO_KEYWORD,
        amountLineIndex = 0,
        amountKeywords = listOf("مبلغ", "واریز", "برداشت", "خرید", "پرداخت", "ریال", "تومان"),
        amountRegex = "",
        balanceMode = ExtractionMode.AUTO_KEYWORD,
        balanceLineIndex = 0,
        balanceKeywords = listOf("مانده", "موجودی", "موجودی حساب"),
        balanceRegex = "",
        typeLineIndex = 0,
        bankNameLineIndex = 1,
        dateLineIndex = 0,
        timeLineIndex = 0,
        sampleSms = """
            بانک تجارت
            برداشت از حساب
            مبلغ: 5,200,000 ریال
            مانده: 12,800,000 ریال
            1404/08/16 18:20
        """.trimIndent()
    )

    val allDefaults: List<SmsPattern> = listOf(
        bluBank,
        mellatBank,
        melliBank,
        samanBank,
        generalIranBank
    )
}
