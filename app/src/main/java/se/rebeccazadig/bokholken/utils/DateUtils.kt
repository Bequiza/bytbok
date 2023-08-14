package se.rebeccazadig.bokholken.utils

import android.content.Context
import se.rebeccazadig.bokholken.R
import java.text.SimpleDateFormat
import java.util.*

fun formatDateForDisplay(context: Context, timestamp: Long): String {
    var now = Calendar.getInstance()
    val creationTime = Calendar.getInstance().apply { timeInMillis = timestamp }
    val sdfTime = SimpleDateFormat("HH:mm", Locale("sv", "SE"))
    // Same day
    if (isSameDay(now, creationTime)) {
        return "${context.getString(R.string.today)} ${sdfTime.format(creationTime.time)}"
    }

    // Yesterday
    now.add(Calendar.DAY_OF_YEAR, -1)
    if (isSameDay(now, creationTime)) {
        return "${context.getString(R.string.yesterday)} ${sdfTime.format(creationTime.time)}"
    }

    // Within the current week
    val days = arrayOf(
        context.getString(R.string.sunday),
        context.getString(R.string.monday),
        context.getString(R.string.tuesday),
        context.getString(R.string.wednesday),
        context.getString(R.string.thursday),
        context.getString(R.string.friday),
        context.getString(R.string.saturday)
    )
    now = Calendar.getInstance()  // Reset to the current day
    for (i in 2..6) {
        now.add(Calendar.DAY_OF_YEAR, -1)
        if (isSameDay(now, creationTime)) {
            return "${days[now.get(Calendar.DAY_OF_WEEK) - 1]} ${sdfTime.format(creationTime.time)}"
        }
    }

    // Older than a week
    val sdfDate = SimpleDateFormat("d MMM HH:mm", Locale("sv", "SE"))
    return sdfDate.format(creationTime.time)
}

private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
