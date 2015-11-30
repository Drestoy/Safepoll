/**
 * @author Daniel Domínguez Restoy
 * @version 1.0
 */
package uma.finalproject.database;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import uma.finalproject.auth.AccountGeneral;
import android.net.NetworkInfo;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class ConnectionManager {
	private static String KEY_SUCCESS = "success";
    private static String KEY_UID = "uid";
    private static String KEY_TOKEN = "token";
    private static String KEY_ERROR = "error";
    
    
    private List<BasicNameValuePair> params;
    private Context ctx;
    private String user, pass, mail,lastname;
    private String mode = "";
    
    private boolean error = false;
    
    private AccountManager am;
    private Account[] ac;
    private JSONObject result;
    
    public ConnectionManager( Context c, String u, String p, String md){
    	
    	user = u;
    	pass = p;
    	ctx = c;
    	mode = md;
    	am = AccountManager.get(c);
    	ac = am.getAccountsByType("com.safepoll");
    }
    
    public ConnectionManager( Context c, List<BasicNameValuePair> paramslist, String md){
    	
    	user = paramslist.get(0).getValue();
    	lastname = paramslist.get(1).getValue();
    	mail = paramslist.get(2).getValue();
    	if(paramslist.size()>3){
    		pass = paramslist.get(3).getValue();
    	}
    	params = paramslist;
    	ctx = c;
    	mode = md;
    	am = AccountManager.get(c);
    	ac = am.getAccountsByType("com.safepoll");
    }
    
  
	    private class NetCheck extends AsyncTask<String, Void, Boolean>
	    {
	    	private ProgressDialog nDialog;
	    	private OnTaskCompleted listener;
	    	
	    	private NetCheck(OnTaskCompleted listener){
	    		this.listener=listener;
	    	}
	    	
	        @Override
	        protected void onPreExecute(){
	            super.onPreExecute();
	            nDialog = new ProgressDialog(ctx);
	            nDialog.setTitle("Checking Network");
	            nDialog.setMessage("Loading..");
	            nDialog.setIndeterminate(false);
	            nDialog.setCancelable(true);
	        }
	        
	        @Override
	        protected Boolean doInBackground(String... args){
	
	            ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
	            NetworkInfo netInfo = cm.getActiveNetworkInfo();
	            if (netInfo != null && netInfo.isConnected()) {
	                try {
	                    URL url = new URL("http://www.google.com");
	                    HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
	                    urlc.setConnectTimeout(3000);
	                    urlc.connect();
	                    if (urlc.getResponseCode() == 200) {
	                    	return true;
	                    }
	                } catch (MalformedURLException e1) {
	                    e1.printStackTrace();
	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
	            }
	            return false;
	        }
	        @Override
	        protected void onPostExecute(Boolean th){
	            if(th == true){
	            	if((mode == "Login")||(mode == "Session")){
	                	new ProcessLogin(listener,mode).execute();
	                }else if(mode == "createGroup"){
	                	new ProcessNewGroup(listener).execute();
	                }else if(mode =="Register"){
	                	new ProcessRegister(listener).execute();
	                }else if(mode == "Reset"){
	                	new ProcessResetPass(listener).execute();
	                }else if(mode == "ChangePass"){
	                	new ProcessChangePass().execute();
	                }else{
	                	new ProcessGetResource(listener).execute();
	                }
	            }
	            else{
	                nDialog.dismiss();
	                Toast.makeText(ctx, "Error al conectar con internet.", Toast.LENGTH_SHORT).show();
	                error = true;
	                listener.onTaskCompleted();
	            }
	        }
	    }

	    private class ProcessLogin extends AsyncTask<String, Void, JSONObject> {
	        private ProgressDialog pDialog;
	        private String email,password;
	        private OnTaskCompleted listener;
	        private String mode;
	    	
	    	private ProcessLogin(OnTaskCompleted listener, String md){
	    		this.listener=listener;
	    		mode = md;
	    	}
	        
	        @Override
	        protected void onPreExecute() {
	            super.onPreExecute();
	            
	            email = user;
	            password = pass;
	            pDialog = new ProgressDialog(ctx);
	            
	            pDialog.setMessage("Conectando.");
	            pDialog.setIndeterminate(false);
	            pDialog.setCancelable(false);
	            pDialog.show();
	        }
	        @Override
	        protected JSONObject doInBackground(String... args) {
	            UserFunctions userFunction = new UserFunctions(ctx);
	            JSONObject json = userFunction.loginUser(email, password, mode);
	            return json;
	        }
	        @Override
	        protected void onPostExecute(JSONObject json) {
	        	
	            try {
	            	
	                if ((json!=null)&&(json.getString(KEY_SUCCESS) != null)) {
	            	   
	                    String res = json.getString(KEY_SUCCESS);
	                    result = json;
	                    if(Integer.parseInt(res) == 1){
	                        JSONObject json_user = json.getJSONObject("user");
	                        
	                        SharedPreferences pref = ctx.getSharedPreferences("SafePollSession", 0);
	                        Editor editor = pref.edit();
	                        editor.putString("CurrentUser", email);
	                        editor.putString("userID", json_user.getString(KEY_UID));
	                        editor.commit();
	                        
	                        pDialog.dismiss();
	                        error = false;
	    	                listener.onTaskCompleted();
	                    }else{
	                    	pDialog.dismiss();
	                        Toast.makeText(ctx, "Usuario o contraseña incorrectos.", Toast.LENGTH_SHORT).show();
	                        error = true;
	    	                listener.onTaskCompleted();
	                    }
	                }else{
	                	pDialog.dismiss();
	                	error = true;
		                Toast.makeText(ctx, "Problemas al conectar con el servidor. Inténtelo de nuevo más tarde.", Toast.LENGTH_SHORT).show();
		                listener.onTaskCompleted();
	                }
	            } catch (JSONException e) {
	                e.printStackTrace();
	                pDialog.dismiss();
	                error = true;
	                Toast.makeText(ctx, "Usuario o contraseña incorrectos.", Toast.LENGTH_SHORT).show();
	                listener.onTaskCompleted();
	            }
	       }
	    }
	    
	    
	    private class ProcessRegister extends AsyncTask<String, Void, JSONObject> {
	    	
	    	private OnTaskCompleted listener;
	    	
	    	private ProcessRegister(OnTaskCompleted listener){
	    		this.listener=listener;
	    	}
	    	        private ProgressDialog pDialog;
	    	        String email,password,fname,lname;
	    	        @Override
	    	        protected void onPreExecute() {
	    	            super.onPreExecute();
	    	            
	    	               fname = user;
	    	               lname = lastname;
	    	               email = mail;
	    	               password = pass;
	    	            pDialog = new ProgressDialog(ctx);
	    	            pDialog.setMessage("Enviando registro ...");
	    	            pDialog.setIndeterminate(false);
	    	            pDialog.setCancelable(false);
	    	            pDialog.show();
	    	        }
	    	        @Override
	    	        protected JSONObject doInBackground(String... args) {
	    	        UserFunctions userFunction = new UserFunctions();
	    	        JSONObject json = userFunction.registerUser(fname, lname, email, password);
	    	            return json;
	    	        }
	    	       @Override
	    	        protected void onPostExecute(JSONObject json) {

	    	                try {
	    	                    if (json.getString(KEY_SUCCESS) != null) {
	    	                        String res = json.getString(KEY_SUCCESS);
	    	                        String red = json.getString(KEY_ERROR);
	    	                        if(Integer.parseInt(res) == 1){
	    	                            result = json;
	    	                            pDialog.dismiss();
	    	                            
	    	                            error = false;
	    	        	                listener.onTaskCompleted();
	    	                        }
	    	                        else if (Integer.parseInt(red) ==2){
	    	                            pDialog.dismiss();
	    	                            Toast.makeText(ctx, "El usuario ya existe.", Toast.LENGTH_SHORT).show();
	    	                            error = true;
	    	        	                listener.onTaskCompleted();
	    	                        }
	    	                        else if (Integer.parseInt(red) ==3){
	    	                            pDialog.dismiss();
	    	                            Toast.makeText(ctx, "Email inválido.", Toast.LENGTH_SHORT).show();
	    	                            error = true;
	    	        	                listener.onTaskCompleted();
	    	                        }
	    	                    }
	    	                        else{
	    	                        pDialog.dismiss();
	    	                        Toast.makeText(ctx, "Error al intentar el registro.", Toast.LENGTH_SHORT).show();
	    	                        error = true;
	    	    	                listener.onTaskCompleted();
	    	                        }
	    	                } catch (JSONException e) {
	    	                    e.printStackTrace();
	    	                    error = true;
	    		                listener.onTaskCompleted();
	    	                }
	    	            }
	    }
	    
	    private class ProcessResetPass extends AsyncTask<String,Void,JSONObject> {
            private ProgressDialog pDialog;
            private OnTaskCompleted listener;
	    	
	    	private ProcessResetPass(OnTaskCompleted listener){
	    		this.listener=listener;
	    	}
            
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pDialog = new ProgressDialog(ctx);
                pDialog.setMessage("Enviando petición ...");
                pDialog.setIndeterminate(false);
                pDialog.setCancelable(false);
                pDialog.show();
            }
            @Override
            protected JSONObject doInBackground(String... args) {
                UserFunctions userFunction = new UserFunctions();
                JSONObject json = userFunction.forPass(user,pass);
                return json;
            }
            @Override
            protected void onPostExecute(JSONObject json) {

                try {
                    if (json.getString(KEY_SUCCESS) != null) {
                        String res = json.getString(KEY_SUCCESS);
                        String red = json.getString(KEY_ERROR);
                        if(Integer.parseInt(res) == 1){
                           pDialog.dismiss();
                           result = json;
                           if(user!=""){
                        	   Toast.makeText(ctx, "Se ha enviado un correo a tu cuenta.", Toast.LENGTH_SHORT).show(); 
                           }else{
                        	   Toast.makeText(ctx, "Obtenida cuenta de correo.", Toast.LENGTH_SHORT).show();
                           }
                           
                           error = false;
       	                   listener.onTaskCompleted();
                        }else if (Integer.parseInt(red) == 2){    
                        	pDialog.dismiss();
                        	Toast.makeText(ctx, "Email no existente en la base de datos.", Toast.LENGTH_SHORT).show();
                        	error = true;
                        	listener.onTaskCompleted();
                        }
                        else {
                            pDialog.dismiss();
                            Toast.makeText(ctx, "Error al intentar reiniciar la contraseña.", Toast.LENGTH_SHORT).show();
                            error = true;
        	                listener.onTaskCompleted();
                        }
                    }}
                catch (JSONException e) {
                    e.printStackTrace();
                    error = true;
	                listener.onTaskCompleted();
                }
            }}
	    
	    private class ProcessChangePass extends AsyncTask<String,Void,JSONObject> {
	        private ProgressDialog pDialog;
	        
	        @Override
	        protected void onPreExecute() {
	            super.onPreExecute();
	            pDialog = new ProgressDialog(ctx);
	            pDialog.setMessage("Cambiando contraseña ...");
	            pDialog.setIndeterminate(false);
	            pDialog.setCancelable(false);
	            pDialog.show();
	        }
	        @Override
	        protected JSONObject doInBackground(String... args) {
	            UserFunctions userFunction = new UserFunctions();
	            String userToken = "";
				try {
					userToken = am.getAuthToken(ac[0], AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, null, (Activity)ctx, null, null).getResult().getString(AccountManager.KEY_AUTHTOKEN).toString();
				} catch (OperationCanceledException e) {
					e.printStackTrace();
				} catch (AuthenticatorException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
	            JSONObject json = userFunction.chgPass(pass, user, userToken);
	            return json;
	        }
	        @Override
	        protected void onPostExecute(JSONObject json) {
	            try {
	                if (json.getString(KEY_SUCCESS) != null) {
	                    String res = json.getString(KEY_SUCCESS);
	                    String red = json.getString(KEY_ERROR);
	                    if (Integer.parseInt(res) == 1) {
	                        
	                        am.setPassword(ac[0], pass);

	                        pDialog.dismiss();
	                        Toast.makeText(ctx, "Contraseña cambiada.", Toast.LENGTH_SHORT).show();
	                        error = false;
	                    } else if (Integer.parseInt(red) == 2) {
	                        pDialog.dismiss();
	                        Toast.makeText(ctx, "Contraseña antigua inválida.", Toast.LENGTH_SHORT).show();
	                        error = true;
	                    } else {
	                        pDialog.dismiss();
	                        Toast.makeText(ctx, "Error al intentar cambiar la contraseña.", Toast.LENGTH_SHORT).show();
	                        error = true;
	                    }
	                }
	            } catch (JSONException e) {
	                e.printStackTrace();
	                pDialog.dismiss();
	                error = true;
	                Toast.makeText(ctx, "Error al intentar cambiar la contraseña.", Toast.LENGTH_SHORT).show();
	            }
	        }
	    }
	    
	    private class ProcessNewGroup extends AsyncTask<String,Void,JSONObject> {
	        private ProgressDialog pDialog;
	        private OnTaskCompleted listener;
	    	
	    	private ProcessNewGroup(OnTaskCompleted listener){
	    		this.listener=listener;
	    	}
	        
	        @Override
	        protected void onPreExecute() {
	            super.onPreExecute();
	            pDialog = new ProgressDialog(ctx);
	            pDialog.setMessage("Creando grupo ...");
	            pDialog.setIndeterminate(false);
	            pDialog.setCancelable(true);
	            pDialog.show();
	        }
	        @Override
	        protected JSONObject doInBackground(String... args) {
	            UserFunctions userFunction = new UserFunctions();
	            SharedPreferences pref = ctx.getSharedPreferences("SafePollSession", 0);
	            String userToken = getAccountItem("token");
	            String uid = pref.getString("userID", "Error");
	            String mail = pref.getString("CurrentUser", "Error");
	            JSONObject json = userFunction.newGroup(mail,userToken, uid, params);
	            return json;
	        }
	        @Override
	        protected void onPostExecute(JSONObject json) {
	            try {
	                if (json.getString(KEY_SUCCESS) != null) {
	                    String res = json.getString(KEY_SUCCESS);
	                    if (Integer.parseInt(res) == 1) {

	                        pDialog.dismiss();
	                        Toast.makeText(ctx, "Grupo creado.", Toast.LENGTH_SHORT).show();
	                        error = false;
	    	                listener.onTaskCompleted();
	                    } else {
	                        pDialog.dismiss();
	                        Toast.makeText(ctx, "Error al crear el grupo.", Toast.LENGTH_SHORT).show();
	                        error = true;
	    	                listener.onTaskCompleted();
	                    }
	                }
	            } catch (JSONException e) {
	                e.printStackTrace();
	                error = true;
	                listener.onTaskCompleted();
	            }
	        }
	    }
	    
	    private class ProcessGetResource extends AsyncTask<String,Void,JSONObject> {
	        private ProgressDialog pDialog;
	        private OnTaskCompleted listener;
	    	
	    	private ProcessGetResource(OnTaskCompleted listener){
	    		this.listener=listener;
	    	}
	        
	        @Override
	        protected void onPreExecute() {
	            super.onPreExecute();
	            pDialog = new ProgressDialog(ctx);
	            pDialog.setMessage("Conectando ...");
	            pDialog.setIndeterminate(false);
	            pDialog.setCancelable(false);
	            if((mode == "createPoll")||(mode == "sendVote")||(mode == "ChangeGroupOptions")){
	            	pDialog.show();
	            }
	            
	        }
	        @Override
	        protected JSONObject doInBackground(String... args) {
	            UserFunctions userFunction = new UserFunctions();
	            
	            String userToken = getAccountItem("token");
            	SharedPreferences pref = ctx.getSharedPreferences("SafePollSession", 0);
	            String uid = pref.getString("userID", "Error");
	            String mail = pref.getString("CurrentUser", "Error");
	            
	            if(mode == "getGroupsList"){
		            JSONObject json = userFunction.getGroupsList(mail, userToken,uid);
		            return json;
	            }else if(mode == "getPollsList"){
		            JSONObject json = userFunction.getPollsList(mail, userToken,user,uid);
		            return json;
	            }else if(mode == "sendVote"){ 
		            JSONObject json = userFunction.sendVote(mail, userToken,uid, params);
		            return json;
	            }else if(mode == "createPoll"){ 
		            JSONObject json = userFunction.createPoll(mail, userToken,params);
		            return json;
	            }else if(mode == "groupSearch"){
		            JSONObject json = userFunction.searchGroups(mail, userToken,uid, user);
		            return json;
	            }else if(mode == "userAddGroup"){
		            JSONObject json = userFunction.userAddGroup(mail, userToken,user,uid,pass);
		            return json;	
	            }else if(mode == "userRequest"){
		            JSONObject json = userFunction.userRequest(mail, userToken,user,pass);
		            return json;	
	            }else if(mode == "decideRequest"){
		            JSONObject json = userFunction.decideRequest(mail, userToken,params);
		            return json;	
	            }else if(mode == "WithdrawGroup"){
		            JSONObject json = userFunction.userWithdrawGroup(mail, userToken,user,uid);
		            return json;	
	            }else if(mode == "DeleteGroup"){
		            JSONObject json = userFunction.deleteGroup(mail, userToken,user,uid);
		            return json;
	            }else if(mode == "setPublicKey"){
		            JSONObject json = userFunction.setPublicKey(mail, userToken,pass, user);
		            return json;
	            }else if(mode == "getPublicKey"){
		            JSONObject json = userFunction.getPublicKey(mail, userToken);
		            return json;
	            }else if(mode == "ChangeGroupOptions"){
		            JSONObject json = userFunction.changeOptions(mail, userToken,user,pass);
		            return json;
	            }else if(mode == "getRequests"){
		            JSONObject json = userFunction.getRequests(mail, userToken,user);
		            return json;
	            }else if(mode == "getMembers"){
		            JSONObject json = userFunction.getMembers(mail, userToken,user);
		            return json;
	            }else if(mode == "changeAdm"){
		            JSONObject json = userFunction.changeAdm(mail, userToken,params);
		            return json;
	            }else{
		            JSONObject json = userFunction.getGroupsList(mail, userToken,uid);
		            return json;
	            }
	        }
	        @Override
	        protected void onPostExecute(JSONObject json) {
	            try {
	                if (json.getString(KEY_SUCCESS) != null) {
	                    String res = json.getString(KEY_SUCCESS);
	                    if (Integer.parseInt(res) == 1) {
	                        
	                    	if((mode == "createPoll")||(mode == "sendVote")||(mode == "ChangeGroupOptions")){
	        	            	pDialog.dismiss();
	        	            }
	                        result = json;
	                        error = false;
	                        if(listener!=null){
	                        	listener.onTaskCompleted();
	                        }
	                    } else {
	                    	if((mode == "createPoll")||(mode == "sendVote")||(mode == "ChangeGroupOptions")){
	        	            	pDialog.dismiss();
	        	            }
	                        Toast.makeText(ctx, json.getString("error_msg"), Toast.LENGTH_SHORT).show();
	                        error = true;
	                        if(listener!=null){
	                        	listener.onTaskCompleted();
	                        }
	                    }
	                }
	            } catch (JSONException e) {
	                e.printStackTrace();
	                if((mode == "createPoll")||(mode == "sendVote")||(mode == "ChangeGroupOptions")){
		            	pDialog.dismiss();
		            }
	                error = true;
	                if(listener!=null){
	                	listener.onTaskCompleted();
	                }
	            }
	            
	        }
	    }
	    
	    public String ProcessNewToken(){ 
	    	String result = "";
	    		UserFunctions userFunction = new UserFunctions();
	            JSONObject json = userFunction.loginUser(user, pass, "Login");
	            
	            
	            try {
	            	
	                if (json.getString(KEY_SUCCESS) != null) {
	            	   
	                    String res = json.getString(KEY_SUCCESS);
	                    if(Integer.parseInt(res) == 1){
	                        result = (String) json.get(KEY_TOKEN);
	                        
	                    }else{	                        
	                        Toast.makeText(ctx, "Usuario o contraseña incorrectos.", Toast.LENGTH_SHORT).show();
	                        
	                    }
	                }
	            } catch (JSONException e) {
	                e.printStackTrace();
	            }
	        return result;
	    }
	    	    
	    private String getAccountItem(String item){
	    	Account[] ac = am.getAccountsByType("com.safepoll");
	    	if (item == "name"){
	    		return ac[0].name;
	    	}else if (item == "password"){
	    		return am.getPassword(ac[0]);
	    	}else if (item == "token"){
	    		AccountManagerFuture<Bundle> tokenResponse = am.getAuthToken(ac[0], AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, null, null, null, null);
	    		Bundle b;
				try {
					b = tokenResponse.getResult();
					return b.getString(AccountManager.KEY_AUTHTOKEN);
				} catch (OperationCanceledException e) {
					e.printStackTrace();
					return "";
				} catch (AuthenticatorException e) {
					e.printStackTrace();
					return "";
				} catch (IOException e) {
					e.printStackTrace();
					return "";
				} 
	    	}else{
	    		return "";
	    	}
            
	    }
	    
	    public void NetAsync(View view,OnTaskCompleted listener ){
	    	NetCheck nCheck = new NetCheck(listener);
	    	nCheck.execute();
	    }
	    
	    
	    public boolean Error(){
	    	return error;
	    }
	    
	    public JSONObject getJson(){
	    	return result;
	    }
	    
	    public String getMode(){
	    	return mode;
	    }
    
}
