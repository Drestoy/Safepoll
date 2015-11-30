/**
 * @author Daniel Domínguez Restoy
 * @version 1.0
 */

package uma.finalproject.safepoll;

import java.util.ArrayList;
import java.util.List;
import uma.finalproject.support.Main_list_item;
import uma.finalproject.support.SafePoll_ListAdapter;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.SearchView;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.ViewSwitcher;
import android.os.AsyncTask;
import uma.finalproject.auth.AccountGeneral;
import uma.finalproject.auth.AuthenticatorActivity;
import uma.finalproject.crypto.CertificateManager;
import uma.finalproject.database.ConnectionManager;
import uma.finalproject.database.OnTaskCompleted;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

public class MainActivity extends ActionBarActivity implements OnTaskCompleted{
	
	private String userName;
	private String token = "";
	Account[] ac;
	AccountManagerFuture<Bundle> tokenResponse;
	OnTaskCompleted listener = this;
	private boolean init = true;
	
	private View view;
	private boolean certChained = false;
	private CertificateManager certMan;
	private ViewSwitcher switcher;
	
	private ListView main_ListView;
    private Context ctx;
    private List<Main_list_item> main_list;
    private Main_list_item list_item;
    
    private SearchView searchView;
    
    private MenuItem m;
    
    ProgressBar p1;
    ActionBar actionBar;
    
    ConnectionManager cm;
    
    public final static String ARG_ACCOUNT_TYPE = "ACCOUNT_TYPE";
    public final static String ARG_AUTH_TYPE = "AUTH_TYPE";
    public final static String ARG_ACCOUNT_NAME = "ACCOUNT_NAME";
    public final static String ARG_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_ACCOUNT";
    public final static String PARAM_USER_PASS = "USER_PASS";
    private final int REQ_LOGIN = 1;
    
    AccountManager am;
	
    private String[] contextMenuOptions = {"Salir del grupo", "Borrar grupo"};
    
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getActionBar().hide();
        
        setContentView(R.layout.mainlayout);
        
        ctx=this; 
        main_list = new ArrayList<Main_list_item>();
        ImageView logo = (ImageView) findViewById(R.id.main_Logo);
        Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		
		logo.setScaleX((float)0.3);
		logo.setScaleY((float)0.3);
        
        switcher = (ViewSwitcher) findViewById(R.id.viewSwitcher1);
        actionBar = getSupportActionBar();
        actionBar.hide();
        
        view = getWindow().getDecorView().findViewById(android.R.id.content);
        
        am = AccountManager.get(getBaseContext());
        
        p1= (ProgressBar) findViewById(R.id.progressbar);
        
        ac = am.getAccountsByType("com.safepoll");
        if(ac!=null){
        	if(ac.length<=0){
              	Intent intent = new Intent(getBaseContext(),AuthenticatorActivity.class);
              	intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
              	Bundle extras = new Bundle();
    			extras.putString(ARG_AUTH_TYPE, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS);
            	extras.putString(ARG_ACCOUNT_TYPE, "com.safepoll");
            	extras.putBoolean(ARG_IS_ADDING_NEW_ACCOUNT, true);
            	intent.putExtras(extras);
    			
    			startActivityForResult(intent, REQ_LOGIN);
            }else{
            	if(ac.length==1){
            		userName = ac[0].name;
            		tokenResponse = am.getAuthToken(ac[0], AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, null, this, null, null);
            		
            		
                    
            		new AsyncTask<Void, Void, String>() {
            			
            			
                        @Override
                        protected String doInBackground(Void... params) {
                        	
                        	
                    		try {
                    			
                    			Bundle b = tokenResponse.getResult();
                    			token = b.getString(AccountManager.KEY_AUTHTOKEN);
                    		} catch (OperationCanceledException e) {
                    			e.printStackTrace();
                    		} catch (AuthenticatorException e) {
                    			e.printStackTrace();
                    		} catch (IOException e) {
                    			e.printStackTrace();
                    		}
                            return token;
                        }

                        @Override
                        protected void onPostExecute(String token) {
                        	if (token != null){
                        		cm = new ConnectionManager(ctx,userName,token,"Session");
                                cm.NetAsync(getWindow().getDecorView().findViewById(android.R.id.content),listener);
                            }
                        }
                    }.execute();
            		
            	}else{
            		//MOSTRAR DIALOG CON TODAS LAS CUENTAS PARA ELEGIR UNA
            	}
            }
        }
        
