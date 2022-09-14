package it.acsoftware.hyperiot.websocket.encryption.mode;

import it.acsoftware.hyperiot.base.security.util.HyperIoTSecurityUtil;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HyperIoTRSAWithAESEncryptionMode extends HyperIoTWebSocketMixedEncryptionMode {
    private static Logger log = LoggerFactory.getLogger(HyperIoTRSAWithAESEncryptionMode.class.getName());

    public static final String MODE_PARAM_AES_PASSWORD = "aesPassword";
    public static final String MODE_PARAM_AES_IV = "aesIv";

    public static Cipher currRsaCipherEnc;
    public static Cipher currRsaCipherEncWeb;
    public static Cipher currRsaCipherDec;
    public static Cipher currRsaCipherDecWeb;

    static {
        try {
            PrivateKey key = HyperIoTSecurityUtil.getServerKeyPair().getPrivate();
            currRsaCipherEnc = HyperIoTSecurityUtil.getCipherRSAOAEPPAdding();
            currRsaCipherEncWeb = HyperIoTSecurityUtil.getCipherRSAPKCS1Padding(true);
            currRsaCipherDec = HyperIoTSecurityUtil.getCipherRSAOAEPPAdding();
            currRsaCipherDec.init(Cipher.DECRYPT_MODE, key);
            currRsaCipherDecWeb = HyperIoTSecurityUtil.getCipherRSAPKCS1Padding(true);
            currRsaCipherDecWeb.init(Cipher.DECRYPT_MODE, key);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private boolean webSession;
    private Cipher currAesCipherEnc;
    private Cipher currAesCipherDec;

    @Override
    public void init(Session session) {
        try {
            String clientPubKeyStrEnc = session.getUpgradeRequest().getHeader(("X-HYPERIOT-CLIENT-PUB-KEY"));
            //Using OAEP Padding
            Cipher currCipherRSADec = null;
            if (clientPubKeyStrEnc == null) {
                //trying with query param since javascript API doesn't support custom headers in websocket
                List<String> pubKeyParam = session.getUpgradeRequest().getParameterMap().get("hyperiot-client-pub-key");
                if (pubKeyParam.size() == 1) {
                    clientPubKeyStrEnc = pubKeyParam.get(0);
                }
                this.webSession = true;
            } else {
                this.webSession = false;
            }
            if (clientPubKeyStrEnc != null) {
                this.setPrivateKey(HyperIoTSecurityUtil.getServerKeyPair().getPrivate());
                byte[] decodedPubKey = Base64.getDecoder().decode(clientPubKeyStrEnc.getBytes("UTF8"));
                byte[] decryptedPubKey = decryptAsymmetric(HyperIoTSecurityUtil.getServerKeyPair().getPrivate(), decodedPubKey);
                String clientPubKeyStr = new String(decryptedPubKey);
                PublicKey clientPubKey = HyperIoTSecurityUtil.getPublicKeyFromString(clientPubKeyStr);
                this.setPublicKey(clientPubKey);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void dispose(Session s) {
        //do nothing
    }

    @Override
    protected byte[] encryptSymmetric(byte[] symmetricPassword, byte[] symmetricIv, String s) throws Exception {
        return HyperIoTSecurityUtil.encryptWithAES(symmetricPassword, symmetricIv, s, getCipherAESEnc());
    }

    @Override
    protected byte[] encryptAsymmetric(PublicKey publicKey, byte[] plainText, boolean encodeBase64) throws Exception {
        Cipher cipher = getCipherRSAEnc();
        synchronized (cipher) {
            return HyperIoTSecurityUtil.encryptText(publicKey, plainText, encodeBase64, cipher);
        }
    }

    @Override
    protected byte[] decryptSymmetric(byte[] symmetricPassword, byte[] symmetricIv, String s) throws Exception {
        return HyperIoTSecurityUtil.decryptWithAES(symmetricPassword, symmetricIv, s, getCipherAESDec());
    }

    @Override
    protected byte[] decryptAsymmetric(PrivateKey privateKey, byte[] plainText) throws Exception {
        Cipher cipher = getCipherRSADec();
        byte[] decrypted = null;
        synchronized (cipher) {
            decrypted = cipher.doFinal(plainText);
        }
        return decrypted;
    }

    @Override
    public void update(Map<String, Object> params) {
        byte[] aesPassword = (byte[]) params.get(MODE_PARAM_AES_PASSWORD);
        byte[] iv = (byte[]) params.get(MODE_PARAM_AES_IV);
        this.setSymmetricPassword(aesPassword);
        this.setSymmetricIv(iv);
    }

    @Override
    public Map<String, Object> getParams() {
        HashMap<String, Object> params = new HashMap<>();
        params.put(MODE_PARAM_AES_PASSWORD, this.getSymmetricPassword());
        params.put(MODE_PARAM_AES_IV, this.getSymmetricIv());
        return params;
    }

    private Cipher getCipherRSADec() {
        try {
            if (this.webSession)
                return currRsaCipherDecWeb;
            return currRsaCipherDec;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    private Cipher getCipherRSAEnc() {
        try {
            if (this.webSession)
                return currRsaCipherEncWeb;
            return currRsaCipherEnc;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    private Cipher getCipherAESEnc() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchProviderException {
        if (this.currAesCipherEnc == null) {
            this.currAesCipherEnc = HyperIoTSecurityUtil.getCipherAES();
            SecretKeySpec skeySpec = new SecretKeySpec(this.getSymmetricPassword(), "AES");
            IvParameterSpec iv = null;
            if (this.getSymmetricIv() != null)
                iv = new IvParameterSpec(this.getSymmetricIv());

            if (iv != null)
                this.currAesCipherEnc.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            else
                this.currAesCipherEnc.init(Cipher.ENCRYPT_MODE, skeySpec);
        }
        return currAesCipherEnc;
    }


    private Cipher getCipherAESDec() throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException {
        if (this.currAesCipherDec == null) {
            this.currAesCipherDec = HyperIoTSecurityUtil.getCipherAES();
            SecretKeySpec skeySpec = new SecretKeySpec(this.getSymmetricPassword(), "AES");
            IvParameterSpec iv = null;
            if (this.getSymmetricIv() != null)
                iv = new IvParameterSpec(this.getSymmetricIv());

            if (iv != null)
                this.currAesCipherDec.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            else
                this.currAesCipherDec.init(Cipher.DECRYPT_MODE, skeySpec);
        }
        return currAesCipherDec;
    }

}
