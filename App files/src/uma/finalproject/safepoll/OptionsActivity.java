/**
 * @author Daniel Domínguez Restoy
 * @version 1.0
 */

package uma.finalproject.safepoll;

import java.security.cert.X509Certificate;
import uma.finalproject.crypto.CertificateManager;
import uma.finalproject.database.ConnectionManager;
import uma.finalproject.database.OnTaskCompleted;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.security.KeyChainAliasCallback;
import android.widget.Toast;

public class OptionsActivity extends PreferenceActivity implements KeyChainAliasCallback, OnTaskCompleted{
	private CertificateManager certMan;
	private String myAlias = "";
	private boolean aliasSelected = false, setChain = false, certChainedRequired = false;
	private Preference selectCert, chainCert, updateCert;
	private PreferenceCategory prefCat;
	private String userName = "user";
	private X509Certificate[] certs;
	private Context ctx;
	private String aliasRetrieved = "";
	private OnTaskCompleted listener = this;
	private ConnectionManager cm;
	private StringBuffer sb;
		
	@SuppressWarnings("deprecation")
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
               
        ctx = this;
        
        SharedPreferences pref = ctx.getSharedPreferences("SafePollSession", 0);
        userName = pref.getString("CurrentUser", "Error");
        certChainedRequired = pref.getBoolean("CertChained", false);
                
        if(userName!="Error"){
        	certMan = new CertificateManager(this, userName);
        }else{
        	Toast.makeText(getApplicationContext(), "Fallo al obtener id de usuario.", Toast.LENGTH_SHORT).show();
			finish();
        }
        
        prefCat=(PreferenceCategory)findPreference("pref_user");
        prefCat.setTitle(prefCat.getTitle() + " " + userName);
        
       selectCert = (Preference) findPreference("pref_user_select_cert");
       chainCert = (Preference) findPreference("pref_user_chain_cert");
       updateCert = (Preference) findPreference("pref_user_update_cert");
       
       
       aliasRetrieved = certMan.getAlias();
       if(aliasRetrieved != "Error"){
       	
       selectCert.setSummary(aliasRetrieved);
       	
			if(certMan.isChained()){
				selectCert.setTitle("Certificado encadenado:");
				prefCat.removePreference(chainCert);
				selectCert.setEnabled(false);
			}else{
				selectCert.setTitle("Certificado:");
			}
			
       }else{
    	   if(certChainedRequired){
    		   selectCert.setTitle("Certificado encadenado:");
    		   prefCat.removePreference(chainCert);
    		   selectCert.setEnabled(false);
    		   updateCert.setTitle("Seleccionar certificado");
    		   updateCert.setSummary("Seleccione el certificado que se enlazó a esta cuenta.(Debe estar instalado previamente)");
    		   
    	   }else{
    		   chainCert.setEnabled(false);
        	   updateCert.setEnabled(false);
    	   }
       }
       
       selectCert.setOnPreferenceClickListener(new OnPreferenceClickListener(){

		@Override
		public boolean onPreferenceClick(Preference pref) {
			
			new AsyncTask<Void, Void, Boolean>() {

				@Override
				protected Boolean doInBackground(Void... arg0) {
					certMan.selectCertificate();
					while(!aliasSelected){
						
					}
					
					return myAlias!="";
				}
				
				@Override
				protected void onPostExecute(Boolean valid) {
					if(valid){
						
						new AsyncTask<Void, Void, Boolean>() {

							@Override
							protected Boolean doInBackground(Void... arg0) {
								certs = certMan.getCertificateChain(myAlias);
								final StringBuffer sb = new StringBuffer();
						        sb.append(certs[0].getSubjectDN());      
						        certMan.saveAlias(myAlias);
						        certMan.saveCertData(sb.toString());
						        
								return certMan.checkCertificate();
							}
							
							@Override
					        protected void onPostExecute(Boolean valid) {
								if(valid){
									selectCert.setTitle("Certificado:");
									selectCert.setSummary(myAlias);
									chainCert.setEnabled(true);
									updateCert.setEnabled(true);
								}
							}
							
						}.execute();
						
						
					}
					aliasSelected = false;
					
				}
    			
    		}.execute();
			
			return false;
		}
    	   
       });
       
