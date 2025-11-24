package com.pranayama.studio.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * DataStore instance for persisting app statistics.
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pranayama_stats")

/**
 * Simple data class holding session statistics.
 */
data class SessionStats(
    val totalSessions: Int = 0,
    val totalMinutes: Int = 0,
    val todayMinutes: Int = 0,
    val lastSessionDate: String = ""
)

/**
 * Repository for managing breathing session statistics.
 * Uses DataStore Preferences for lightweight, async persistence.
 * 
 * Stats tracked:
 * - Total sessions completed (all time)
 * - Total minutes practiced (all time)
 * - Today's minutes (resets daily)
 * - Last session date (for daily reset logic)
 */
class StatsRepository(private val context: Context) {
    
    companion object {
        private val TOTAL_SESSIONS = intPreferencesKey("total_sessions")
        private val TOTAL_MINUTES = intPreferencesKey("total_minutes")
        private val TODAY_MINUTES = intPreferencesKey("today_minutes")
        private val LAST_SESSION_DATE = stringPreferencesKey("last_session_date")
    }
    
    /**
     * Flow of current session statistics.
     * Automatically handles daily reset of todayMinutes.
     */
    val statsFlow: Flow<SessionStats> = context.dataStore.data.map { preferences ->
        val lastDate = preferences[LAST_SESSION_DATE] ?: ""
        val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        
        // Reset today's minutes if it's a new day
        val todayMinutes = if (lastDate == today) {
            preferences[TODAY_MINUTES] ?: 0
        } else {
            0
        }
        
        SessionStats(
            totalSessions = preferences[TOTAL_SESSIONS] ?: 0,
            totalMinutes = preferences[TOTAL_MINUTES] ?: 0,
            todayMinutes = todayMinutes,
            lastSessionDate = lastDate
        )
    }
    
    /**
     * Record a completed breathing session.
     * 
     * @param durationMinutes Duration of the session in minutes
     */
    suspend fun recordSession(durationMinutes: Int) {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        
        context.dataStore.edit { preferences ->
            // Increment total sessions
            val currentSessions = preferences[TOTAL_SESSIONS] ?: 0
            preferences[TOTAL_SESSIONS] = currentSessions + 1
            
            // Add to total minutes
            val currentTotal = preferences[TOTAL_MINUTES] ?: 0
            preferences[TOTAL_MINUTES] = currentTotal + durationMinutes
            
            // Handle today's minutes with daily reset
            val lastDate = preferences[LAST_SESSION_DATE] ?: ""
            val currentToday = if (lastDate == today) {
                preferences[TODAY_MINUTES] ?: 0
            } else {
                0
            }
            preferences[TODAY_MINUTES] = currentToday + durationMinutes
            preferences[LAST_SESSION_DATE] = today
        }
    }
    
    /**
     * Clear all statistics (for testing or user request).
     */
    suspend fun clearStats() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
