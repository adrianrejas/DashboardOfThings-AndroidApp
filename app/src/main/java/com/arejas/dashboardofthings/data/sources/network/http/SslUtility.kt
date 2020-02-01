package com.arejas.dashboardofthings.data.sources.network.http


import android.content.Context

import java.io.BufferedInputStream
import java.io.FileInputStream
import java.io.InputStream
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.HashMap

import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

class SslUtility(context: Context) {
    private var mContext: Context? = null
    private val mSocketFactoryMap = HashMap<Int, SSLSocketFactory>()
    private val mTrustManagersMap = HashMap<Int, X509TrustManager>()

    init {
        mContext = context
    }

    fun createSocketFactoryAndTrustManager(networkId: Int, certificateFile: String) {

        val result = mSocketFactoryMap[networkId]    // check to see if already created

        if (null == result && null != mContext) {                    // not cached so need to load server certificate

            try {
                // Load CAs from an InputStream
                // (could be from a resource or ByteArrayInputStream or ...)
                val cf = CertificateFactory.getInstance("X.509")
                val caInput = BufferedInputStream(FileInputStream(certificateFile))
                val ca: Certificate
                try {
                    ca = cf.generateCertificate(caInput)
                    println("ca=" + (ca as X509Certificate).subjectDN)
                } finally {
                    caInput.close()
                }

                // Create a KeyStore containing our trusted CAs
                val keyStoreType = KeyStore.getDefaultType()
                val keyStore = KeyStore.getInstance(keyStoreType)
                keyStore.load(null, null)
                keyStore.setCertificateEntry("ca", ca)

                // Create a TrustManager that trusts the CAs in our KeyStore
                val tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm()
                val tmf = TrustManagerFactory.getInstance(tmfAlgorithm)
                tmf.init(keyStore)
                if (tmf.trustManagers.size != 1 || tmf.trustManagers[0] !is X509TrustManager) {
                    return
                }

                // Create an SSLContext that uses our TrustManager
                val sslContext = SSLContext.getInstance("TLS")
                sslContext.init(null, tmf.trustManagers, null)

                mSocketFactoryMap[networkId] = sslContext.socketFactory    // cache for reuse
                mTrustManagersMap[networkId] =
                    tmf.trustManagers[0] as X509TrustManager    // cache for reuse
            } catch (ex: Exception) {
            }

        }
    }

    fun getSocketFactory(networkId: Int): SSLSocketFactory? {
        return mSocketFactoryMap[networkId]
    }

    fun getTrustedManager(networkId: Int): X509TrustManager? {
        return mTrustManagersMap[networkId]
    }

    companion object {

        private var mInstance: SslUtility? = null

        val instance: SslUtility
            get() {
                if (null == mInstance) {
                    throw RuntimeException("first call must be to SslUtility.newInstance(Context) ")
                }
                return mInstance
            }

        fun newInstance(context: Context): SslUtility {
            if (null == mInstance) {
                mInstance = SslUtility(context)
            }
            return mInstance
        }
    }

}