        main_ListView = (ListView) findViewById(R.id.main_screen_list);
        registerForContextMenu(main_ListView);
        
        main_ListView.setOnItemClickListener(new ListView.OnItemClickListener(){
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                	Main_list_item item = (Main_list_item) parent.getItemAtPosition(position);
                	boolean certRequired = (item.getOptions().charAt(1) == '1');
                	if((certRequired)&&(!certMan.isChained())){
                		Toast.makeText(ctx, "Necesita tener un certificado enlazado a su cuenta para poder acceder a este grupo.", Toast.LENGTH_SHORT).show();
                	}else{
                		Bundle group_data = new Bundle();
                    	group_data.putString("Nombre", item.getName());
                    	group_data.putString("ID", item.getID());
                    	group_data.putString("Imagen", item.getImage());
                    	group_data.putString("options", item.getOptions());
                    	Intent intent=new Intent(MainActivity.this,GroupActivity.class);
                		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                		intent.putExtras(group_data);
                		startActivity(intent);
                	}
                    
                }
                catch(Exception e) {
                	e.printStackTrace();
                }
            }    	
        });  
    }
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQ_LOGIN && resultCode == RESULT_OK) {
        	switcher.showNext();
    		actionBar.show();
    		Bundle extras = data.getExtras();
    		certChained = extras.getBoolean("certChained");
    		init();
		p1.setVisibility(View.INVISIBLE);
        }else{
        	finish();
        }
        	
    }
	
	@SuppressLint("NewApi") 
	public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar_buttons, menu);
        
    	   SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
    	   
    	   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
               searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
       	   }else{ 
               searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
       	   }
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setIconifiedByDefault(false);
            
            m = menu.findItem(R.id.action_search);
            
            
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
				
            	public boolean onQueryTextSubmit(String query) {
                    MenuItem searchMenuItem = getSearchMenuItem();
                    if (searchMenuItem != null) {
                    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    		searchMenuItem.collapseActionView();
                    	}else{
                    		MenuItemCompat.collapseActionView(searchMenuItem);
                    	}
                        
                    }
                    return false;
                }
            	
                public boolean onQueryTextChange(String newText) {
                    return true;
                }
			});
      
       return super.onCreateOptionsMenu(menu);
    }
	
	@Override
	public boolean onKeyUp(int keycode, KeyEvent e) {
		if(keycode==KeyEvent.KEYCODE_MENU){
			showPopup();
        	return true;
		}else{
			return super.onKeyUp(keycode, e);
		}
	}
	
	private void showPopup(){
		View menuItemView = findViewById(R.id.action_overflow);
        PopupMenu popupMenu = new PopupMenu(this, menuItemView); 
        popupMenu.inflate(R.menu.overflow_menu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {  
            public boolean onMenuItemClick(MenuItem item) {
            	switch (item.getItemId()) {
            	case R.id.action_settings:
    	        	Intent intent_settings=new Intent(MainActivity.this,OptionsActivity.class);
    	        	intent_settings.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            		startActivity(intent_settings);
    	            return true;
            	case R.id.action_newgroup:
            		Intent intent_newgroup = new Intent(MainActivity.this,CreateGroupActivity.class);
            		intent_newgroup.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            		startActivity(intent_newgroup);
            		return true;
            	}  
             return true;  
            }  
           });
        popupMenu.show();
	}
	
	
	private void showList(JSONObject json){
		
		if(json!=null){
			
			try {
				main_list.clear();
				int x = 1;
				JSONObject res = json.getJSONObject("result");
				int size = res.length();
				while(x<=size){
					JSONObject json2 = res.getJSONObject("R"+x);
					
					String name = json2.getString("group_name");
					String guid = json2.getString("guid");
					String options = json2.getString("options");
					
					main_list.add(new Main_list_item(name,"",guid,"",options));
					
					x++;
				}
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		
		
		p1.setVisibility(View.INVISIBLE);
		main_ListView.setAdapter(new SafePoll_ListAdapter<Main_list_item>(ctx,R.layout.main_screen_list_row,main_list,"Main_list_item",null));
	}
	
	public MenuItem getSearchMenuItem(){
    	return m;
    }
	
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.action_search:
	            return true;
	        case R.id.action_overflow:
	        	showPopup();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		
	
	  if (v.getId()==R.id.main_screen_list) {
	    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
	    Main_list_item item = main_list.get(info.position);
	    
	    menu.setHeaderTitle("Grupo " + item.getName());
	    String[] menuItems = contextMenuOptions;
	    for (int i = 0; i<menuItems.length; i++) {
	      menu.add(Menu.NONE, i, i, menuItems[i]);
	    }
	  }
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	  AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
	  int menuItemIndex = item.getItemId();
	  String[] menuItems = contextMenuOptions;
	  String menuItemName = menuItems[menuItemIndex];
	  if(menuItemName == "Salir del grupo"){
		  list_item = main_list.get(info.position);
		  cm = new ConnectionManager(ctx, list_item.getID(),null,"WithdrawGroup");
		  cm.NetAsync(view, listener);
	  }else if(menuItemName == "Borrar grupo"){
		  list_item = main_list.get(info.position);
		  cm = new ConnectionManager(ctx, list_item.getID(),null,"DeleteGroup");
		  cm.NetAsync(view, listener);
	  }
	  
	  return true;
	}
	
	private void init(){
		ac = am.getAccountsByType("com.safepoll");
        if(ac!=null){
        	userName = ac[0].name;
    		tokenResponse = am.getAuthToken(ac[0], AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, null, this, null, null);
    		
    		
    		SharedPreferences pref = ctx.getSharedPreferences("SafePollSession", 0);
            Editor editor = pref.edit();
            editor.putString("CurrentUser", userName);
            editor.putBoolean("CertChained", certChained);
            editor.commit();
            certMan = new CertificateManager(this,userName);
    		
    		new AsyncTask<Void, Void, String>() {
    			
    			
                @Override
                protected String doInBackground(Void... params) {
                	
                	
            		try {
            			
            			Bundle b = tokenResponse.getResult();
            			token = b.getString(AccountManager.KEY_AUTHTOKEN);
            		} catch (OperationCanceledException e) {
            			e.printStackTrace();
            		} catch (AuthenticatorException e) {
            			e.printStackTrace();
            		} catch (IOException e) {
            			e.printStackTrace();
            		}
                    return token;
                }

                @Override
                protected void onPostExecute(String token) {
                	if (token != null){
                		init = false;
                		
                		cm = new ConnectionManager(ctx,userName,token,"getGroupsList");
                        cm.NetAsync(getWindow().getDecorView().findViewById(android.R.id.content),listener);
                    }
                	
                }
            }.execute();
        }	
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void onTaskCompleted() {
		if(!cm.Error()){
			if(init){
				switcher.showNext();
	    		actionBar.show();
	    		init = false;
	    		
        		SharedPreferences pref = ctx.getSharedPreferences("SafePollSession", 0);
                Editor editor = pref.edit();
                editor.putString("CurrentUser", userName);
                editor.commit();
                certChained = pref.getBoolean("CertChained", false);
                certMan = new CertificateManager(this,userName);
	    		
	    		cm = new ConnectionManager(ctx,userName,token,"getGroupsList");
	            cm.NetAsync(getWindow().getDecorView().findViewById(android.R.id.content),listener);
			}else{
				if((cm.getMode().equals("WithdrawGroup"))||(cm.getMode().equals("DeleteGroup"))){
					((SafePoll_ListAdapter<Main_list_item>)main_ListView.getAdapter()).remove(list_item);
					((SafePoll_ListAdapter<Main_list_item>)main_ListView.getAdapter()).notifyDataSetChanged();
				}else{
					showList(cm.getJson());
				}
				
			}
			
		}else if(cm.getMode() == "Session"){
			am.removeAccount(ac[0], null, null);
			
			
			Intent intent = new Intent(getBaseContext(),AuthenticatorActivity.class);
          	intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

          	Bundle extras = new Bundle();
			extras.putString(ARG_AUTH_TYPE, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS);
        	extras.putString(ARG_ACCOUNT_TYPE, "com.safepoll");
        	extras.putBoolean(ARG_IS_ADDING_NEW_ACCOUNT, true);
        	intent.putExtras(extras);
			
			startActivityForResult(intent, REQ_LOGIN);
			
		}
		p1.setVisibility(View.INVISIBLE);
		
	}
	
	@Override
	public void onPause(){
		super.onPause();
	}
	
	@Override
	public void onResume(){
		super.onResume();
		if(!init){
			cm = new ConnectionManager(ctx,userName,token,"getGroupsList");
	        cm.NetAsync(getWindow().getDecorView().findViewById(android.R.id.content),listener);
		}
		
	}
}
