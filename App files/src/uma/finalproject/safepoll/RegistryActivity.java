/**
 * @author Daniel Domínguez Restoy
 * @version 1.0
 */

package uma.finalproject.safepoll;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import uma.finalproject.crypto.CertificateManager;
import uma.finalproject.database.ConnectionManager;
import uma.finalproject.database.OnTaskCompleted;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.security.KeyChainAliasCallback;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ViewSwitcher;

public class RegistryActivity extends Activity implements OnTaskCompleted, KeyChainAliasCallback{
	
	private ViewSwitcher switcher;
	private boolean pRegistro;
	private EditText user, pass, mail, lastname;
	private ConnectionManager cm;
	private Context ctx;
	private OnTaskCompleted listener = this;
	private Button getCertButton,sendCertButton, regButton,resButton;
	private CertificateManager certMan;
	private String myAlias = "";
	private boolean aliasSelected = false;
	private X509Certificate[] certs;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registrylayout);
        
        Intent intent = getIntent();
        pRegistro = intent.getBooleanExtra("PANTALLA_REGISTRO", false);
        
        switcher = (ViewSwitcher) findViewById(R.id.viewSwitcher1);
        getCertButton = (Button) findViewById(R.id.get_certificate_Button);
        sendCertButton = (Button) findViewById(R.id.passrecover_cert_sendButton);
        regButton = (Button) findViewById(R.id.signup_Button);
        resButton = (Button) findViewById(R.id.passrecover_sendButton);
        mail = (EditText) findViewById(R.id.passrecover_mail);
        
        ctx = this;
        if(!pRegistro){ //Si la pantalla requerida no es la de registro, entonces es la de recuperar password
        	switcher.showNext();
        	
        	certMan = new CertificateManager(this,"");
        	
        	resButton.setOnClickListener(new Button.OnClickListener() {
            	public void onClick(View v) {
            		if ( !mail.getText().toString().equals(""))
                    {
            			cm = new ConnectionManager(ctx, mail.getText().toString(), "","Reset");
            			cm.NetAsync(v,listener);
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),
                                "Email field is empty", Toast.LENGTH_SHORT).show();
                    }
        		}
            }
            );
        	
        	getCertButton.setOnClickListener(new Button.OnClickListener() {
            	public void onClick(View v) {
            		
            		new AsyncTask<Void, Void, Boolean>() {

						@Override
						protected Boolean doInBackground(Void... arg0) {
							certMan.selectCertificate();
							while(!aliasSelected){
								// Esperar
							}
							
							return myAlias!="";
						}
						
						@Override
						protected void onPostExecute(Boolean valid) {
							if(valid){
								getCertButton.setText(myAlias);
							}
							aliasSelected = false;
							
						}
            			
            		}.execute();
            		
            		
        		}
            }
            );
        	
        	sendCertButton.setOnClickListener(new Button.OnClickListener() {
            	public void onClick(View v) {
            		
            		new AsyncTask<Void, Void, Boolean>() {

						@Override
						protected Boolean doInBackground(Void... arg0) {
							if(myAlias!=""){
								certs = certMan.getCertificateChain(myAlias);
								final StringBuffer sb = new StringBuffer();
						        sb.append(certs[0].getSubjectDN());
								cm = new ConnectionManager(ctx,"",sb.toString(),"Reset");
								
								return true;
							}else{
								return false;
							}
						}
						
						@Override
						protected void onPostExecute(Boolean valid) {
							if(valid){
								startCM();
							}
						}
            			
            		}.execute();
            		
            		
        		}
            }
            );
        	
        	
        }else{
        	user = (EditText) findViewById(R.id.signup_name);
        	pass = (EditText) findViewById(R.id.signup_password);
        	mail = (EditText) findViewById(R.id.signup_mail);
        	lastname = (EditText) findViewById(R.id.signup_lastname);
        	regButton.setOnClickListener(new Button.OnClickListener() {
            	public void onClick(View v) {
            		InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); 
                	inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                	List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
                    params.add(new BasicNameValuePair("user", user.getText().toString()));
                    params.add(new BasicNameValuePair("lastname", lastname.getText().toString()));
                    params.add(new BasicNameValuePair("mail", mail.getText().toString()));
                    params.add(new BasicNameValuePair("pass", pass.getText().toString()));
                    
                    
            		cm = new ConnectionManager(ctx,params,"Register");
            		if (  ( !user.getText().toString().equals("")) && ( !pass.getText().toString().equals("")) && ( !mail.getText().toString().equals(""))&& ( !lastname.getText().toString().equals("")))
                    {
            			cm.NetAsync(v, listener);
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),
                                "Some fields are empty", Toast.LENGTH_SHORT).show();
                    }
        		}
            }
            );
        	
        }
    }
	
	private void startCM(){
		cm.NetAsync(getWindow().getDecorView().findViewById(android.R.id.content),listener);
	}
	
	@Override
	public void onTaskCompleted() {
		if(!cm.Error()){
			if(pRegistro){ //Si estabamos realizando un nuevo registro:
				JSONObject json = cm.getJson();
				String mail = "", pass = "";
				Intent res = new Intent();
				try {
					mail =  (String) json.getString("email");
					pass =  (String) json.getString("pass");
					
					
		            res.putExtra("mail",mail);
		            res.putExtra("pass",pass);
		            setResult(RESULT_OK, res);
		            finish();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
	            setResult(RESULT_CANCELED, res);
	            finish();
				
			}else{
				if(myAlias!=""){
					JSONObject json = cm.getJson();
					try {
						mail.setText(json.getString("result"));
						getCertButton.setText(R.string.get_certificate_button);
						myAlias = "";
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}else{
					finish();
				}
				
			}
			
		}
		
	}

	
	
	@Override
	public void alias(String alias) {
		if(alias!=null){
			Log.e("JANDER","Alias: " + alias + ".");
			myAlias = alias;
		}
		aliasSelected = true;
	}
	
	
}
