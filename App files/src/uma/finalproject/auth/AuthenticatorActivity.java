/**
 * @author Daniel Domínguez Restoy
 * This class is based in an implementation made by Udinic: https://github.com/Udinic
 */
package uma.finalproject.auth;

import org.json.JSONObject;
import uma.finalproject.database.ConnectionManager;
import uma.finalproject.database.OnTaskCompleted;
import uma.finalproject.safepoll.R;
import uma.finalproject.safepoll.RegistryActivity;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;


public class AuthenticatorActivity extends AccountAuthenticatorActivity implements OnTaskCompleted {

    public final static String ARG_ACCOUNT_TYPE = "ACCOUNT_TYPE";
    public final static String ARG_AUTH_TYPE = "AUTH_TYPE";
    public final static String ARG_ACCOUNT_NAME = "ACCOUNT_NAME";
    public final static String ARG_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_ACCOUNT";

    public static final String KEY_ERROR_MESSAGE = "ERR_MSG";

    public final static String PARAM_USER_PASS = "USER_PASS";
    
    private static String KEY_REFRESH_TOKEN = "token";

    private final int REQ_SIGNUP = 1;

    private AccountManager mAccountManager;
    private String mAuthTokenType;
    
    private EditText userName,userPass;
    private ConnectionManager cm;
    
    private ProgressBar p1;
    private Context ctx;
    
    private OnTaskCompleted listener;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getActionBar().hide();
        
        setContentView(R.layout.loginlayout);
        mAccountManager = AccountManager.get(getBaseContext());
        
        mAuthTokenType = getIntent().getStringExtra(ARG_AUTH_TYPE);
        if (mAuthTokenType == null)
            mAuthTokenType = AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS;

        userName = ((EditText) findViewById(R.id.login_editText_user));
        userPass = ((EditText) findViewById(R.id.login_editText_pass));
        
        
        p1= (ProgressBar) findViewById(R.id.progressbar);
        p1.setVisibility(View.INVISIBLE);
        listener = this;
        
        ctx = this;
        
        ImageView logo = (ImageView) findViewById(R.id.Logo);
        Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		
		logo.setScaleX((float)0.5);
		logo.setScaleY((float)0.5);
        
        findViewById(R.id.login_Button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); 
            	inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            	
            	cm = new ConnectionManager(ctx,userName.getText().toString(),userPass.getText().toString(),"Login");
            	if (  ( !userName.getText().toString().equals("")) && ( !userPass.getText().toString().equals("")) )
                { 
        			p1.setVisibility(View.VISIBLE);
        			cm.NetAsync(v,listener);
                }
                else if ( ( !userName.getText().toString().equals("")) )
                {
                    Toast.makeText(getApplicationContext(),
                            "Password field empty", Toast.LENGTH_SHORT).show();
                }
                else if ( ( !userPass.getText().toString().equals("")) )
                {
                    Toast.makeText(getApplicationContext(),
                            "Email field empty", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(getApplicationContext(),
                            "Email and Password field are empty", Toast.LENGTH_SHORT).show();
                }
            	
            }
        });
        findViewById(R.id.textLink_signUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signup = new Intent(getBaseContext(), RegistryActivity.class);
                signup.putExtra("PANTALLA_REGISTRO", true);
                signup.putExtras(getIntent().getExtras());
                startActivityForResult(signup, REQ_SIGNUP);
            }
        });
        findViewById(R.id.textLink_recoverPass).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	Intent intent=new Intent(AuthenticatorActivity.this,RegistryActivity.class);
        		intent.putExtra("PANTALLA_REGISTRO", false);
        		startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQ_SIGNUP && resultCode == RESULT_OK) {
        	userName.setText(data.getStringExtra("mail"));
        	userPass.setText(data.getStringExtra("pass"));
        	cm = new ConnectionManager(ctx,data.getStringExtra("mail"),data.getStringExtra("pass"),"Login");
        	p1.setVisibility(View.VISIBLE);
    		cm.NetAsync(getWindow().getDecorView().findViewById(android.R.id.content),listener);
        } else{
        	super.onActivityResult(requestCode, resultCode, data);
        }
            
    }

    
    private void finishLogin(Intent intent) {

        String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        String accountPassword = intent.getStringExtra(PARAM_USER_PASS);
        
        final Account account = new Account(accountName, intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));
   
        if (getIntent().getBooleanExtra(ARG_IS_ADDING_NEW_ACCOUNT, false)) {
            String authtoken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);
            String authtokenType = mAuthTokenType;
            mAccountManager.addAccountExplicitly(account, accountPassword, null);
            mAccountManager.setAuthToken(account, authtokenType, authtoken);
        } else {
            mAccountManager.setPassword(account, accountPassword);
        }

        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }
    
    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

	@Override
	public void onTaskCompleted() {
		if(!cm.Error()){
			JSONObject json = cm.getJson();
			
			Bundle data = new Bundle();
            try {
            	String token = json.getString(KEY_REFRESH_TOKEN);
                data.putString(AccountManager.KEY_ACCOUNT_NAME, userName.getText().toString());
                data.putString(AccountManager.KEY_ACCOUNT_TYPE, getIntent().getStringExtra(ARG_ACCOUNT_TYPE));
                data.putString(AccountManager.KEY_AUTHTOKEN, token);
                data.putString(PARAM_USER_PASS, userPass.getText().toString());
                data.putBoolean("certChained", json.getJSONObject("user").getBoolean("subject"));

            } catch (Exception e) {
            	data.putString(KEY_ERROR_MESSAGE, e.getMessage());
            }

            final Intent res = new Intent();
            res.putExtras(data);
			
			finishLogin(res);
		}
		p1.setVisibility(View.INVISIBLE);
	}

}