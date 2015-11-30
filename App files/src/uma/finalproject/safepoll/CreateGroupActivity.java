/**
 * @author Daniel Domínguez Restoy
 * @version 1.0
 */

package uma.finalproject.safepoll;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.message.BasicNameValuePair;
import uma.finalproject.crypto.CertificateManager;
import uma.finalproject.database.ConnectionManager;
import uma.finalproject.database.OnTaskCompleted;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class CreateGroupActivity extends ActionBarActivity implements OnTaskCompleted{
	
	private Context ctx;
	private ActionBar actionBar;
	private EditText groupName, description;
	private CheckBox groupPrivate, groupRequeriment;
	private ConnectionManager cm;
	private OnTaskCompleted listener;
	private CertificateManager certMan;
	private String userName;
	private boolean allowed = false;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.creategrouplayout);
        ctx = this;
        listener = this;
        
        actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.newgroup_title);
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        groupName = ((EditText) findViewById(R.id.newgroup_name));
        description = ((EditText) findViewById(R.id.newgroup_description));
        groupPrivate = (CheckBox) findViewById(R.id.newgroup_option_private);
        groupRequeriment = (CheckBox) findViewById(R.id.newgroup_option_requeriment);
        
        SharedPreferences pref = ctx.getSharedPreferences("SafePollSession", 0);
        userName = pref.getString("CurrentUser", "Error");
        
        
        if(userName!="Error"){
        	certMan = new CertificateManager(this, userName);
        }else{
        	Toast.makeText(getApplicationContext(), "Fallo al obtener id de usuario.", Toast.LENGTH_SHORT).show();
			finish();
        }
        
        findViewById(R.id.newgroup_ButtonOK).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); 
            	inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            	
            	if (  ( !groupName.getText().toString().equals("")) && ( !description.getText().toString().equals("")) )
                { 
            		String options = "";
                	if(groupPrivate.isChecked()){
                		options = options + "1";
                	}else{
                		options = options + "0";
                	}
                	if(groupRequeriment.isChecked()){
                		options = options + "1";
                		allowed = certMan.isChained();
                	}else{
                		options = options + "0";
                		allowed = true;
                	}
                	if(allowed){
                		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
                        params.add(new BasicNameValuePair("gname", groupName.getText().toString()));
                        params.add(new BasicNameValuePair("desc",description.getText().toString() ));
                        params.add(new BasicNameValuePair("options",options ));
                        
                		cm = new ConnectionManager(ctx,params,"createGroup");
            			cm.NetAsync(v,listener);
                	}else{
                		Toast.makeText(getApplicationContext(),
                                "Debe enlazar un certificado a su cuenta antes de exigir certificados en un grupo.", Toast.LENGTH_SHORT).show();
                	}
                	
                }
                else if (groupName.getText().toString().equals(""))
                {
                    Toast.makeText(getApplicationContext(),
                            "No ha escrito el nombre del grupo", Toast.LENGTH_SHORT).show();
                }
                else if (description.getText().toString().equals(""))
                {
                    Toast.makeText(getApplicationContext(),
                            "Descripción del grupo vacía", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(getApplicationContext(),
                            "Los campos de texto están vacíos", Toast.LENGTH_SHORT).show();
                }
            	
            }
        });
	}

	@Override
	public void onTaskCompleted() {
		if(!cm.Error()){
			Toast.makeText(getApplicationContext(), "¡Grupo creado!", Toast.LENGTH_SHORT).show();
			finish();
		}else{
			Toast.makeText(getApplicationContext(), "Ha habido problemas para crear el grupo.", Toast.LENGTH_SHORT).show();
		}
		
	}
	
	public void onPause() {
	     super.onPause();
	     overridePendingTransition(0, 0);
	 }
}
