package com.moefactory.bettermiuiexpress.hook

import android.content.Context
import com.highcapable.yukihookapi.hook.log.YLog
import com.moefactory.bettermiuiexpress.base.app.PA_EXPRESS_ENTRY
import com.moefactory.bettermiuiexpress.base.app.PA_EXPRESS_INFO_DETAIL
import com.moefactory.bettermiuiexpress.model.ExpressEntryWrapper
import com.moefactory.bettermiuiexpress.model.ExpressTrace
import com.moefactory.bettermiuiexpress.model.toExpressEntryWrapper
import com.moefactory.bettermiuiexpress.model.toExpressInfoDetailWrapper
import java.util.Locale
import com.highcapable.kavaref.extension.toClass
import com.moefactory.bettermiuiexpress.ktx.ContextClass

/** Host objects must be serialized with the host's Gson and class loader. */
internal class PAExpressCache(classLoader: ClassLoader) {

    val mutationLock = Any()
    private val revisions = mutableMapOf<String, Long>()
    private val intentRevisions = mutableMapOf<String, Long>()
    private val moduleWrite = ThreadLocal<Boolean>()
    val isModuleWrite: Boolean get() = moduleWrite.get() == true
    private val entryClass = PA_EXPRESS_ENTRY.toClass(classLoader, false)
    private val cacheClass = "com.miui.personalassistant.service.express.db.Cache".toClass(classLoader, false)
    private val preferencesClass = "com.miui.personalassistant.service.express.ExpressPreferences".toClass(classLoader, false)
    private val gsonClass = "com.google.gson.Gson".toClass(classLoader, false)
    private val gson = gsonClass.getDeclaredConstructor().newInstance()
    private val toJson = gsonClass.getMethod("toJson", Any::class.java)
    private val fromJson = gsonClass.getMethod("fromJson", String::class.java, Class::class.java)
    private val getKey = cacheClass.getMethod("getKey", ContextClass, entryClass)
    private val get = cacheClass.getMethod("get", ContextClass, String::class.java)
    private val getRemark = cacheClass.getMethod("getRemark", ContextClass, String::class.java)
    private val getLatest = cacheClass.getMethod("getLatestExpressList", ContextClass, Int::class.javaPrimitiveType)
    private val getDeleted = preferencesClass.getMethod("getDeletedExpress", ContextClass)
    private val setDeleted = preferencesClass.getMethod("setDeletedExpress", ContextClass, Map::class.java)
    private val getDeletedInfo = preferencesClass.getMethod("getDeletedExpressInfo", ContextClass)
    private val removeDeletedInfo = preferencesClass.getMethod("removeDeletedExpressInfo", ContextClass, String::class.java)
    private val getModified = preferencesClass.getMethod("getModifiedExpress", ContextClass)
    private val notifyDataChanged = cacheClass.getMethod("notifyDataChanged", ContextClass)
    private val notifyWidget = "com.miui.personalassistant.service.express.widget.ExpressWidgetProvider".toClass(classLoader, false)
        .getMethod("notifyMiuiWidgetUpdate", ContextClass)
    private val detailConstructor = PA_EXPRESS_INFO_DETAIL.toClass(classLoader, false).getDeclaredConstructor()
    private val buildDetails = "com.miui.personalassistant.service.express.bean.DetailEntry".toClass(classLoader, false)
        .getMethod("buildEntries", List::class.java, Int::class.javaPrimitiveType)

    val addQueryRecordMethod = preferencesClass.getMethod("addQueryRecord", ContextClass, entryClass)
    val putEntryMethod = cacheClass.getMethod("putExpressEntry", ContextClass, entryClass)
    val deleteMethod = cacheClass.getMethod("deleteExpress", ContextClass, List::class.java, Boolean::class.javaPrimitiveType)
    val setRemarkMethod = cacheClass.getMethod("setRemark", ContextClass, String::class.java, String::class.java)

    fun revision(orderNumber: String): Long = synchronized(mutationLock) {
        revisions[orderNumber.lowercase(Locale.ROOT)] ?: 0L
    }

    fun intentRevision(orderNumber: String): Long = synchronized(mutationLock) {
        intentRevisions[orderNumber.lowercase(Locale.ROOT)] ?: 0L
    }

    fun recordWrite(orderNumber: String): Long = synchronized(mutationLock) {
        val key = orderNumber.lowercase(Locale.ROOT)
        val revision = (revisions[key] ?: 0L) + 1
        revisions[key] = revision
        revision
    }