       chainCert.setOnPreferenceClickListener(new OnPreferenceClickListener(){

   			@Override
   			public boolean onPreferenceClick(Preference pref) {
   				new AlertDialog.Builder(ctx)
   			    .setTitle("Enlazar certificado")
   			    .setMessage("Esto le permitirá firmar sus votos pero el certificado quedará enlazado permanentemente y no se podrá usar otro distinto en el futuro con esta cuenta. ¿Desea realizar esta acción?")
   			    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
   			        public void onClick(DialogInterface dialog, int which) { 
   			        	
   			        	new AsyncTask<Void, Void, Boolean>() {

							@Override
							protected Boolean doInBackground(Void... params) {
								certs = certMan.getCertificateChain();
								final StringBuffer sb = new StringBuffer();
						        sb.append(certs[0].getSubjectDN());
								cm = new ConnectionManager(ctx,sb.toString(),certMan.byteToString(certMan.getPublicKey(certMan.getAlias()).getEncoded()),"setPublicKey");
		   			        	
								return true;
							}
   			        		
							@Override
					        protected void onPostExecute(Boolean valid) {
								if(valid){
									setChain = true;
									startCM();
								}
							}
   			        	}.execute();
   			        }
   			     })
   			    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
   			        public void onClick(DialogInterface dialog, int which) { 
   			            // do nothing
   			        }
   			     })
   			    .setIcon(android.R.drawable.ic_dialog_alert)
   			     .show();
   				
   			
   				return false;
   			}
       	   
       });
       
       updateCert.setOnPreferenceClickListener(new OnPreferenceClickListener(){

      		@Override
      		public boolean onPreferenceClick(Preference pref) {
      			
      			
      			new AsyncTask<Void, Void, Boolean>() {

    				@Override
    				protected Boolean doInBackground(Void... arg0) {
    					certMan.selectCertificate();
    					while(!aliasSelected){
    						
    					}
    					
    					return myAlias!="";
    				}
    				
    				@Override
    				protected void onPostExecute(Boolean valid) {
    					if(valid){
    						
    						new AsyncTask<Void, Void, Boolean>() {

    							@Override
    							protected Boolean doInBackground(Void... arg0) {
    								certs = certMan.getCertificateChain(myAlias);
    						        sb = new StringBuffer();
    						        sb.append(certs[0].getSubjectDN());
    						        
    						        if(certChainedRequired){
    						        	
    						        	setChain = true;
    						        }else{
    						        	if(sb.toString().equals(certMan.getCertData())){
        						        	certMan.saveAlias(myAlias);
            						        certMan.saveCertData(sb.toString());
        						        }
    						        }
    						        cm = new ConnectionManager(ctx,sb.toString(),certMan.byteToString(certMan.getPublicKey(myAlias).getEncoded()),"setPublicKey");
    								return true;
    							}
    							
    							@Override
    							protected void onPostExecute(Boolean valid){
    								if(valid){
    									startCM();
    									if(!certChainedRequired){
    										selectCert.setTitle("Certificado:");
            	    						selectCert.setSummary(myAlias);
    									}
    									
    								}
    								
    							}
    							
    						}.execute();
    						
    						
    					}
    					aliasSelected = false;
    					
    				}
        			
        		}.execute();
      			
      			
      			return false;
      		}
          	   
       });
    }
		
		private void startCM(){
			cm.NetAsync(getWindow().getDecorView().findViewById(android.R.id.content),listener);
		}
		
		@Override
		public void alias(String alias) {
			if(alias!=null){
				myAlias = alias;
			}
			aliasSelected = true;
	    	
		}
		@Override
		public void onTaskCompleted() {
			if(!cm.Error()){
				if(setChain){
					if(certChainedRequired){
						certMan.saveAlias(myAlias);
				        certMan.saveCertData(sb.toString());
						selectCert.setSummary(myAlias);
						updateCert.setTitle(R.string.pref_title_update_cert);
						updateCert.setSummary(R.string.pref_summary_update_cert);
					}
					new AsyncTask<Void, Void, Boolean>() {

						@Override
						protected Boolean doInBackground(Void... arg0) {
							certMan.setChained();
							setChain = false;
		  					prefCat.removePreference(chainCert);
							return null;
						}
						
					}.execute();
					selectCert.setEnabled(false);
				}
				if(certMan.isChained()){
  					selectCert.setEnabled(false);
  					prefCat.removePreference(chainCert);
  				}else{
  					//error
  				}
			}
		}
		
		public void onPause() {
		     super.onPause();
		     overridePendingTransition(0, 0);
		 }
}
