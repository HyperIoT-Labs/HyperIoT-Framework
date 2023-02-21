/*
 * Copyright 2019-2023 HyperIoT
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package it.acsoftware.hyperiot.base.security.util;

import it.acsoftware.hyperiot.base.api.*;
import it.acsoftware.hyperiot.base.exception.HyperIoTRuntimeException;
import it.acsoftware.hyperiot.base.util.HyperIoTConstants;
import it.acsoftware.hyperiot.base.util.HyperIoTUtil;
import it.acsoftware.hyperiot.osgi.util.filter.OSGiFilterBuilder;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.openssl.PEMException;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.x500.X500Principal;
import javax.security.auth.x500.X500PrivateCredential;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Author Aristide Cittadino.
 * This class helps developers to interact with permission system
 */
public class HyperIoTSecurityUtil {
    private static Logger log = LoggerFactory.getLogger(HyperIoTSecurityUtil.class.getName());
    private static final long MILLIS_PER_DAY = 86400000l;
    private static Properties props;

    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    /**
     * @param context Bundle Context
     * @return JwtProperties
     */
    public static Properties getJwtProperties(BundleContext context) {
        if (props == null) {
            ServiceReference<?> configurationAdminReference = context
                    .getServiceReference(ConfigurationAdmin.class.getName());

            if (configurationAdminReference != null) {
                ConfigurationAdmin confAdmin = (ConfigurationAdmin) context
                        .getService(configurationAdminReference);
                try {
                    Configuration configuration = confAdmin
                            .getConfiguration(HyperIoTConstants.HYPERIOT_JWT_CONFIG_FILE_NAME);
                    if (configuration != null && configuration.getProperties() != null) {
                        Dictionary<String, Object> dict = configuration.getProperties();
                        List<String> keys = Collections.list(dict.keys());
                        Map<String, Object> dictCopy = keys.stream()
                                .collect(Collectors.toMap(Function.identity(), dict::get));
                        props = new Properties();
                        props.putAll(dictCopy);
                        log.debug("Loaded properties from JWT Filter: {}", props);
                        return props;
                    }
                } catch (IOException e) {
                    log.error(
                            "Impossible to find {}", new Object[]{HyperIoTConstants.HYPERIOT_JWT_CONFIG_FILE_NAME, e});
                    return null;
                }
            }
            log.error(
                    "Impossible to find {}", new Object[]{HyperIoTConstants.HYPERIOT_JWT_CONFIG_FILE_NAME});
            return null;
        }
        return props;
    }

    /**
     * Returns true if user has at least one roles listed in the rolesNames array
     *
     * @param ctx
     * @param username
     * @param rolesNames
     * @return
     */
    public static boolean checkUserHasRoles(HyperIoTContext ctx, String username, String[] rolesNames) {
        HyperIoTPermissionManager pm = getPermissionManager(ctx);
        return pm.userHasRoles(username, rolesNames);
    }

    /**
     * @param context HyperIoTContext
     * @param o       resource
     * @param action  Action
     * @return true if Logged User has permission to do the action on the specified entity
     */
    public static boolean checkPermission(HyperIoTContext context, Object o,
                                          HyperIoTAction action) {
        HyperIoTPermissionManager pm = getPermissionManager(context);

        if ((pm == null && HyperIoTPermissionManager.isProtectedEntity(o))) {
            return false;
        } else if (pm == null) {
            return true;
        }

        HyperIoTResource entity = (HyperIoTResource) o;
        return pm.checkPermission(context.getLoggedUsername(), entity, action);
    }

    /**
     * @param context HyperIoTContext
     * @param action  Action
     * @return true if Logged User has permission to do the action generically
     * NOTE: this method do not check if the user can modify (owns) the entity, so it's generic permission
     * that should be used carefully
     */
    public static boolean checkPermission(HyperIoTContext context, String resourceName,
                                          HyperIoTAction action) {
        HyperIoTPermissionManager pm = getPermissionManager(context);

        if ((pm == null && HyperIoTPermissionManager.isProtectedEntity(resourceName))) {
            return false;
        } else if (pm == null) {
            return true;
        }

        return pm.checkPermission(context.getLoggedUsername(), resourceName, action);
    }

