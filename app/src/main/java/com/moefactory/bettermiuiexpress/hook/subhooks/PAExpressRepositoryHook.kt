package com.moefactory.bettermiuiexpress.hook.subhooks

import android.content.Context
import android.os.Looper
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.highcapable.yukihookapi.extension.kavaref.hook
import com.highcapable.yukihookapi.extension.kavaref.toClassOrNull
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import com.moefactory.bettermiuiexpress.base.app.PA_EXPRESS_INFO_DETAIL
import com.moefactory.bettermiuiexpress.base.app.PA_EXPRESS_REPOSITOIRY
import com.moefactory.bettermiuiexpress.base.app.PREF_KEY_DEVICE_TRACK_ID
import com.moefactory.bettermiuiexpress.hook.PAExpressCache
import com.moefactory.bettermiuiexpress.ktx.JavaListClass
import com.moefactory.bettermiuiexpress.model.ExpressInfoWrapper
import com.moefactory.bettermiuiexpress.model.ExpressTrace
import com.moefactory.bettermiuiexpress.model.isXiaomiOrJingDong
import com.moefactory.bettermiuiexpress.model.shouldUseNativeUI
import com.moefactory.bettermiuiexpress.model.toExpressEntryWrapper
import com.moefactory.bettermiuiexpress.model.toExpressInfoDetailWrapper
import com.moefactory.bettermiuiexpress.model.toExpressInfoWrapper
import com.moefactory.bettermiuiexpress.model.toExpressTrace
import com.moefactory.bettermiuiexpress.repository.ExpressActualRepository
import com.moefactory.bettermiuiexpress.utils.ExpressCompanyUtils
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Proxy
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import com.highcapable.kavaref.extension.makeAccessible

// Enrich host refreshes and persist parcels queried from the host search page.
object PAExpressRepositoryHook : YukiBaseHooker() {

    private val refreshExecutorDelegate = lazy {
        Executors.newSingleThreadExecutor { runnable ->
            Thread(runnable, "BetterMiuiExpress-refresh")
        }
    }

    private val storageExecutorDelegate = lazy {
        Executors.newSingleThreadExecutor { runnable ->
            Thread(runnable, "BetterMiuiExpress-storage")
        }
    }

    private val refreshExecutor by refreshExecutorDelegate
    private val storageExecutor by storageExecutorDelegate
    private val workLock = Any()
    private var pendingTasks = 0
    private val pendingSearchRequests = mutableSetOf<SearchRequest>()

    @Volatile
    private var disposed = false

    private val deviceTrackId: String
        get() = preferences.getString(PREF_KEY_DEVICE_TRACK_ID)

    private var hostCache: PAExpressCache? = null
    private var contextField: Field? = null
    private val refreshOrders = ThreadLocal<MutableSet<String>>()
    private val serverWrite = ThreadLocal<Boolean>()
    private val explicitSearchWrite = ThreadLocal<Boolean>()
    private val searchResponse = ThreadLocal<SearchRequest>()
    private val runningQueries = ConcurrentHashMap<QueryKey, CompletableDeferred<Result<QueryResult?>>>()

    private data class QueryKey(val companyCode: String, val orderNumber: String, val phone: String?, val trackId: String)

    private data class SearchRequest(val orderNumber: String, val revision: Long)

    private data class QueryResult(
        val traces: List<ExpressTrace>,
        val timestamp: Long?,
        val state: Int?,
        val placeholder: Boolean = false
    )

    fun canReload(): Boolean = synchronized(workLock) {
        // A search callback can enqueue a worker before it completes; inspect both under the same lock.
        val networkIdle = ExpressActualRepository.isIdle()
        val idle = pendingTasks == 0 && pendingSearchRequests.isEmpty() && runningQueries.isEmpty() && networkIdle
        if (!idle) {
            YLog.warn(
                "Hot reload refused: pendingTasks=$pendingTasks, pendingSearchCallbacks=${pendingSearchRequests.size}, " +
                    "runningQueries=${runningQueries.size}, networkIdle=$networkIdle"
            )
        }
        idle
    }

