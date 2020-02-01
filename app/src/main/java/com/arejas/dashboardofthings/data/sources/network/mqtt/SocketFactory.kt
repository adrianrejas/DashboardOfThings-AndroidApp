package com.arejas.dashboardofthings.data.sources.network.mqtt

import android.util.Log

import java.io.IOException
import java.io.InputStream
import java.net.InetAddress
import java.net.Socket
import java.security.KeyManagementException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.UnrecoverableKeyException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.Enumeration

import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.security.cert.CertificateException

/**
 * Original SocketFactory file taken from https://github.com/owntracks/android
 */

class SocketFactory @Throws(
    KeyStoreException::class,
    NoSuchAlgorithmException::class,
    IOException::class,
    KeyManagementException::class,
    java.security.cert.CertificateException::class,
    UnrecoverableKeyException::class
)
@JvmOverloads constructor(options: SocketFactoryOptions = SocketFactoryOptions()) :
    javax.net.ssl.SSLSocketFactory() {
    private val factory: javax.net.ssl.SSLSocketFactory


    private val tmf: TrustManagerFactory

    val trustManagers: Array<TrustManager>
        get() = tmf.trustManagers

    class SocketFactoryOptions {

        var caCrtInputStream: InputStream? = null
            private set
        var caClientP12InputStream: InputStream? = null
            private set
        var caClientP12Password: String? = null
            private set

        fun withCaInputStream(stream: InputStream): SocketFactoryOptions {
            this.caCrtInputStream = stream
            return this
        }

        fun withClientP12InputStream(stream: InputStream): SocketFactoryOptions {
            this.caClientP12InputStream = stream
            return this
        }

        fun withClientP12Password(password: String): SocketFactoryOptions {
            this.caClientP12Password = password
            return this
        }

        fun hasCaCrt(): Boolean {
            return caCrtInputStream != null
        }

        fun hasClientP12Crt(): Boolean {
            return caClientP12Password != null
        }

        fun hasClientP12Password(): Boolean {
            return caClientP12Password != null && caClientP12Password != ""
        }
    }

    init {
        Log.v(this.toString(), "initializing CustomSocketFactory")

        tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        val kmf = KeyManagerFactory.getInstance("X509")


        if (options.hasCaCrt()) {

            val caKeyStore = KeyStore.getInstance(KeyStore.getDefaultType())
            caKeyStore.load(null, null)

            val caCF = CertificateFactory.getInstance("X.509")
            val ca = caCF.generateCertificate(options.caCrtInputStream) as X509Certificate
            val alias = ca.subjectX500Principal.name
            // Set propper alias name
            caKeyStore.setCertificateEntry(alias, ca)
            tmf.init(caKeyStore)
            val aliasesCA = caKeyStore.aliases()
            while (aliasesCA.hasMoreElements()) {
                val o = aliasesCA.nextElement()
            }


        } else {
            val keyStore = KeyStore.getInstance("AndroidCAStore")
            keyStore.load(null)
            tmf.init(keyStore)
        }

        if (options.hasClientP12Crt()) {

            val clientKeyStore = KeyStore.getInstance("PKCS12")

            clientKeyStore.load(
                options.caClientP12InputStream,
                if (options.hasClientP12Password()) options.caClientP12Password!!.toCharArray() else CharArray(
                    0
                )
            )
            kmf.init(
                clientKeyStore,
                if (options.hasClientP12Password()) options.caClientP12Password!!.toCharArray() else CharArray(
                    0
                )
            )

            Log.v(this.toString(), "Client .p12 Keystore content: ")
            val aliasesClientCert = clientKeyStore.aliases()
            while (aliasesClientCert.hasMoreElements()) {
                val o = aliasesClientCert.nextElement()
            }
        } else {
            Log.v(this.toString(), "Client .p12 sideload: false, using null CLIENT cert")
            kmf.init(null, null)
        }

        // Create an SSLContext that uses our TrustManager
        val context = SSLContext.getInstance("TLSv1.2")
        context.init(kmf.keyManagers, trustManagers, null)
        this.factory = context.socketFactory

    }

    override fun getDefaultCipherSuites(): Array<String> {
        return this.factory.defaultCipherSuites
    }

    override fun getSupportedCipherSuites(): Array<String> {
        return this.factory.supportedCipherSuites
    }

    @Throws(IOException::class)
    override fun createSocket(): Socket {
        val r = this.factory.createSocket() as SSLSocket
        r.enabledProtocols = arrayOf("TLSv1", "TLSv1.1", "TLSv1.2")
        return r
    }

    @Throws(IOException::class)
    override fun createSocket(s: Socket, host: String, port: Int, autoClose: Boolean): Socket {
        val r = this.factory.createSocket(s, host, port, autoClose) as SSLSocket
        r.enabledProtocols = arrayOf("TLSv1", "TLSv1.1", "TLSv1.2")
        return r
    }

    @Throws(IOException::class)
    override fun createSocket(host: String, port: Int): Socket {

        val r = this.factory.createSocket(host, port) as SSLSocket
        r.enabledProtocols = arrayOf("TLSv1", "TLSv1.1", "TLSv1.2")
        return r
    }

    @Throws(IOException::class)
    override fun createSocket(
        host: String,
        port: Int,
        localHost: InetAddress,
        localPort: Int
    ): Socket {
        val r = this.factory.createSocket(host, port, localHost, localPort) as SSLSocket
        r.enabledProtocols = arrayOf("TLSv1", "TLSv1.1", "TLSv1.2")
        return r
    }

    @Throws(IOException::class)
    override fun createSocket(host: InetAddress, port: Int): Socket {
        val r = this.factory.createSocket(host, port) as SSLSocket
        r.enabledProtocols = arrayOf("TLSv1", "TLSv1.1", "TLSv1.2")
        return r
    }

    @Throws(IOException::class)
    override fun createSocket(
        address: InetAddress,
        port: Int,
        localAddress: InetAddress,
        localPort: Int
    ): Socket {
        val r = this.factory.createSocket(address, port, localAddress, localPort) as SSLSocket
        r.enabledProtocols = arrayOf("TLSv1", "TLSv1.1", "TLSv1.2")
        return r
    }
}