    /**
     * @param context      HyperIoTContext
     * @param resourceName Resource Name
     * @param action       Action
     * @param entities     List of entities that users must own in order allow the action
     * @return NOTE: this method checks wheter logged user can perform a generic action but at the same time
     * the user itself must own other resources.
     * For an example see @See AreaServiceImpl
     */
    public static boolean checkPermissionAndOwnership(HyperIoTContext context, String resourceName,
                                                      HyperIoTAction action, HyperIoTResource... entities) {
        HyperIoTPermissionManager pm = getPermissionManager(context);
        if ((pm == null && HyperIoTPermissionManager.isProtectedEntity(resourceName))) {
            return false;
        } else if (pm == null) {
            return true;
        }
        return pm.checkPermissionAndOwnership(context.getLoggedUsername(), resourceName, action, entities);
    }

    /**
     * @param context  HyperIoTContext
     * @param o        the entity on which action should be performed
     * @param action   Action
     * @param entities List of entities that users must own in order allow the action
     * @return NOTE: this method checks wheter logged user can perform a generic action but at the same time
     * the user itself must own other resources.
     * For an example see @See AreaServiceImpl
     */
    public static boolean checkPermissionAndOwnership(HyperIoTContext context, Object o,
                                                      HyperIoTAction action, HyperIoTResource... entities) {
        HyperIoTPermissionManager pm = getPermissionManager(context);
        if ((pm == null && HyperIoTPermissionManager.isProtectedEntity(o))) {
            return false;
        } else if (pm == null) {
            return true;
        }
        HyperIoTResource entity = (HyperIoTResource) o;
        return pm.checkPermissionAndOwnership(context.getLoggedUsername(), entity, action, entities);
    }

    /**
     * Return the permission manager
     *
     * @param ctx
     * @return
     */
    public static HyperIoTPermissionManager getPermissionManager(HyperIoTContext ctx) {
        Collection<ServiceReference<HyperIoTPermissionManager>> serviceReferences;
        String permissionFilter = OSGiFilterBuilder
                .createFilter(HyperIoTConstants.OSGI_PERMISSION_MANAGER_IMPLEMENTATION,
                        ctx.getPermissionImplementation())
                .getFilter();
        log.debug(
                "Searching for OSGi registered Permission with filter: {}", permissionFilter);
        try {
            serviceReferences = HyperIoTUtil.getBundleContext(ctx)
                    .getServiceReferences(HyperIoTPermissionManager.class, permissionFilter);
            if (serviceReferences.size() > 1) {
                log.debug("No OSGi action found for filter: {}", permissionFilter);
                throw new HyperIoTRuntimeException();
            }
            HyperIoTPermissionManager permissionManager = (HyperIoTPermissionManager) HyperIoTUtil
                    .getBundleContext(ctx).getService(serviceReferences.iterator().next());
            log.debug("OSGi permission manager found {}", permissionManager);
            return permissionManager;
        } catch (InvalidSyntaxException e) {
            log.error("Invalid OSGi Syntax", e);
            throw new HyperIoTRuntimeException();
        }
    }

    /**
     * Checks wheter the user owns the specified resource
     *
     * @param context
     * @param user
     * @param resource
     * @return
     */
    public static boolean checkUserOwnsResource(HyperIoTContext context, HyperIoTUser user, Object resource) {
        HyperIoTPermissionManager pm = getPermissionManager(context);
        if (pm != null) {
            return pm.checkUserOwnsResource(user, resource);
        }
        return false;
    }

    /**
     * @return
     */
    public static String getEncryptionAlgorithm() {
        return "SHA256withRSA";
    }

    /**
     * Generates KeyPair value with 2048 bytes
     *
     * @return
     */

    //Returning keystore save in it.acsoftware.hyperiot.jwt.config
    public static String getServerKeystoreFilePath() {
        return props.get("rs.security.keystore.file").toString();
    }

    //Returning keystore save in it.acsoftware.hyperiot.jwt.config
    public static String getServerKeystorePassword() {
        return props.get("rs.security.keystore.password").toString();
    }

    //Returning keystore save in it.acsoftware.hyperiot.jwt.config
    public static String getServerKeyPassword() {
        return props.get("rs.security.key.password").toString();
    }

    //Returning keystore save in it.acsoftware.hyperiot.jwt.config
    public static String getServerKeystoreAlias() {
        return props.get("rs.security.keystore.alias").toString();
    }

