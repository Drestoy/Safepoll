/**
 * @author Daniel Domínguez Restoy
 * @version 1.0
 */
package uma.finalproject.database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import uma.finalproject.auth.AccountGeneral;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

public class JSONParser {
    static InputStream is = null;
    static JSONObject jObj = null;
    static String json = "";
    private Context ctx;
    private String user, refTok;
    private boolean error = false;
    // constructor
    public JSONParser() {
    }
    public JSONObject getJSONFromUrl(String url, List<BasicNameValuePair> params, String mode, Context ctx ) {
        
    	this.ctx = ctx;
  
    	if(mode=="Login"){
    		String user = params.get(1).getValue();
        	String pass = params.get(2).getValue();
        	getAuth(url,user);
        	readJson("");
        	getToken(url,"authorization_code",user,pass);
        	readJson("");
        	try {
        		if(jObj!=null){
        			String code = jObj.getString("refresh_token");
        			getToken(url,"refresh_token",user, code); 
        			readJson("");
        			getResource(url,params);
        			readJson(code);
        		}
    		} catch (JSONException e) {
    			e.printStackTrace();
    		}
    	}else if(mode=="register"){
    		register(url, params);
        	readJson("");
    	}else if(mode =="resetPass"){
    		register(url, params);
    		readJson("");
    	}else{
    		user = params.get(1).getValue();
        	refTok = params.get(2).getValue();
    		getToken(url,"refresh_token",user, refTok);
    		readJson("");
    		getResource(url,params);
    		readJson("");
    	}
    	
        return jObj;
    }
    
    private void getToken(String url, String grantType, String username, String password){  
    	try {
    		
    			UsernamePasswordCredentials creds = null;
    			
    			List<BasicNameValuePair> params2 = new ArrayList<BasicNameValuePair>();
            	
                params2.add(new BasicNameValuePair("grant_type",grantType));
            	if(grantType == "refresh_token"){
            		params2.add(new BasicNameValuePair("refresh_token", password));
            		creds = new UsernamePasswordCredentials(username, "");
            	}else if(grantType == "authorization_code"){
            		if(jObj!=null){
            		String authCode = jObj.getString("code");
            		params2.add(new BasicNameValuePair("code", authCode));
            		creds = new UsernamePasswordCredentials(username, password);
            		}
            	}else{
            		creds = new UsernamePasswordCredentials(username, password);
            	}
            	DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(url + "Oauth2Serv/token.php");
                
                httpPost.setEntity(new UrlEncodedFormEntity(params2));
                
                try {
    				httpPost.addHeader(new BasicScheme().authenticate(creds, httpPost));
    			} catch (AuthenticationException e) {
    				e.printStackTrace();
    			}
                
                HttpResponse httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity(); 
            	
            	
                is = httpEntity.getContent();
    		
			
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
			e.printStackTrace();
		}
    }
  
    private void getResource(String url, List<BasicNameValuePair> params){
    	try {
    		if(jObj!=null){
    			String tok = jObj.getString("access_token");
    			
                params.add(new BasicNameValuePair("access_token", tok));
                DefaultHttpClient httpClient = new DefaultHttpClient();
                
                HttpPost httpPost = new HttpPost(url + "Oauth2Serv/resource.php");
                httpPost.setEntity(new UrlEncodedFormEntity(params));
                HttpResponse httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity(); 
            	
                is = httpEntity.getContent();
    		}
			
            
		} catch (JSONException e) {
			try {
				if((jObj.getString("error").equals("invalid_grant"))&&(!error)){
					error = true;
					
					AccountManager am = AccountManager.get(ctx);
					Account[] ac = am.getAccountsByType("com.safepoll");
					
					String user = ac[0].name;
		        	String pass = am.getPassword(ac[0]);
		        	getAuth(url,user);
		        	readJson("");
		        	getToken(url,"authorization_code",user,pass);
		        	readJson("");
		        	try {
		    			String code = jObj.getString("refresh_token");
		    			am.setAuthToken(ac[0],AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, code);
		    			getToken(url,"refresh_token",user, code); 
		    			readJson("");
		    			getResource(url,params);
		    			readJson(code);
		    		} catch (JSONException e2) {
		    			e.printStackTrace();
		    		}
				}
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    private void getAuth(String url, String username){
    	try {
			
			List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
        	
            params.add(new BasicNameValuePair("response_type", "code"));
            params.add(new BasicNameValuePair("client_id", username));
            params.add(new BasicNameValuePair("state", "xyz"));
           
            DefaultHttpClient httpClient = new DefaultHttpClient();
            
            HttpPost httpPost = new HttpPost(url + "Oauth2Serv/authorize.php");
            httpPost.setEntity(new UrlEncodedFormEntity(params));
            
            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity(); 
        	
            is = httpEntity.getContent();
            
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    private void register(String url, List<BasicNameValuePair> params){
    	try {
			DefaultHttpClient httpClient = new DefaultHttpClient();
            
            HttpPost httpPost = new HttpPost(url + "Oauth2Serv/newuser.php");
            httpPost.setEntity(new UrlEncodedFormEntity(params));
            
            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity(); 
        	
            is = httpEntity.getContent();
            
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    private void readJson(String code){
    		try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        is, "iso-8859-1"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "n");
                }
                is.close();
                json = sb.toString();
                if(code!=""){
                	json = json.substring(0, json.length()-2) + ",\"token\":\"" + code + "\"}n";
                }
            } catch (Exception e) {
            	e.printStackTrace();
            }
    		
            try {
                jObj = new JSONObject(json);
            } catch (JSONException e) {
            	e.printStackTrace();
            }
    	
    }
}
