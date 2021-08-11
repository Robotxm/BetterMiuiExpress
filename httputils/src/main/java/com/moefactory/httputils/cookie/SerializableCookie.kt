package com.moefactory.httputils.cookie

import android.util.Base64
import okhttp3.Cookie
import java.io.*

class SerializableCookie(@Transient private val cookie: Cookie) : Serializable {

    companion object {

        private const val serialVersionUID = 6374381828722046732L

        fun encode(cookie: SerializableCookie?): String? {
            if (cookie == null) {
                return null
            }
            val os = ByteArrayOutputStream()
            try {
                val outputStream = ObjectOutputStream(os)
                outputStream.writeObject(cookie)
            } catch (e: IOException) {
                e.printStackTrace()
                return null
            }

            return Base64.encodeToString(os.toByteArray(), Base64.NO_WRAP)
        }

        fun decode(cookieString: String): Cookie? {
            val bytes = Base64.decode(cookieString, Base64.NO_WRAP)
            val byteArrayInputStream = ByteArrayInputStream(bytes)
            try {
                val objectInputStream = ObjectInputStream(byteArrayInputStream)
                return (objectInputStream.readObject() as SerializableCookie).getCookie()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            }
            return null
        }
    }

    @Transient
    private var clientCookie: Cookie? = null

    fun getCookie(): Cookie {
        var bestCookie = cookie
        if (clientCookie != null) {
            bestCookie = clientCookie as Cookie
        }
        return bestCookie
    }

    private fun writeObject(out: ObjectOutputStream) {
        out.writeObject(cookie.name)
        out.writeObject(cookie.value)
        out.writeLong(cookie.expiresAt)
        out.writeObject(cookie.domain)
        out.writeObject(cookie.path)
        out.writeBoolean(cookie.secure)
        out.writeBoolean(cookie.httpOnly)
        out.writeBoolean(cookie.hostOnly)
        out.writeBoolean(cookie.persistent)
    }

    private fun readObject(oin: ObjectInputStream) {
        val name = oin.readObject() as String
        val value = oin.readObject() as String
        val expiresAt = oin.readLong()
        val domain = oin.readObject() as String
        val path = oin.readObject() as String
        val secure = oin.readBoolean()
        val httpOnly = oin.readBoolean()
        val hostOnly = oin.readBoolean()
        val persistent = oin.readBoolean()

        val builder = Cookie.Builder()
            .name(name)
            .value(value)
            .expiresAt(expiresAt)
            .path(path)

        if (hostOnly) {
            builder.hostOnlyDomain(domain)
        } else {
            builder.domain(domain)
        }
        if (secure) {
            builder.secure()
        }
        if (httpOnly) {
            builder.httpOnly()
        }
        clientCookie = builder.build()
    }
}