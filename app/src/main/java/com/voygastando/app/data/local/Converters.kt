package com.voygastando.app.data.local

import androidx.room.TypeConverter
import com.voygastando.app.data.local.entity.SessionStatus

class Converters {
    @TypeConverter
    fun fromStatus(status: SessionStatus): String = status.name

    @TypeConverter
    fun toStatus(value: String): SessionStatus = SessionStatus.valueOf(value)
}
