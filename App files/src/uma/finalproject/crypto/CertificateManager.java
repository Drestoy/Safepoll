/**
 * @author Daniel Domínguez Restoy
 * @version 1.0
 */
package uma.finalproject.crypto;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.security.KeyChain;
import android.security.KeyChainAliasCallback;
import android.security.KeyChainException;
import android.util.Base64;

public class CertificateManager {
	
    private String KEYCHAIN_PREF_FILE = "keychainFile";
    private static final String KEYCHAIN_PREF_ALIAS = "alias";
    private static final String KEYCHAIN_PREF_DATA = "CertificateSubjectDN";
    private static final String KEYCHAIN_PREF_CHAINED = "isChained";
    private static final String PREF_ERROR = "Error";
	
	private Activity act;
	
	private Context ctx;
	
	/**
	 * Constructor para la clase CertificateManager.
	 * @param a La Activity que realiza la llamada.
	 * @param user	El nombre del fichero donde guardar la información.
	 */
	public CertificateManager(Activity a, String user){
		act = a;
		ctx = a;
		KEYCHAIN_PREF_FILE = user;
	}
	
	/**
	 * Muestra un menú emergente con todos los certificados instalados en el dispositivo.
	 */
	public void selectCertificate(){
		KeyChain.choosePrivateKeyAlias(act, (KeyChainAliasCallback)act, new String[] { "RSA" }, null, null, -1, null);
	}
	
	/**
	 * Cambia el nombre del fichero sobre el que trabaja CertificateManager.
	 * @param user	El nombre del fichero donde guardar la información.
	 */
	public void setUser(String user){
		KEYCHAIN_PREF_FILE = user;
	}
	
	/**
	 * Recoge el alias guardado en archivo.
	 * @return El alias del certificado guardado.
	 */
	public String getAlias() {
        SharedPreferences pref = ctx.getSharedPreferences(KEYCHAIN_PREF_FILE, 0);
        return pref.getString(KEYCHAIN_PREF_ALIAS, PREF_ERROR);
    }

    /**
     * Guarda el alias de la cadena de certificados en un fichero.
     * @param alias El nombre del fichero donde guardar la información.
     */
    public void saveAlias(String alias) {
        SharedPreferences pref = ctx.getSharedPreferences(KEYCHAIN_PREF_FILE, 0);
        Editor editor = pref.edit();
        editor.putString(KEYCHAIN_PREF_ALIAS, alias);
        editor.commit();
    }
    
    /**
     * Recoge el SubjectDN guardado en fichero.
     * @return El SubjectDN del certificado guardado.
     */
    public String getCertData(){
    	SharedPreferences pref = ctx.getSharedPreferences(KEYCHAIN_PREF_FILE, 0);
        return pref.getString(KEYCHAIN_PREF_DATA, PREF_ERROR);
    }
    
    /**
     * Guarda el SubjectDN del certificado en fichero.
     * @param subjectDN	El SubjectDN del certificado.
     */
    public void saveCertData(String subjectDN) {
        SharedPreferences pref = ctx.getSharedPreferences(KEYCHAIN_PREF_FILE, 0);
        Editor editor = pref.edit();
        editor.putString(KEYCHAIN_PREF_DATA, subjectDN);
        editor.commit();
    }
    
    /**
     * Comprueba si el certificado está enlazado a la cuenta del usuario.
     * @return	true en caso de estar enlazado, false en cualquier otro caso.
     */
    @SuppressLint("SdCardPath") 
    public boolean isChained(){
    	File f = new File("/data/data/uma.finalproject.safepoll/shared_prefs/" + KEYCHAIN_PREF_FILE + ".xml");;
    	if (f.exists()){
    		SharedPreferences pref = ctx.getSharedPreferences(KEYCHAIN_PREF_FILE, 0);
            return pref.getBoolean(KEYCHAIN_PREF_CHAINED, false);
    	}else{
    		return false;
    	}
    	
    }
    
    /**
     * Guarda en fichero el estado de enlazado a cuenta del certificado.
     * @return true si la operación se realizó con éxito, false en caso contrario.
     */
    public boolean setChained(){
    	if(checkCertificate()){
    		SharedPreferences pref = ctx.getSharedPreferences(KEYCHAIN_PREF_FILE, 0);
        	Editor editor = pref.edit();
            editor.putBoolean(KEYCHAIN_PREF_CHAINED, true);
            editor.commit();
            return true;
    	}else{
    		return false;
    	}
    	
        
    }
    
    /**
     * Devuelve la fecha de caducidad que el certificado tiene guardada.
     * @return La fecha de caducidad del certificado.
     */
    @SuppressLint("SimpleDateFormat") 
    public String getExpirationDate(){
    	X509Certificate[] certs = getCertificateChain(getAlias());
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    	return sdf.format(certs[0].getNotAfter());
    }
	