    fun dispose() {
        disposed = true
        // Do not create unused executors just to shut them down.
        val executors = listOf(refreshExecutorDelegate, storageExecutorDelegate).filter { it.isInitialized() }.map { it.value }
        executors.forEach { it.shutdown() }
        executors.forEach {
            check(it.awaitTermination(1, TimeUnit.SECONDS)) { "Express worker did not stop" }
        }
        ExpressActualRepository.dispose()
        hostCache = null
        contextField = null
        runningQueries.clear()
        synchronized(workLock) { pendingSearchRequests.clear() }
    }

    private fun ExecutorService.executeTracked(block: () -> Unit) {
        synchronized(workLock) { pendingTasks++ }
        try {
            execute {
                try {
                    block()
                } finally {
                    synchronized(workLock) { pendingTasks-- }
                }
            }
        } catch (exception: Exception) {
            synchronized(workLock) { pendingTasks-- }
            throw exception
        }
    }

    override fun onHook() {
        val repositoryClass = PA_EXPRESS_REPOSITOIRY.toClassOrNull() ?: return
        hostCache = runCatching {
            contextField = repositoryClass.getDeclaredField("mContext").apply { isAccessible = true }
            PAExpressCache(repositoryClass.classLoader!!)
        }.onFailure {
            YLog.warn("Host cache API unavailable; search persistence is disabled", e = it)
        }.getOrNull()
        hostCache?.let { hookCache(it, repositoryClass) }

        // requestExpressAll posts this callback to the main thread. Keep saving and
        // notifying in their original order, but allow Retrofit callbacks to run.
        val refreshResultMethod = repositoryClass.declaredMethods.singleOrNull {
            it.name == "lambda\$requestExpressAll\$1" && it.parameterCount == 1 && it.returnType == Void.TYPE
        }
        if (refreshResultMethod != null) {
            refreshResultMethod.hook {
                intercept {
                    val repository = instance
                    val responseArgs = args.copyOf()
                    refreshExecutor.executeTracked {
                        val seenOrders = mutableSetOf<String>()
                        refreshOrders.set(seenOrders)
                        try {
                            runCatching {
                                refreshResultMethod.invokeOriginal(repository, *responseArgs)
                            }.onFailure {
                                YLog.error("Failed to save express refresh result", e = it)
                            }
                        } finally {
                            refreshOrders.remove()
                            repositoryContext(repository)?.let { refreshLocalEntries(it, seenOrders) }
                        }
                    }
                    null
                }
            }
        } else {
            YLog.warn("Express refresh callback not found; main-thread saves will skip detail queries")
        }

        // Both an empty successful response and a failed host request still need a local refresh.
        repositoryClass.declaredMethods.singleOrNull {
            it.name == "lambda\$requestExpressAll\$2" && it.parameterTypes.contentEquals(arrayOf(Throwable::class.java))
        }?.hook {
            after {
                repositoryContext(instance)?.let { context ->
                    refreshExecutor.executeTracked { refreshLocalEntries(context) }
                }
            }
        }

        repositoryClass.resolve().firstMethod {
            name = "requestExpressAllSync"
            parameters(Int::class.javaPrimitiveType!!)
        }.hook {
            intercept {
                if (refreshOrders.get() != null) {
                    return@intercept invokeOriginal(*args)
                }
                val context = repositoryContext(instance)
                val seenOrders = mutableSetOf<String>()
                refreshOrders.set(seenOrders)
                try {
                    invokeOriginal(*args)
                } finally {
                    refreshOrders.remove()
                    if (context != null) {
                        refreshExecutor.executeTracked { refreshLocalEntries(context, seenOrders) }
                    }
                }
            }
        }

        repositoryClass.resolve().firstMethod {
            name = "saveExpress"
            parameters(JavaListClass)
        }.hook {
            intercept {
                val expressInfoList = firstArg().get<List<*>>()?.mapNotNull { it?.toExpressInfoWrapper() }.orEmpty()
                val context = repositoryContext(instance)
                val seenOrders = mutableSetOf<String>()
                // Unknown main-thread callers save first, then enrich their cached records in the background.
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    YLog.warn("Skipping express detail queries on the main thread")
                } else {
                    runBlocking {
                        for (expressInfoWrapper in expressInfoList) {
                            try {
                                val companyCode = expressInfoWrapper.companyCode
                                val mailNumber = expressInfoWrapper.orderNumber
                                seenOrders.add(mailNumber.lowercase(Locale.ROOT))
                                if (mailNumber.isBlank() || expressInfoWrapper.isXiaomiOrJingDong ||
                                    (context != null && hostCache?.isDeleted(context, mailNumber) == true)
                                ) {
                                    continue
                                }
                                val phoneNumber = expressInfoWrapper.phone?.takeIf { it.isNotBlank() } ?: expressInfoWrapper.sendPhone
                                val queryResult = fetchExpressDetails(mailNumber, companyCode, phoneNumber) ?: continue

                                val detailClass = PA_EXPRESS_INFO_DETAIL.toClassOrNull() ?: return@runBlocking
                                saveLatestExpressTrace(expressInfoWrapper, detailClass, queryResult)
                            } catch (exception: Exception) {
                                YLog.warn("Failed to enrich express details; keeping host data", e = exception)
                            }
                        }
                    }
                }
                refreshOrders.get()?.addAll(seenOrders)
                val previousWrite = serverWrite.get()
                serverWrite.set(true)
                try {
                    invokeOriginal(*args)
                } finally {
                    if (previousWrite == null) {
                        serverWrite.remove()
                    } else {
                        serverWrite.set(previousWrite)
                    }
                    if (refreshOrders.get() == null && context != null) {
                        refreshExecutor.executeTracked { refreshLocalEntries(context, seenOrders) }
                    }
                }
            }
        }
    }

    private fun hookCache(cache: PAExpressCache, repositoryClass: Class<*>) {
        runCatching {
            val classLoader = repositoryClass.classLoader!!
            val widgetItemClass = "com.miui.personalassistant.service.express.widget.model.ExpressItem".toClass(classLoader, false)
            val widgetContextField = widgetItemClass.getDeclaredField("mContext").apply { makeAccessible() }
            val totalListField = widgetItemClass.getDeclaredField("mTotalList").apply { makeAccessible() }
            val setupExpressList = widgetItemClass.getDeclaredMethod("setupExpressList", JavaListClass).apply { makeAccessible() }
            val sortExpressEntries = "com.miui.personalassistant.service.express.util.ExpressUtils".toClass(classLoader, false)
                .getMethod("sortExpressEntries", JavaListClass)

            widgetItemClass.getDeclaredMethod("fetchDisplayedList").hook {
                intercept {
                    synchronized(cache.mutationLock) {
                        val original = invokeOriginal(*args)
                        runCatching {
                            val displayed = original as? List<*> ?: return@runCatching original
                            val context = widgetContextField.get(instance) as? Context ?: return@runCatching original
                            // The host has already loaded its 30-day cache. Do not change the server's order-number index.
                            val localEntries = totalListField.get(instance) as? List<*> ?: return@runCatching original
                            val merged = displayed.filterNotNull().toMutableList()
                            val seenOrders = merged.mapTo(mutableSetOf()) { it.toExpressEntryWrapper().orderNumber.lowercase(Locale.ROOT) }
                            var changed = false
                            for (value in localEntries.filterNotNull()) {
                                val entry = value.toExpressEntryWrapper()
                                val orderNumber = entry.orderNumber.lowercase(Locale.ROOT)
                                if (orderNumber.isBlank() || orderNumber in seenOrders || !entry.displayOnCard ||
                                    cache.isDeleted(context, entry.orderNumber)
                                ) {
                                    continue
                                }

                                // Reuse the host's state/date rules per entry, without its three-entry fallback limit.
                                val eligible = setupExpressList.invoke(instance, listOf(value)) as? List<*>
                                if (eligible.isNullOrEmpty()) {
                                    continue
                                }
                                merged.add(value)
                                seenOrders.add(orderNumber)
                                changed = true
                            }
                            if (changed) {
                                // Let recent local parcels compete for the widget's limited rows using the host's ordering.
                                sortExpressEntries.invoke(null, merged)
                                merged
                            } else {
                                original
                            }
                        }.onFailure {
                            YLog.warn("Failed to merge local express into widget; keeping host list", e = it)
                        }.getOrDefault(original)
                    }
                }
            }
        }.onFailure {
            YLog.warn("Host widget list API unavailable; local express widget merge is disabled", e = it)
        }

        cache.deleteMethod.hook {
            intercept {
                synchronized(cache.mutationLock) {
                    arg(1).get<List<*>>()?.forEach { entry ->
                        runCatching {
                            entry?.toExpressEntryWrapper()?.orderNumber?.let { cache.invalidate(it) }
                        }.onFailure { YLog.warn("Failed to invalidate pending express query", e = it) }
                    }
                    invokeOriginal(*args)
                }
            }
        }
        cache.putEntryMethod.hook {
            intercept {
                synchronized(cache.mutationLock) {
                    val context = firstArg().get<Context>()
                    val entry = arg(1).get<Any>()?.toExpressEntryWrapper()
                    if (context != null && entry != null) {
                        // saveExpress's own deletion map may have been read before a concurrent deletion.
                        if (explicitSearchWrite.get() != true && cache.isDeleted(context, entry.orderNumber)) {
                            return@synchronized false
                        }
                        if (cache.isModuleWrite || serverWrite.get() == true) {
                            runCatching { cache.preserveLocalChanges(context, entry) }.onFailure {
                                YLog.warn("Failed to merge current express data; keeping host write", e = it)
                            }
                        }
                    }
                    val saved = invokeOriginal(*args)
                    if (saved == true && entry != null) {
                        cache.recordWrite(entry.orderNumber)
                    }
                    saved
                }
            }
        }
        cache.setRemarkMethod.hook {
            intercept {
                synchronized(cache.mutationLock) { invokeOriginal(*args) }
            }
        }

        // Carry the request's generation through its listener; a late response must not undo a later deletion.
        val singleRequestMethod = repositoryClass.declaredMethods.singleOrNull {
            it.name == "requestExpressSingle" && it.parameterCount == 4 && it.parameterTypes[3].isInterface
        }
        if (singleRequestMethod != null) {
            val listenerInterface = singleRequestMethod.parameterTypes[3]
            singleRequestMethod.hook {
                intercept {
                    if (arg(2).get<String>() != "2") {
                        return@intercept invokeOriginal(*args)
                    }
                    var pendingRequest: SearchRequest? = null
                    runCatching {
                        val entry = firstArg().get<Any>()?.toExpressEntryWrapper() ?: return@runCatching
                        val listener = arg(3).get<Any>() ?: return@runCatching
                        val orderNumber = entry.orderNumber
                        val request = SearchRequest(orderNumber, cache.invalidate(orderNumber))
                        val proxy = Proxy.newProxyInstance(listenerInterface.classLoader, arrayOf(listenerInterface)) { proxy, method, arguments ->
                            if (method.declaringClass == Any::class.java) {
                                return@newProxyInstance when (method.name) {
                                    "equals" -> proxy === arguments?.firstOrNull()
                                    "hashCode" -> System.identityHashCode(proxy)
                                    else -> "BetterMiuiExpress search listener"
                                }
                            }
                            val previous = searchResponse.get()
                            if (!disposed && method.name == "success") {
                                searchResponse.set(request)
                            }
                            try {
                                method.invoke(listener, *arguments.orEmpty())
                            } catch (exception: InvocationTargetException) {
                                throw exception.targetException
                            } finally {
                                if (method.name == "success") {
                                    if (previous == null) {
                                        searchResponse.remove()
                                    } else {
                                        searchResponse.set(previous)
                                    }
                                }
                                synchronized(workLock) { pendingSearchRequests.remove(request) }
                            }
                        }
                        pendingRequest = request
                        synchronized(workLock) { pendingSearchRequests.add(request) }
                        arg(3).set(proxy)
                    }.onFailure {
                        synchronized(workLock) { pendingSearchRequests.remove(pendingRequest) }
                        YLog.warn("Failed to track host search request", e = it)
                    }
                    try {
                        invokeOriginal(*args)
                    } catch (exception: Throwable) {
                        synchronized(workLock) { pendingSearchRequests.remove(pendingRequest) }
                        throw exception
                    }
                }
            }
        } else {
            YLog.warn("Host search request callback unavailable; deleted parcels will not be re-added")
        }

        cache.addQueryRecordMethod.hook {
            after {
                runCatching {
                    val context = firstArg().get<Context>()?.applicationContext ?: return@after
                    val original = arg(1).get<Any>() ?: return@after
                    val (entry, revision) = synchronized(cache.mutationLock) {
                        val entry = cache.copyEntry(original)
                        if (entry.companyCode.isBlank() || entry.orderNumber.isBlank()) {
                            return@after
                        }
                        val request = searchResponse.get()
                        if (request != null) {
                            if (!entry.orderNumber.equals(request.orderNumber, ignoreCase = true) ||
                                cache.intentRevision(entry.orderNumber) != request.revision
                            ) {
                                return@after
                            }
                            entry to request.revision
                        } else {
                            // Fail closed for re-adds if this host version cannot identify the originating request.
                            if (cache.isDeleted(context, entry.orderNumber)) {
                                return@after
                            }
                            entry to cache.invalidate(entry.orderNumber)
                        }
                    }
                    storageExecutor.executeTracked {
                        runCatching {
                            val saved = synchronized(cache.mutationLock) {
                                if (cache.intentRevision(entry.orderNumber) != revision) {
                                    false
                                } else {
                                    if (!entry.shouldUseNativeUI()) {
                                        entry.clickDisappear = false
                                    }
                                    entry.displayOnCard = true
                                    explicitSearchWrite.set(true)
                                    try {
                                        cache.putEntry(context, entry).also {
                                            if (it) {
                                                // Only a new explicit search can re-add a deleted parcel.
                                                cache.clearDeleted(context, entry.orderNumber)
                                            }
                                        }
                                    } finally {
                                        explicitSearchWrite.remove()
                                    }
                                }
                            }
                            if (saved) {
                                cache.notifyChanged(context)
                                refreshExecutor.executeTracked { refreshLocalEntries(context, onlyOrder = entry.orderNumber) }
                            }
                        }.onFailure { YLog.error("Failed to persist searched express", e = it) }
                    }
                }.onFailure { YLog.warn("Failed to capture searched express", e = it) }
            }
        }
    }

    private fun repositoryContext(repository: Any): Context? {
        return runCatching { (contextField?.get(repository) as? Context)?.applicationContext }.getOrNull()
    }

    private fun refreshLocalEntries(context: Context, excludedOrders: Set<String> = emptySet(), onlyOrder: String? = null) {
        val cache = hostCache ?: return
        if (deviceTrackId.isBlank()) {
            return
        }
        var changed = false
        runCatching {
            val snapshots = synchronized(cache.mutationLock) {
                cache.recentEntries(context).map { it to cache.revision(it.orderNumber) }
            }
            runBlocking {
                for ((entry, revision) in snapshots.distinctBy { it.first.orderNumber.lowercase(Locale.ROOT) }) {
                    try {
                        val orderNumber = entry.orderNumber
                        if (orderNumber.isBlank() || entry.companyCode.isBlank() || entry.state == 107 || entry.shouldUseNativeUI() ||
                            orderNumber.lowercase(Locale.ROOT) in excludedOrders ||
                            (onlyOrder != null && !orderNumber.equals(onlyOrder, ignoreCase = true)) || cache.isDeleted(context, orderNumber)
                        ) {
                            continue
                        }
                        val result = fetchExpressDetails(orderNumber, entry.companyCode, entry.phone) ?: continue
                        val timestamp = result.timestamp ?: continue
                        if (result.placeholder) {
                            continue
                        }
                        val saved = synchronized(cache.mutationLock) {
                            if (cache.revision(orderNumber) != revision || cache.isDeleted(context, orderNumber)) {
                                return@synchronized false
                            }
                            val current = cache.readEntry(context, cache.key(context, entry)) ?: return@synchronized false
                            // The list reader substitutes insertion time for missing logistics time.
                            // Compare only the actual JSON time, or a new parcel's first real trace would be rejected.
                            val currentTime = current.latestTime?.toLongOrNull() ?: 0L
                            if (current.state == 107 || timestamp < currentTime) {
                                return@synchronized false
                            }
                            val state = result.state.takeUnless { cache.isModified(context, orderNumber) }
                            cache.updateDetails(current, result.traces, timestamp, state)
                            cache.putEntry(context, current)
                        }
                        changed = changed || saved
                    } catch (exception: Exception) {
                        YLog.warn("Failed to refresh local express; keeping cached data", e = exception)
                    }
                }
            }
        }.onFailure { YLog.warn("Failed to load local express records", e = it) }
        if (changed) {
            cache.notifyChanged(context)
        }
    }

    private fun saveLatestExpressTrace(
        expressInfoWrapper: ExpressInfoWrapper,
        detailClass: Class<out Any>,
        result: QueryResult
    ) {
        expressInfoWrapper.clickDisappear = false
        val originalDetails = expressInfoWrapper.details
        val detail = originalDetails?.firstOrNull() ?: detailClass.getDeclaredConstructor().newInstance().also {
            expressInfoWrapper.details = arrayListOf(it)
        }
        detail.toExpressInfoDetailWrapper().apply {
            desc = result.traces.first().description
            if (!result.placeholder || originalDetails.isNullOrEmpty()) {
                time = result.traces.first().fullDateTime
            }
        }
        if (result.state != null && !result.placeholder) {
            expressInfoWrapper.stateNum = result.state
        }
    }

    private suspend fun fetchExpressDetails(
        mailNumber: String, originalCompanyCode: String, phoneNumber: String?
    ): QueryResult? {
        val trackId = deviceTrackId
        if (trackId.isBlank()) {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
            val currentDateTimeString = sdf.format(Date())

            return QueryResult(
                traces = listOf(
                    ExpressTrace(
                        fullDateTime = currentDateTimeString,
                        date = currentDateTimeString.split(" ")[0],
                        time = currentDateTimeString.split(" ")[1],
                        description = "请先打开模块主界面完成初始化"
                    )
                ),
                timestamp = null,
                state = null,
                placeholder = true
            )
        }

        val key = QueryKey(originalCompanyCode, mailNumber.lowercase(Locale.ROOT), phoneNumber, trackId)
        val pending = CompletableDeferred<Result<QueryResult?>>()
        val existing = runningQueries.putIfAbsent(key, pending)
        if (existing != null) {
            return existing.await().getOrThrow()
        }
        try {
            val result = runCatching {
                val companyCode = ExpressCompanyUtils.convertCode(originalCompanyCode)
                    ?: ExpressActualRepository.queryCompanyActual(mailNumber).firstOrNull()?.companyCode
                    ?: return@runCatching null
                val response = ExpressActualRepository.queryExpressDetailsFromKuaiDi100Actual(companyCode, mailNumber, phoneNumber, trackId)
                val details = response?.lastResult ?: return@runCatching null
                val traces = details.data?.map { it.toExpressTrace() }?.sortedDescending()?.takeIf { it.isNotEmpty() }
                    ?: return@runCatching null
                val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).apply { isLenient = false }
                    .parse(traces.first().fullDateTime)?.time?.takeIf { it > 0 } ?: return@runCatching null
                // Host ExpressState uses 101..111, not the courier API's 0..8 codes.
                val state = when (details.state?.toIntOrNull()) {
                    0, 6, 7, 8 -> 104
                    1 -> 103
                    2 -> 109
                    3 -> 107
                    4 -> 108
                    5 -> 105
                    else -> null
                }
                QueryResult(traces, timestamp, state)
            }
            pending.complete(result)
            return result.getOrThrow()
        } finally {
            runningQueries.remove(key, pending)
        }
    }
}
