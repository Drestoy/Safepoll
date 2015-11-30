/**
 * @author Daniel Domínguez Restoy
 * @version 1.0
 */

package uma.finalproject.safepoll;

import uma.finalproject.crypto.CertificateManager;
import uma.finalproject.database.ConnectionManager;
import uma.finalproject.database.OnTaskCompleted;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.widget.Toast;

public class GroupOptionsActivity extends PreferenceActivity implements OnTaskCompleted{
	
	private ConnectionManager cm;
	private String options, guid, newOptions,userName;
	private OnTaskCompleted listener;
	private Context ctx;
	private boolean changesDone = false;
	private CertificateManager certMan;
	
	private CheckBoxPreference gPrivate, requeriment;
	
	@SuppressWarnings("deprecation")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.group_options);
        
        Bundle group_info = getIntent().getExtras();
        guid = group_info.getString("guid");
        options = group_info.getString("options");
        
        ctx=this;
        
        SharedPreferences pref = ctx.getSharedPreferences("SafePollSession", 0);
        userName = pref.getString("CurrentUser", "Error");
                
        if(userName!="Error"){
        	certMan = new CertificateManager(this, userName);
        }else{
        	Toast.makeText(getApplicationContext(), "Fallo al obtener id de usuario.", Toast.LENGTH_SHORT).show();
			finish();
        }
        
        newOptions = options;
        
        gPrivate = (CheckBoxPreference) findPreference("pref_group_private");
        requeriment = (CheckBoxPreference) findPreference("pref_group_requeriment");
        
        ctx = this;
        listener = this;
        
        if(options.charAt(0) == '1'){
        	gPrivate.setChecked(true);
        }else{
        	gPrivate.setChecked(false);
        }
        
        if(options.charAt(1) == '1'){
        	requeriment.setChecked(true);
        }else{
        	requeriment.setChecked(false);
        }
        
        gPrivate.setOnPreferenceClickListener(new CheckBoxPreference.OnPreferenceClickListener(){

			@Override
			public boolean onPreferenceClick(Preference arg0) {
				if(gPrivate.isChecked()){
					options = "1" + options.charAt(1);
				}else{
					options = "0" + options.charAt(1);
				}
				
				return true;
			}
        	
        });
        
        requeriment.setOnPreferenceClickListener(new CheckBoxPreference.OnPreferenceClickListener(){

			@Override
			public boolean onPreferenceClick(Preference arg0) {
				if(requeriment.isChecked()){
					options = options.charAt(0) + "1";
				}else{
					options = options.charAt(0) + "0";
				}
				
				return true;
			}
        	
        });
	}

	@Override
	public void onTaskCompleted() {
		if(!cm.Error()){
			changesDone = true;
			onBackPressed();
		}
		
	}
	
	@Override
    public void onBackPressed() {
		if((changesDone)||(options.equals(newOptions))){
			super.onBackPressed();
		}else{
			if(options.charAt(1) == '1'){
				if(certMan.isChained()){
					cm = new ConnectionManager(ctx,guid,options,"ChangeGroupOptions");
					cm.NetAsync(getWindow().getDecorView().findViewById(android.R.id.content), listener);
				}else{
					Toast.makeText(getApplicationContext(),
                            "Debe enlazar un certificado a su cuenta antes de exigir certificados en un grupo.", Toast.LENGTH_SHORT).show();
				}
			}else{
				cm = new ConnectionManager(ctx,guid,options,"ChangeGroupOptions");
				cm.NetAsync(getWindow().getDecorView().findViewById(android.R.id.content), listener);
			}
			
		}
    }
}