    fun invalidate(orderNumber: String): Long = synchronized(mutationLock) {
        recordWrite(orderNumber)
        val key = orderNumber.lowercase(Locale.ROOT)
        val revision = (intentRevisions[key] ?: 0L) + 1
        intentRevisions[key] = revision
        revision
    }

    fun copyEntry(entry: Any): ExpressEntryWrapper {
        val json = toJson.invoke(gson, entry) as String
        return fromJson.invoke(gson, json, entryClass)!!.toExpressEntryWrapper()
    }

    fun key(context: Context, entry: ExpressEntryWrapper): String = getKey.invoke(null, context, entry.expressEntryObject) as String

    fun readEntry(context: Context, key: String): ExpressEntryWrapper? {
        val json = get.invoke(null, context, key) as? String ?: return null
        val entry = fromJson.invoke(gson, json, entryClass)?.toExpressEntryWrapper() ?: return null
        entry.key = key
        entry.remark = getRemark.invoke(null, context, key) as? String
        return entry
    }

    fun recentEntries(context: Context): List<ExpressEntryWrapper> {
        // Match the host list's 90-day retention; this parameter is days, not a row limit.
        return (getLatest.invoke(null, context, 90) as? List<*>)?.mapNotNull { it?.toExpressEntryWrapper() }.orEmpty()
    }

    fun isDeleted(context: Context, orderNumber: String): Boolean {
        return (getDeleted.invoke(null, context) as Map<*, *>).keys.any { it.toString().equals(orderNumber, ignoreCase = true) }
    }

    fun isModified(context: Context, orderNumber: String): Boolean {
        return (getModified.invoke(null, context) as Map<*, *>).keys.any { it.toString().equals(orderNumber, ignoreCase = true) }
    }

    fun clearDeleted(context: Context, orderNumber: String) {
        val deleted = (getDeleted.invoke(null, context) as Map<*, *>).toMutableMap()
        if (deleted.keys.removeAll { it.toString().equals(orderNumber, ignoreCase = true) }) {
            setDeleted.invoke(null, context, deleted)
        }
        val deletedInfo = getDeletedInfo.invoke(null, context) as Map<*, *>
        for (key in deletedInfo.keys.filter { it.toString().equals(orderNumber, ignoreCase = true) }) {
            removeDeletedInfo.invoke(null, context, key.toString())
        }
    }

    /** Called under mutationLock by the shared host write hook. No network work belongs here. */
    fun preserveLocalChanges(context: Context, incoming: ExpressEntryWrapper) {
        val key = key(context, incoming)
        val current = readEntry(context, key) ?: return
        incoming.key = key
        incoming.remark = current.remark
        if (incoming.phone.isNullOrBlank()) {
            incoming.phone = current.phone
        }
        if ((current.latestTime?.toLongOrNull() ?: 0L) > (incoming.latestTime?.toLongOrNull() ?: 0L)) {
            incoming.latestTime = current.latestTime
            incoming.latestDetail = current.latestDetail
            incoming.details = current.details
            if (incoming.state != 107) {
                incoming.state = current.state
            }
        }
        if (current.state == 107 && isModified(context, incoming.orderNumber)) {
            incoming.state = current.state
        }
    }

    fun putEntry(context: Context, entry: ExpressEntryWrapper): Boolean {
        moduleWrite.set(true)
        try {
            return putEntryMethod.invoke(null, context, entry.expressEntryObject) == true
        } finally {
            moduleWrite.remove()
        }
    }

    fun updateDetails(entry: ExpressEntryWrapper, traces: List<ExpressTrace>, timestamp: Long, state: Int?) {
        if (state != null) {
            entry.state = state
        }
        val detail = detailConstructor.newInstance()
        detail.toExpressInfoDetailWrapper().apply {
            desc = traces.first().description
            time = traces.first().fullDateTime
        }
        entry.latestDetail = traces.first().description
        entry.latestTime = timestamp.toString()
        @Suppress("UNCHECKED_CAST")
        entry.details = buildDetails.invoke(null, listOf(detail), entry.state) as List<Any>
        entry.clickDisappear = false
    }

    fun notifyChanged(context: Context) {
        runCatching { notifyDataChanged.invoke(null, context) }.onFailure {
            YLog.warn("Failed to notify express list update", e = it)
        }
        runCatching { notifyWidget.invoke(null, context) }.onFailure {
            YLog.warn("Failed to notify express widget update", e = it)
        }
    }
}