    /**
     * @param keySize
     * @return
     */
    public static KeyPair generateSSLKeyPairValue(int keySize) {
        try {
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
            final java.security.KeyPairGenerator rsaKeyPairGenerator =
                    java.security.KeyPairGenerator.getInstance("RSA");
            rsaKeyPairGenerator.initialize(keySize, random);
            final KeyPair rsaKeyPair = rsaKeyPairGenerator.generateKeyPair();
            return rsaKeyPair;
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * @param pair
     * @param subjectString
     * @return
     * @throws Exception
     */
    public static PKCS10CertificationRequest generateRequest(KeyPair pair, String subjectString) throws Exception {
        PKCS10CertificationRequestBuilder p10Builder = new JcaPKCS10CertificationRequestBuilder(
                new X500Principal("CN=" + subjectString), pair.getPublic());
        JcaContentSignerBuilder csBuilder = new JcaContentSignerBuilder("SHA256withRSA");
        ContentSigner signer = csBuilder.build(pair.getPrivate());
        PKCS10CertificationRequest csr = p10Builder.build(signer);
        return csr;
    }

    /**
     * @param issuerStr
     * @param subjectStr
     * @param validDays
     * @param keyPair
     * @param caCert
     * @return
     */
    public static X500PrivateCredential createServerClientX509Cert(String issuerStr, String subjectStr, int validDays, KeyPair keyPair, Certificate caCert) {
        try {
            PKCS10CertificationRequest request = generateRequest(keyPair, subjectStr);
            BigInteger serialNumber = BigInteger.valueOf(System.currentTimeMillis());
            PrivateKey privateKey = HyperIoTSecurityUtil.getServerKeyPair().getPrivate(); // The CA's private key
            Date issuedDate = new Date();
            Date expiryDate = new Date(System.currentTimeMillis() + validDays * MILLIS_PER_DAY); //MILLIS_PER_DAY=86400000l
            JcaPKCS10CertificationRequest jcaRequest = new JcaPKCS10CertificationRequest(request);
            X509v3CertificateBuilder certificateBuilder = new JcaX509v3CertificateBuilder((X509Certificate) caCert,
                    serialNumber, issuedDate, expiryDate, jcaRequest.getSubject(), jcaRequest.getPublicKey());
            JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();
            certificateBuilder.addExtension(Extension.authorityKeyIdentifier, false,
                            extUtils.createAuthorityKeyIdentifier(caCert.getPublicKey()))
                    .addExtension(Extension.subjectKeyIdentifier, false, extUtils.createSubjectKeyIdentifier(jcaRequest
                            .getPublicKey()))
                    .addExtension(Extension.basicConstraints, true, new BasicConstraints(0));
            ContentSigner signer = new JcaContentSignerBuilder("SHA1withRSA").setProvider("BC").build(privateKey);
            X509Certificate signedCert = new JcaX509CertificateConverter().setProvider("BC").getCertificate
                    (certificateBuilder.build(signer));
            return new X500PrivateCredential(signedCert, keyPair.getPrivate());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new HyperIoTRuntimeException("Error generating certificate", e);
        }

    }

    /**
     * @return
     * @throws PEMException
     */
    public static Certificate getServerRootCert() throws PEMException {
        try {
            KeyStore keystore = KeyStore.getInstance("JKS");
            keystore.load(new FileInputStream(new File(getServerKeystoreFilePath())), getServerKeystorePassword().toCharArray());
            return keystore.getCertificate(getServerKeystoreAlias());
        } catch (Exception e) {
            throw new PEMException("unable to convert key pair: " + e.getMessage(), e);
        }
    }

    /**
     * @return the key Pair associated with the current instance of this server
     */
    public static KeyPair getServerKeyPair() {
        try {
            KeyStore keystore = KeyStore.getInstance("JKS");
            keystore.load(new FileInputStream(new File(getServerKeystoreFilePath())), getServerKeystorePassword().toCharArray());
            String alias = getServerKeystoreAlias();
            Key key = keystore.getKey(alias, getServerKeyPassword().toCharArray());
            if (key instanceof PrivateKey) {
                // Get certificate of public key
                Certificate cert = keystore.getCertificate(alias);

                // Get public key
                PublicKey publicKey = cert.getPublicKey();

                // Return a key pair
                return new KeyPair(publicKey, (PrivateKey) key);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * @param publicKey current publick key
     * @return String rapresentation of the publick key
     */
    public static String getPublicKeyString(PublicKey publicKey) {
        try {
            StringWriter writer = new StringWriter();
            PemWriter pemWriter = new PemWriter(writer);
            pemWriter.writeObject(new PemObject("PUBLIC KEY", publicKey.getEncoded()));
            pemWriter.flush();
            pemWriter.close();
            return writer.toString();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * Accepts PKCS8 Keys only
     *
     * @param key
     * @return
     */
    public static PublicKey getPublicKeyFromString(String key) {
        try {
            String publicKeyPEM = key;
            publicKeyPEM = publicKeyPEM.replace("-----BEGIN PUBLIC KEY-----", "");
            publicKeyPEM = publicKeyPEM.replace("-----END PUBLIC KEY-----", "");
            publicKeyPEM = publicKeyPEM.replace("\r", "");
            publicKeyPEM = publicKeyPEM.replace("\n", "");
            byte[] byteKey = Base64.getDecoder().decode(publicKeyPEM.getBytes("UTF8"));
            X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(byteKey);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(X509publicKey);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Accepts PKCS8 Keys only
     *
     * @param key
     * @return
     */
    public static PrivateKey getPrivateKeyFromString(String key) {
        try {
            String privateKeyPEM = key;
            privateKeyPEM = privateKeyPEM.replace("-----BEGIN PRIVATE KEY-----", "");
            privateKeyPEM = privateKeyPEM.replace("-----END PRIVATE KEY-----", "");
            privateKeyPEM = privateKeyPEM.replace("\r", "");
            privateKeyPEM = privateKeyPEM.replace("\n", "");
            byte[] byteKey = Base64.getDecoder().decode(privateKeyPEM.getBytes("UTF8"));
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(byteKey);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(keySpec);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * @param padding
     * @return
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     */
    public static Cipher getCipherRSA(String padding) throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException {

        if (padding == null) {
            return Cipher.getInstance("RSA", "BC");
        } else {
            return Cipher.getInstance("RSA/NONE/" + padding, "BC");
        }
    }

    /**
     * @param padding
     * @return
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     */
    public static Cipher getCipherRSAECB(String padding) throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException {

        if (padding == null) {
            return Cipher.getInstance("RSA", "BC");
        } else {
            return Cipher.getInstance("RSA/ECB/" + padding, "BC");
        }
    }

    /**
     * @param ecb
     * @return
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     */
    public static Cipher getCipherRSAPKCS1Padding(boolean ecb) throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException {
        if (ecb)
            return getCipherRSAECB("PKCS1PADDING");
        return getCipherRSA("PKCS1PADDING");
    }

    /**
     * @return
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     */
    public static Cipher getCipherRSAOAEPPAdding() throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException {
        return getCipherRSA("OAEPPadding");
    }

    /**
     * @return Default cipher CBC/PKCS5PADDING
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     */
    public static Cipher getCipherAES() throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException {
        return getCipherAES("PKCS5PADDING");
    }

    /**
     * @param padding
     * @return
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     */
    public static Cipher getCipherAES(String padding) throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException {
        return Cipher.getInstance("AES/CBC/" + padding, "BC");
    }

    /**
     * @param plainTextMessage
     * @param publicKeyBytes
     * @return
     */
    public static byte[] encodeMessageWithPublicKey(byte[] plainTextMessage, byte[] publicKeyBytes) {
        try {
            PublicKey pk = getPublicKeyFromString(new String(publicKeyBytes));
            return encryptText(pk, plainTextMessage, true, getCipherRSA(null));
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        }
        return null;
    }

    /**
     * @param plainTextMessage
     * @param privateKeyBytes
     * @return
     */
    public static byte[] encodeMessageWithPrivateKey(byte[] plainTextMessage, byte[] privateKeyBytes) {
        try {
            PrivateKey pk = getPrivateKeyFromString(new String(privateKeyBytes));
            return encryptText(pk, plainTextMessage, true, getCipherRSA(null));
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        }
        return null;
    }

    /**
     * @param cipherText     Encrypted Text
     * @param publicKeyBytes Public Key encoded bytes
     * @return decoded String message
     */
    public static byte[] decodeMessageWithPublicKey(byte[] cipherText, byte[] publicKeyBytes) {
        try {
            // asume, that publicKeyBytes contains a byte array representing
            // your public key
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            Cipher asymmetricCipher = getCipherRSA(null);
            KeyFactory keyFactory;
            keyFactory = KeyFactory.getInstance(publicKeySpec.getFormat());
            Key key = keyFactory.generatePublic(publicKeySpec);
            // initialize your cipher
            asymmetricCipher.init(Cipher.DECRYPT_MODE, key);
            // asuming, cipherText is a byte array containing your encrypted message
            byte[] plainText = asymmetricCipher.doFinal(cipherText);
            return plainText;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * @param cipherText Encrypted Text
     * @return Decoded String message using the current private key loaded form the keystore
     */
    public static byte[] decodeMessageWithServerPrivateKey(byte[] cipherText, Cipher asymmetricCipher) {
        return decodeMessageWithPrivateKey(HyperIoTSecurityUtil.getServerKeyPair().getPrivate(), cipherText, asymmetricCipher);
    }

    /**
     * @param cipherText Encrypted Text
     * @return Decoded String message using the current private key loaded form the keystore
     */
    public static byte[] decodeMessageWithPrivateKey(PrivateKey key, byte[] cipherText, Cipher asymmetricCipher) {
        try {
            // initialize your cipher
            asymmetricCipher.init(Cipher.DECRYPT_MODE, key);
            // asuming, cipherText is a byte array containing your encrypted message
            byte[] plainText = asymmetricCipher.doFinal(cipherText);
            return plainText;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Create a signature of the input string with the server private certificate.
     *
     * @param data         data to be signed
     * @param encodeBase64 true if you want the result be encoded in base64
     * @return
     */
    public static byte[] signDataWithServerCert(byte[] data, boolean encodeBase64) {
        try {
            Signature sig = Signature.getInstance("SHA1WithRSA");
            sig.initSign(getServerKeyPair().getPrivate());
            sig.update(data);
            byte[] signatureBytes = sig.sign();
            if (encodeBase64)
                return Base64.getEncoder().encode(signatureBytes);
            return signatureBytes;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Verifies signed data with server cert
     *
     * @param inputData
     * @param signedData
     * @param decodeSignedDataFromBase64
     * @return
     */
    public static boolean verifyDataSignedWithServerCert(byte[] inputData, byte[] signedData, boolean decodeSignedDataFromBase64) {
        try {
            Signature sig = Signature.getInstance("SHA1WithRSA");
            sig.initVerify(getServerKeyPair().getPublic());
            sig.update(inputData);
            byte[] signatureBytes = (decodeSignedDataFromBase64) ? Base64.getDecoder().decode(signedData) : signedData;
            return sig.verify(signatureBytes);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * @param plainTextMessage plain text message
     * @param cipherText       encrypted challenge text
     * @param publicKeyBytes   public key
     * @return true if plain text message and decrypted cipherText are equal
     */
    public static boolean checkChallengeMessage(String plainTextMessage, String cipherText, byte[] publicKeyBytes) {
        String decodedCipherText = new String(decodeMessageWithPublicKey(Base64.getDecoder().decode(cipherText.getBytes()), publicKeyBytes));
        return decodedCipherText != null && decodedCipherText.equals(plainTextMessage);
    }

    /**
     * @param pk   Private Key
     * @param text String to encrypt
     * @return encrypted text
     */
    public static byte[] encryptText(PrivateKey pk, byte[] text, boolean encodeInBase64, Cipher asymmetricCipher) {
        try {
            asymmetricCipher.init(Cipher.ENCRYPT_MODE, pk);
            if (encodeInBase64) {
                return Base64.getEncoder().encode(asymmetricCipher.doFinal(text));
            }
            return asymmetricCipher.doFinal(text);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * @param pk   Publick key
     * @param text String to encrypt
     * @return encrypted text
     */
    public static byte[] encryptText(PublicKey pk, byte[] text, boolean encodeInBase64, Cipher asymmetricCipher) {
        try {
            asymmetricCipher.init(Cipher.ENCRYPT_MODE, pk);
            if (encodeInBase64) {
                return Base64.getEncoder().encode(asymmetricCipher.doFinal(text));
            }
            return asymmetricCipher.doFinal(text);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * @return Random 32 byte length password
     */
    public static byte[] generateRandomAESPassword() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        SecureRandom secureRandom = new SecureRandom();
        int keyBitSize = 256;
        keyGen.init(keyBitSize, secureRandom);
        return keyGen.generateKey().getEncoded();
    }

    /**
     * @return Random byte init vector
     */
    public static byte[] generateRandomAESInitVector() throws NoSuchAlgorithmException {
        try {
            Cipher c = getCipherAES();
            SecureRandom randomSecureRandom = new SecureRandom();
            byte[] iv = new byte[c.getBlockSize()];
            randomSecureRandom.nextBytes(iv);
            IvParameterSpec ivParams = new IvParameterSpec(iv);
            return iv;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return new byte[16];
    }

    /**
     * @param saltBytesSize
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static byte[] generateRandomAESSalt(int saltBytesSize) throws NoSuchAlgorithmException {
        Random r = new SecureRandom();
        byte[] salt = new byte[saltBytesSize];
        r.nextBytes(salt);
        return salt;
    }

    /**
     * Generates AES key from a basic password
     *
     * @param password
     * @param salt
     * @param hashMethod
     * @return
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     */
    public static byte[] getAESKeyFromPassword(String password, String hashMethod, byte[] salt, int numIterations, int keyBitSize) throws InvalidKeySpecException, NoSuchAlgorithmException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance(hashMethod);
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, numIterations, keyBitSize);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
        return secret.getEncoded();
    }


    /**
     * @param aesPassword secret aes key
     * @param content     Content to encrypt
     * @return Encrypted text as a String
     * @throws InvalidKeyException          Invalid key exception
     * @throws UnsupportedEncodingException Unsupported encoding
     * @throws NoSuchPaddingException       No Such padding
     * @throws NoSuchAlgorithmException     No Such Algotithm
     */
    public static byte[] encryptWithAES(byte[] aesPassword, byte[] initVector, String content, Cipher aesCipher) throws InvalidKeyException, UnsupportedEncodingException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector);
            SecretKeySpec skeySpec = new SecretKeySpec(aesPassword, "AES");
            aesCipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            byte[] encrypted = aesCipher.doFinal(content.getBytes("UTF8"));
            return Base64.getEncoder().encode(encrypted);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        return null;
    }

    /**
     * @param aesPassword secret aes key
     * @param content     Content to encrypt
     * @return Encrypted text as a String encoded in base 64 ivBytes+encryptedBytes
     * @throws InvalidKeyException          Invalid key exception
     * @throws UnsupportedEncodingException Unsupported encoding
     * @throws NoSuchPaddingException       No Such padding
     * @throws NoSuchAlgorithmException     No Such Algotithm
     */
    public static byte[] encryptWithAESAndCipherData(byte[] aesPassword, byte[] initVector, String content, Cipher aesCipher) throws InvalidKeyException, UnsupportedEncodingException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector);
            SecretKeySpec skeySpec = new SecretKeySpec(aesPassword, "AES");
            aesCipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            byte[] encrypted = aesCipher.doFinal(content.getBytes("UTF8"));
            byte[] toEncode = new byte[initVector.length + encrypted.length];
            System.arraycopy(initVector, 0, toEncode, 0, initVector.length);
            System.arraycopy(encrypted, 0, toEncode, initVector.length, encrypted.length);
            return Base64.getEncoder().encode(toEncode);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        return null;
    }

    /**
     * @param aesPassword secret aes key
     * @param content     Content to encrypt
     * @return Encrypted text as a String encoded in base 64 ivBytes+saltBytes+encryptedBytes
     * @throws InvalidKeyException          Invalid key exception
     * @throws UnsupportedEncodingException Unsupported encoding
     * @throws NoSuchPaddingException       No Such padding
     * @throws NoSuchAlgorithmException     No Such Algotithm
     */
    public static byte[] encryptWithAESAndCipherData(byte[] aesPassword, byte[] initVector, byte[] salt, String content, Cipher aesCipher) throws InvalidKeyException, UnsupportedEncodingException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector);
            SecretKeySpec skeySpec = new SecretKeySpec(aesPassword, "AES");
            aesCipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            byte[] encrypted = aesCipher.doFinal(content.getBytes("UTF8"));
            byte[] toEncode = new byte[initVector.length + salt.length + encrypted.length];
            System.arraycopy(initVector, 0, toEncode, 0, initVector.length);
            System.arraycopy(salt, 0, toEncode, initVector.length, salt.length);
            System.arraycopy(encrypted, 0, toEncode, (initVector.length + salt.length), encrypted.length);
            return Base64.getEncoder().encode(toEncode);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        return null;
    }

    /**
     * @param aesPassword secret aes key
     * @param content     Content to decrypt
     * @return Decrypted text as a String
     * @throws InvalidKeyException      Invalid key exception
     * @throws NoSuchPaddingException   No Such padding
     * @throws NoSuchAlgorithmException No Such Algotithm
     */
    public static byte[] decryptWithAES(byte[] aesPassword, byte[] initVector, String content, Cipher aesCipher) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException, InvalidAlgorithmParameterException {
        SecretKeySpec skeySpec = new SecretKeySpec(aesPassword, "AES");
        IvParameterSpec iv = new IvParameterSpec(initVector);
        aesCipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
        return aesCipher.doFinal(Base64.getDecoder().decode(content));
    }
}


