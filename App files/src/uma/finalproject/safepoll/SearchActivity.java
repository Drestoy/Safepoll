/**
 * @author Daniel Domínguez Restoy
 * @version 1.0
 */

package uma.finalproject.safepoll;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import uma.finalproject.database.ConnectionManager;
import uma.finalproject.database.OnTaskCompleted;
import uma.finalproject.support.Main_list_item;
import uma.finalproject.support.SafePoll_ListAdapter;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SearchActivity extends ActionBarActivity implements OnTaskCompleted, OnClickListener{
	
	private ListView search_ListView;
    private Context ctx;
    private List<Main_list_item> group_search_list;
    
    private SearchView searchView;
    
    private MenuItem m;
    private OnTaskCompleted listener;
    
    private ConnectionManager cm;
    
    private ProgressBar pBar;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.searchlayout);
        
        ctx=this; 
        group_search_list = new ArrayList<Main_list_item>();
        listener = this;
        
        pBar = (ProgressBar) findViewById(R.id.progressbar_search);
        
        search_ListView = (ListView) findViewById(R.id.search_screen_list);
        
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Buscar grupos");
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        handleIntent(getIntent());
	}
	
	protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }
	
	private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            cm = new ConnectionManager(ctx,query,"","groupSearch");
            cm.NetAsync(getWindow().getDecorView().findViewById(android.R.id.content),listener);
        }
    }
	
	public void onClick(View view){
		showPopup(this, view);
	}
	
	@SuppressWarnings("deprecation")
	private void showPopup(final Activity context, View view) {
		   int popupWidth = 200;
		   int popupHeight = 200;
		 
		   LinearLayout viewGroup = (LinearLayout) context.findViewById(R.id.popup);
		   LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		   View layout = layoutInflater.inflate(R.layout.popup_layout, viewGroup);
		 
		   final PopupWindow popup = new PopupWindow(context);
		   popup.setContentView(layout);
		   popup.setFocusable(true);
		   popup.setBackgroundDrawable(new BitmapDrawable());
		 
		   Display display = getWindowManager().getDefaultDisplay();
		   Point size = new Point();
		   display.getSize(size);
		   popupWidth = size.x - (size.x/8);
		   popupHeight = size.y/2;
		   
		   popup.setWidth(popupWidth);
		   popup.setHeight(popupHeight);
		   int OFFSET_X = (15*popupWidth)/100;
		   int OFFSET_Y = (10*popupHeight)/100;
		   
		   popup.showAtLocation(layout, Gravity.CENTER, OFFSET_X, OFFSET_Y);
		   
		   TextView field1 = (TextView) layout.findViewById(R.id.popup_field1);
		   TextView field2 = (TextView) layout.findViewById(R.id.popup_field2);
		   TextView field3 = (TextView) layout.findViewById(R.id.popup_field3);
		   
		   int pos = (Integer)view.getTag();
		   
		   field1.setText(((Main_list_item)group_search_list.get(pos)).getName());
		   field3.setText(((Main_list_item)group_search_list.get(pos)).getDesc());
		   String options =((Main_list_item)group_search_list.get(pos)).getOptions(); 
		   
		   if(options.charAt(0) == '1'){
			   field2.setText("Grupo privado.");
		   }else{
			   field2.setText("Grupo público.");
		   }
		   if(options.charAt(1) == '1'){
			   field2.setText(field2.getText() + "\nSe requiere tener un certificado digital.");
		   }else{
			   field2.setText(field2.getText() + "\nNo se requiere certificado digital.");
		   }
		   
		   layout.setOnClickListener(new LinearLayout.OnClickListener(){

			@Override
			public void onClick(View arg0) {
				popup.dismiss();
			}
			   
		   });
				   
		   
		}
	
	public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar_search_buttons, menu);
        
        
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
	
	public MenuItem getSearchMenuItem(){
    	return m;
    }

	@Override
	public void onTaskCompleted() {
		group_search_list.clear();
		if(!cm.Error()){
			JSONObject json = cm.getJson();
			int x = 1;
			try {
				JSONObject res = json.getJSONObject("result");
				int size = res.length();
				while(x<=size){
					JSONObject json2 = res.getJSONObject("R"+x);
					
					String name = json2.getString("group_name");
					String picture = json2.getString("picture");
					String guid = json2.getString("guid");
					String desc = json2.getString("description");
					String options = json2.getString("options");
					
					
					group_search_list.add(new Main_list_item(name,picture,guid,desc,options));
					x++;
				}
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			search_ListView.setAdapter(new SafePoll_ListAdapter<Main_list_item>(ctx,R.layout.search_group_list_row,group_search_list,"Search_list_item",this));
			
		}
		pBar.setVisibility(View.INVISIBLE);
	}
	
	public void onPause() {
	     super.onPause();
	     overridePendingTransition(0, 0);
	 }
        
}