    /**
     * Comprueba la validez de la cadena de certificados.
     * @return true si la cadena de certificados es válida, false en cualquier otro caso.
     */
    public boolean checkCertificate(){
    	//Comprobación de la validez de la cadena de certificados
    	X509Certificate[] certs = getCertificateChain(getAlias());
    	try {
			certs[0].checkValidity();
		} catch (CertificateExpiredException e) {
			return false;
		} catch (CertificateNotYetValidException e) {
			return false;
		}
    	
        TrustManagerFactory tmf;
		try {
			tmf = TrustManagerFactory.getInstance("X509");
			tmf.init((KeyStore) null);
			TrustManager[] tms = tmf.getTrustManagers();
	        X509TrustManager xtm = (X509TrustManager) tms[0];
			
	        xtm.checkClientTrusted(certs, "RSA");
		} catch (NoSuchAlgorithmException e1) {
			return false;
		} catch (KeyStoreException e) {
			return false;
		} catch (CertificateException e) {
			return false;
		}
    	return true;
    }
    
    /**
     * Firma el texto aportado.
     * @param text El texto a firmar.
     * @return La firma en formato String.
     */
	public String sign(String text){
		byte[] signature = null;
		
		try {
	        if(checkCertificate()){
	        	byte[] aFirmar = text.getBytes("ISO-8859-1");
		        Signature dsa = Signature.getInstance("SHA1withRSA");
		        dsa.initSign(getPrivateKey(getAlias()));
		        
		        dsa.update(aFirmar);
				signature = dsa.sign();
	        }else{
	        	return "";
	        }
			
			} catch (SignatureException e) {
				return "";
			} catch (NoSuchAlgorithmException e) {
				return "";
			} catch (UnsupportedEncodingException e) {
				return "";
			} catch (InvalidKeyException e) {
				return "";
			}
		
		return byteToString(signature);
	}
	
	/**
	 * Verifica si una firma es correcta.
	 * @param text El texto a comparar con la firma.
	 * @param pk La clave pública del certificado usado para la firma.
	 * @param signature La firma que se desea verificar.
	 * @return true en caso de ser una firma válida, false en cualquier otro caso.
	 */
	public boolean verify(String text, PublicKey pk, String signature){
        byte[] aFirmar;
        boolean res = false;
		try {
			aFirmar = text.getBytes("ISO-8859-1");
			Signature dsa = Signature.getInstance("SHA1withRSA");
			dsa.initVerify(pk);
			dsa.update(aFirmar);
			res = dsa.verify(stringToByte(signature));
		} catch (UnsupportedEncodingException e) {
			return false;
		} catch (NoSuchAlgorithmException e) {
			return false;
		} catch (InvalidKeyException e) {
			return false;
		} catch (SignatureException e) {
			return false;
		}
        
		
		return res;
	}
	
	/**
	 * Recupera la cadena de certificados del alias guardado en fichero.
	 * @return La cadena de certificados.
	 */
	public X509Certificate[] getCertificateChain() {
        try {
            return KeyChain.getCertificateChain(ctx, this.getAlias());
        } catch (KeyChainException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
	
	/**
	 * Recupera la cadena de certificados del alias especificado.
	 * @param alias El alias del que se desea obtener la cadena de certificados.
	 * @return La cadena de certificados.
	 */
	public X509Certificate[] getCertificateChain(String alias) {
        try {
            return KeyChain.getCertificateChain(ctx, alias);
        } catch (KeyChainException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
	
	/**
	 * Recupera la clave pública del alias especificado.
	 * @param alias El alias del certificado del cual se desea obtener la clave pública.
	 * @return La clave pública del certificado.
	 */
	public PublicKey getPublicKey(String alias){
		X509Certificate[] certs = getCertificateChain(alias);
		return certs[0].getPublicKey();
	}
	
	/**
	 * Recupera la clave privada del alias especificado.
	 * @param alias	El alias del certificado del cual se desea obtener la clave privada.
	 * @return	La clave pública del certificado.
	 */
	public PrivateKey getPrivateKey(String alias) {
        try {
            return KeyChain.getPrivateKey(ctx, alias);
        } catch (KeyChainException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
	
	/**
	 * Pasa el byte especificado a cadena de caracteres.
	 * @param b La cadena de bytes para pasarla a cadena de caracteres.
	 * @return La cadena de caracteres obtenida.
	 */
	public String byteToString(byte[] b){
		return new String(Base64.encode(b,Base64.DEFAULT));
	}
	
	/**
	 * Pasa la cadena de caracteres a cadena de bytes.
	 * @param str La cadena de caracteres para pasarla a cadena de bytes.
	 * @return La cadena de bytes obtenida.
	 */
	public byte[] stringToByte(String str){
		return Base64.decode(str, Base64.DEFAULT);
	}
	
	
}


