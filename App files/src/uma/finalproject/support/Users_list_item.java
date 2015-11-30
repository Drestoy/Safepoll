/**
 * @author Daniel Domínguez Restoy
 * @version 1.0
 */
package uma.finalproject.support;

public class Users_list_item {
	private String username;
	private String picture;
	private String bbdd_ID;
	private String group_ID;
	private String account;
	private boolean useCert;
	private String certificate;
	
	public Users_list_item(String name, String pic, String id, String guid, String mail, boolean cert, String certificate){
		username = name;
		picture = pic;
		bbdd_ID = id;
		account = mail;
		useCert = cert;
		group_ID = guid;
		this.certificate = certificate;
	}
	
	public String getName(){
		return username;
	}
	
	public String getImage(){
		return picture;
	}
	
	public String getID(){
		return bbdd_ID;
	}
	
	public String getAccount(){
		return account;
	}
	
	public String getGUID(){
		return group_ID;
	}
	
	public boolean hasCertificate(){
		return useCert;
	}
	
	public String getCertificate(){
		return certificate;
	}
}
