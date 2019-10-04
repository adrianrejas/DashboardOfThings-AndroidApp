package com.arejas.dashboardofthings.data.sources.network.http;


import android.content.Context;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class SslUtility {

    private static SslUtility		mInstance = null;
    private Context mContext = null;
    private HashMap<Integer, SSLSocketFactory> mSocketFactoryMap = new HashMap<Integer, SSLSocketFactory>();
    private HashMap<Integer, X509TrustManager> mTrustManagersMap = new HashMap<Integer, X509TrustManager>();

    public SslUtility(Context context) {
        mContext = context;
    }

    public static SslUtility getInstance( ) {
        if ( null == mInstance ) {
            throw new RuntimeException("first call must be to SslUtility.newInstance(Context) ");
        }
        return mInstance;
    }

    public static SslUtility newInstance( Context context ) {
        if ( null == mInstance ) {
            mInstance = new SslUtility( context );
        }
        return mInstance;
    }

    public void createSocketFactoryAndTrustManager(int networkId, String certificateFile ) {

        SSLSocketFactory result = mSocketFactoryMap.get(networkId);  	// check to see if already created

        if ( ( null == result) && ( null != mContext ) ) {					// not cached so need to load server certificate

            try {
                // Load CAs from an InputStream
                // (could be from a resource or ByteArrayInputStream or ...)
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                InputStream caInput = new BufferedInputStream(new FileInputStream(certificateFile));
                Certificate ca;
                try {
                    ca = cf.generateCertificate(caInput);
                    System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
                } finally {
                    caInput.close();
                }

                // Create a KeyStore containing our trusted CAs
                String keyStoreType = KeyStore.getDefaultType();
                KeyStore keyStore = KeyStore.getInstance(keyStoreType);
                keyStore.load(null, null);
                keyStore.setCertificateEntry("ca", ca);

                // Create a TrustManager that trusts the CAs in our KeyStore
                String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
                tmf.init(keyStore);
                if (tmf.getTrustManagers().length != 1 || !(tmf.getTrustManagers()[0] instanceof X509TrustManager)) {
                    return;
                }

                // Create an SSLContext that uses our TrustManager
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, tmf.getTrustManagers(), null);

                mSocketFactoryMap.put( networkId, sslContext.getSocketFactory());	// cache for reuse
                mTrustManagersMap.put( networkId, (X509TrustManager) tmf.getTrustManagers()[0]);	// cache for reuse
            }
            catch ( Exception ex ) {}
        }
    }

    public SSLSocketFactory getSocketFactory(int networkId ) {
        return mSocketFactoryMap.get(networkId);
    }

    public X509TrustManager getTrustedManager(int networkId ) {
        return mTrustManagersMap.get(networkId);
    }

}
