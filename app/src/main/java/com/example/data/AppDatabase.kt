package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [TransactionEntity::class, CategoryBudgetEntity::class, MlRuleEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expense_tracker_db"
                )
                .addCallback(DatabaseCallback(context))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(database.transactionDao())
                    }
                }
            }

            suspend fun populateDatabase(dao: TransactionDao) {
                // Default Category Budgets
                val defaultBudgets = listOf(
                    CategoryBudgetEntity("Food & Dining", 15000.0, "restaurant", "#FF6B6B"),
                    CategoryBudgetEntity("Shopping", 10000.0, "shopping_bag", "#4ECDC4"),
                    CategoryBudgetEntity("Bills & Utilities", 8000.0, "bolt", "#FFE66D"),
                    CategoryBudgetEntity("Transport", 5000.0, "directions_car", "#1A535C"),
                    CategoryBudgetEntity("Entertainment", 4000.0, "movie", "#9B5DE5"),
                    CategoryBudgetEntity("Health", 3000.0, "medical_services", "#F15BB5"),
                    CategoryBudgetEntity("General", 5000.0, "category", "#00F5D4")
                )
                dao.insertCategoryBudgets(defaultBudgets)

                // Default ML Rules
                val defaultRules = listOf(
                    MlRuleEntity("swiggy", "Food & Dining", 2.0f),
                    MlRuleEntity("zomato", "Food & Dining", 2.0f),
                    MlRuleEntity("starbucks", "Food & Dining", 2.0f),
                    MlRuleEntity("mcdonalds", "Food & Dining", 2.0f),
                    MlRuleEntity("cafe", "Food & Dining", 1.8f),
                    MlRuleEntity("bakery", "Food & Dining", 1.8f),
                    MlRuleEntity("amazon", "Shopping", 2.0f),
                    MlRuleEntity("flipkart", "Shopping", 2.0f),
                    MlRuleEntity("myntra", "Shopping", 2.0f),
                    MlRuleEntity("zara", "Shopping", 2.0f),
                    MlRuleEntity("airtel", "Bills & Utilities", 2.0f),
                    MlRuleEntity("jio", "Bills & Utilities", 2.0f),
                    MlRuleEntity("electric", "Bills & Utilities", 2.0f),
                    MlRuleEntity("bescom", "Bills & Utilities", 2.0f),
                    MlRuleEntity("tneb", "Bills & Utilities", 2.0f),
                    MlRuleEntity("uber", "Transport", 2.0f),
                    MlRuleEntity("ola", "Transport", 2.0f),
                    MlRuleEntity("rapido", "Transport", 2.0f),
                    MlRuleEntity("fuel", "Transport", 2.0f),
                    MlRuleEntity("netflix", "Entertainment", 2.0f),
                    MlRuleEntity("hotstar", "Entertainment", 2.0f),
                    MlRuleEntity("spotify", "Entertainment", 2.0f),
                    MlRuleEntity("pvr", "Entertainment", 2.0f),
                    MlRuleEntity("apollo", "Health", 2.0f),
                    MlRuleEntity("pharmacy", "Health", 2.0f),
                    MlRuleEntity("1mg", "Health", 2.0f),
                    MlRuleEntity("salary", "Income", 2.5f),
                    MlRuleEntity("payroll", "Income", 2.5f),
                    MlRuleEntity("deposit", "Income", 2.0f),
                    MlRuleEntity("cashback", "Income", 2.0f),
                    MlRuleEntity("upi", "Transfer", 1.8f),
                    MlRuleEntity("gpay", "Transfer", 1.8f),
                    MlRuleEntity("phonepe", "Transfer", 1.8f),
                    MlRuleEntity("paytm", "Transfer", 1.8f)
                )
                dao.insertMlRules(defaultRules)

                // Sample Pre-populated Financial Transactions (Simulated parsed bank SMSs)
                val now = System.currentTimeMillis()
                val day = 86400000L

                val sampleTransactions = listOf(
                    TransactionEntity(
                        title = "Salary Direct Deposit",
                        amount = 85000.00,
                        dateMillis = now - day * 1,
                        type = "INCOME",
                        category = "Income",
                        merchant = "TCS Corporate Payroll",
                        bankAccount = "HDFC *8021",
                        sourceType = "SMS",
                        originalSmsText = "ALERT: Your A/C *8021 was CREDITED with INR 85,000.00 from TCS Corporate Payroll. Available Bal: Rs.1,24,500.50.",
                        mlConfidence = 0.98f
                    ),
                    TransactionEntity(
                        title = "Swiggy Food Order",
                        amount = 485.00,
                        dateMillis = now - day * 1 - 3600000 * 2,
                        type = "EXPENSE",
                        category = "Food & Dining",
                        merchant = "Swiggy",
                        bankAccount = "HDFC *8021",
                        sourceType = "SMS",
                        originalSmsText = "Paid Rs.485.00 to SWIGGY FOOD ORDER using card *8021 on 02-Aug. Txn: TXN994021.",
                        mlConfidence = 0.96f
                    ),
                    TransactionEntity(
                        title = "Amazon Electronics Purchase",
                        amount = 2499.00,
                        dateMillis = now - day * 2,
                        type = "EXPENSE",
                        category = "Shopping",
                        merchant = "Amazon.in",
                        bankAccount = "Google Pay UPI",
                        sourceType = "SMS",
                        originalSmsText = "Debited Rs.2,499.00 at AMAZON INDIA via Google Pay UPI ending 4092. Ref: AMZ88219.",
                        mlConfidence = 0.95f
                    ),
                    TransactionEntity(
                        title = "Electricity & Fiber Bill",
                        amount = 1850.00,
                        dateMillis = now - day * 3,
                        type = "EXPENSE",
                        category = "Bills & Utilities",
                        merchant = "Airtel Broadband & Power",
                        bankAccount = "SBI Bank *1102",
                        sourceType = "SMS",
                        originalSmsText = "Auto-debit of Rs.1,850.00 for AIRTEL BROADBAND processed successfully from A/C *1102.",
                        mlConfidence = 0.97f
                    ),
                    TransactionEntity(
                        title = "Uber Ride to Airport",
                        amount = 320.00,
                        dateMillis = now - day * 4,
                        type = "EXPENSE",
                        category = "Transport",
                        merchant = "Uber India",
                        bankAccount = "HDFC *8021",
                        sourceType = "SMS",
                        originalSmsText = "Debited INR 320.00 for UBER RIDE TRIP from card ending 8021.",
                        mlConfidence = 0.94f
                    ),
                    TransactionEntity(
                        title = "Zomato Restaurant Dinner",
                        amount = 680.00,
                        dateMillis = now - day * 5,
                        type = "EXPENSE",
                        category = "Food & Dining",
                        merchant = "Zomato",
                        bankAccount = "PhonePe UPI",
                        sourceType = "SMS",
                        originalSmsText = "Paid Rs.680.00 at ZOMATO with PhonePe UPI ending 4092.",
                        mlConfidence = 0.92f
                    ),
                    TransactionEntity(
                        title = "Netflix Premium Plan",
                        amount = 649.00,
                        dateMillis = now - day * 6,
                        type = "EXPENSE",
                        category = "Entertainment",
                        merchant = "Netflix India",
                        bankAccount = "SBI Bank *1102",
                        sourceType = "SMS",
                        originalSmsText = "Subscription charge Rs.649.00 for NETFLIX INDIA charged to debit card *1102.",
                        mlConfidence = 0.99f
                    ),
                    TransactionEntity(
                        title = "Apollo Pharmacy Medicines",
                        amount = 450.00,
                        dateMillis = now - day * 7,
                        type = "EXPENSE",
                        category = "Health",
                        merchant = "Apollo Pharmacy",
                        bankAccount = "HDFC *8021",
                        sourceType = "SMS",
                        originalSmsText = "Debited Rs.450.00 at APOLLO PHARMACY store #3091.",
                        mlConfidence = 0.95f
                    )
                )
                dao.insertTransactions(sampleTransactions)
            }
        }
    }
}
