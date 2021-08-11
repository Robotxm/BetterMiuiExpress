package com.moefactory.httputils.cookie

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import okhttp3.Cookie
import okhttp3.HttpUrl
import okhttp3.internal.canParseAsIpAddress
import java.util.concurrent.ConcurrentHashMap

class PersistentCookieStore(context: Context) : CookieStore {

    // domain -> token (domain, name and path) -> cookie
    private val cookies: MutableMap<String, ConcurrentHashMap<String, Cookie>>
    private val cookiePrefs: SharedPreferences

    init {
        cookiePrefs = context.getSharedPreferences(COOKIE_PREFS, Context.MODE_PRIVATE)
        cookies = HashMap()

        // Load cookies from shared preference
        for ((key, value) in cookiePrefs.all) {
            // Load domains
            if (key.startsWith(HOST_PREFIX)) {
                cookies[key.removePrefix(HOST_PREFIX)] = ConcurrentHashMap()
                // Load cookies
                for (cookieToken in (value as String).split(",")) {
                    cookiePrefs.getString(
                        COOKIE_NAME_PREFIX + cookieToken,
                        null
                    )?.let { encodedCookie ->
                        SerializableCookie.decode(encodedCookie)?.let {
                            if (!cookies.containsKey(it.domain)) {
                                cookies[it.domain] = ConcurrentHashMap()
                            }
                            cookies[it.domain]!![cookieToken] = it
                        }
                    }
                }
            }
        }
    }

    @Synchronized
    override fun add(cookie: Cookie) {
        if (cookie.isExpired()) {
            remove(cookie)
        } else {
            add(cookie, getCookieToken(cookie))
        }
    }

    @Synchronized
    override fun add(cookies: List<Cookie>) {
        for (cookie in cookies) {
            add(cookie)
        }
    }

    @Synchronized
    override fun get(url: HttpUrl): List<Cookie> {
        val ret = mutableListOf<Cookie>()

        for (matchedCookies in cookies.filter { domainMatch(url.host, it.key) }.values) {
            for (cookie in matchedCookies.values) {
                if (cookie.isExpired()) {
                    remove(cookie)
                } else {
                    if (cookie.matches(url)) {
                        ret.add(cookie)
                    }
                }
            }
        }

        return ret
    }

    @Synchronized
    override fun getByDomain(url: HttpUrl): List<Cookie> {
        val ret = mutableListOf<Cookie>()

        for (matchedCookies in cookies.filter { domainMatch(url.host, it.key) }.values) {
            for (cookie in matchedCookies.values) {
                if (cookie.isExpired()) {
                    remove(cookie)
                } else {
                    ret.add(cookie)
                }
            }
        }

        return ret
    }

    override fun removeByDomain(url: HttpUrl): Boolean {
        val it = cookies.iterator()
        while (it.hasNext()) {
            val kv = it.next()
            val domain = kv.key
            val cookiesOfDomain = kv.value
            if (domainMatch(url.host, domain)) {
                it.remove()
                cookiePrefs.edit {
                    remove(HOST_PREFIX + domain)
                    cookiesOfDomain.keys.forEach {
                        remove(HOST_PREFIX + domain)
                        remove(COOKIE_NAME_PREFIX + it)
                    }
                }
            }
        }

        return true
    }

    @Synchronized
    override fun removeAll(): Boolean {
        // Clear memory
        cookies.clear()
        // Clear shared preference
        cookiePrefs.edit { clear() }
        return true
    }

    /**
     * Get a unique token for specified token
     */
    private fun getCookieToken(cookie: Cookie) = "${cookie.domain}@${cookie.name}@${cookie.path}"

    /**
     * Add a new cookie to map and shared preference
     */
    @Synchronized
    private fun add(cookie: Cookie, cookieToken: String) {
        // Save to map
        if (!cookies.containsKey(cookie.domain)) {
            cookies[cookie.domain] = ConcurrentHashMap()
        }
        cookies[cookie.domain]!![cookieToken] = cookie
        // Save to shared preference
        cookiePrefs.edit {
            putString(HOST_PREFIX + cookie.domain, cookies[cookie.domain]!!.keys.joinToString(","))
            putString(
                COOKIE_NAME_PREFIX + cookieToken,
                SerializableCookie.encode(SerializableCookie(cookie))
            )
        }
    }

    /**
     * Remove specified cookie from cookie store
     */
    private fun remove(cookie: Cookie): Boolean {
        val cookieToken = getCookieToken(cookie)
        if (!cookies.containsKey(cookie.domain)) {
            return false
        }
        if (!cookies[cookie.domain]!!.containsKey(cookieToken)) {
            return false
        }

        // Remove from map
        cookies[cookie.domain]!!.remove(cookieToken)
        // Remove from shared preference
        if (cookiePrefs.contains(COOKIE_NAME_PREFIX + cookieToken)) {
            cookiePrefs.edit { remove(COOKIE_NAME_PREFIX + cookieToken) }
        }

        return true
    }

    /**
     * Return true if [urlHost] matches [domain].
     *
     * Refer to [RFC6265](https://tools.ietf.org/html/rfc6265) for further information
     */
    private fun domainMatch(urlHost: String, domain: String): Boolean {
        if (urlHost == domain) {
            return true // As in 'example.com' matching 'example.com'.
        }

        return urlHost.endsWith(domain) &&
                urlHost[urlHost.length - domain.length - 1] == '.' &&
                !urlHost.canParseAsIpAddress()
    }

    companion object {

        private const val COOKIE_PREFS = "cookies" // Shared preference name
        private const val COOKIE_NAME_PREFIX = "cookie_" // Cookie prefix
        private const val HOST_PREFIX = "host_" // Host prefix

        /**
         * Return true if cookie is expired
         */
        private fun Cookie.isExpired() = this.expiresAt < System.currentTimeMillis()
    }
}
