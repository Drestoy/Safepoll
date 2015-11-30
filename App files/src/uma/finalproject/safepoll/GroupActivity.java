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
import uma.finalproject.support.SafePollFragmentPagerAdapter;
import uma.finalproject.support.Votes_list_item;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.PopupMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

public class GroupActivity extends ActionBarActivity implements OnTaskCompleted{
	
	SafePollFragmentPagerAdapter mAdapter;
	ViewPager mViewPager;
	String groupID;
	
	
	private OnTaskCompleted listener;
	private ConnectionManager cm;
	private List<Votes_list_item> polls_list, history_list;
	private ActionBar actionBar; 
	private boolean init = true, isAdm = false;
	private String options = "";
	private int numRequests = 0;
	private String uid;
	Context ctx = this;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grouplayout);
                
        Bundle group_info = getIntent().getExtras();
        String Nombre = group_info.getString("Nombre");
        groupID = group_info.getString("ID");
        options = group_info.getString("options");
        
        actionBar = getSupportActionBar();
        actionBar.setTitle(Nombre);
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        SharedPreferences pref = ctx.getSharedPreferences("SafePollSession", 0);
        uid = pref.getString("userID", "Error");
        
        mAdapter = new SafePollFragmentPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        getSupportActionBar().setSelectedNavigationItem(position);
                    }
                });
        
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        ActionBar.TabListener tabAbiertosListener = new ActionBar.TabListener() {
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
            }

            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
            }
        };
        
        ActionBar.TabListener tabCerradosListener = new ActionBar.TabListener() {
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
            	mViewPager.setCurrentItem(tab.getPosition());
            }

            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
            }

            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
            }
        };

        actionBar.addTab(actionBar.newTab().setText("Votaciones").setTabListener(tabAbiertosListener));
        actionBar.addTab(actionBar.newTab().setText("Historial").setTabListener(tabCerradosListener));
        
        listener = this;
        
        cm = new ConnectionManager(this,groupID,null,"getPollsList");
        cm.NetAsync(getWindow().getDecorView().findViewById(android.R.id.content), listener);
        history_list = new ArrayList<Votes_list_item>();
        polls_list = new ArrayList<Votes_list_item>();
        
    }
	
	public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar_groups_buttons, menu);
        
        
       
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
        popupMenu.inflate(R.menu.overflow_groups_menu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {  
            public boolean onMenuItemClick(MenuItem item) {
            	switch (item.getItemId()) {
            	case R.id.action_settings:
    	        	Intent intent_settings=new Intent(GroupActivity.this,OptionsActivity.class);
            		startActivity(intent_settings);
    	            return true;
            	}  
             return true;  
            }  
           });
        popupMenu.show();
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.action_newpoll:
	        	Intent intent=new Intent(GroupActivity.this,NewPoll.class);
	        	intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
	        	
	        	Bundle group_data = new Bundle();
            	group_data.putString("guid", groupID);
            	group_data.putString("options", options);
        		intent.putExtras(group_data);
	        	
        		startActivity(intent);
	            return true;
	        case R.id.action_overflow:
	        	showPopup();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	protected String getGUID(){
		return groupID;
	}

	@Override
	public void onTaskCompleted() {
		if(!cm.Error()){
			JSONObject json = cm.getJson();
			int x = 1;
			try {
				
				int size = 0;
				JSONObject res = null;
				if(json.get("result").getClass()!=Boolean.class){
					res = json.getJSONObject("result");
					size = res.length();
				}
				
				int size2;
				JSONObject res2 = null;
				if(json.get("result2").getClass()==Boolean.class){
					size2 = 0;
				}else{
					res2 = json.getJSONObject("result2");
					size2 = res2.length();
				}
				
				isAdm = json.getBoolean("result3");
				if(isAdm){
					LinearLayout buttonBar = (LinearLayout) findViewById(R.id.buttonbar);
					
					buttonBar.setVisibility(View.VISIBLE);
					
					ImageButton admSettings = (ImageButton) findViewById(R.id.adm_group_options);
					admSettings.setOnClickListener(new ImageButton.OnClickListener() {
			        	public void onClick(View v) {
			        		Intent intent=new Intent(GroupActivity.this,GroupOptionsActivity.class);
			        		
			        		Bundle extras = new Bundle();
		        			extras.putString("guid", groupID);
		        			extras.putString("options", options);
		                	intent.putExtras(extras);
			        		
			        		startActivity(intent);
			    		}
			        }
			        );
					
					ImageButton admRequests = (ImageButton) findViewById(R.id.adm_group_requests);
					admRequests.setOnClickListener(new ImageButton.OnClickListener() {
			        	public void onClick(View v) {
			        		if(numRequests>0){
			        			Intent intent=new Intent(GroupActivity.this,GroupRequestsActivity.class);
				        		
				        		Bundle extras = new Bundle();
			        			extras.putString("guid", groupID);
			                	intent.putExtras(extras);
				        		
				        		startActivity(intent);
			        		}else{
			        			Toast.makeText(ctx, "No hay peticiones pendientes en este grupo.", Toast.LENGTH_SHORT).show();
			        		}
			        		
			    		}
			        }
			        );
					
					ImageButton admMembers = (ImageButton) findViewById(R.id.adm_group_members);
					admMembers.setOnClickListener(new ImageButton.OnClickListener() {
			        	public void onClick(View v) {
			        		Intent intent=new Intent(GroupActivity.this,GroupMembersActivity.class);
			        		
			        		Bundle extras = new Bundle();
		        			extras.putString("guid", groupID);
		                	intent.putExtras(extras);
			        		
			        		startActivity(intent);
			    		}
			        }
			        );
					
					numRequests = json.getInt("result5");
				}
				
				options = json.getString("result4");
				
				while(x<=size){
					int y = 1;
					JSONObject json2 = res.getJSONObject("R"+x);
					
					String question = json2.getString("question");
					String id = json2.getString("id");
					String desc = json2.getString("description");
					String answers = json2.getString("possible_answers");
					String start = json2.getString("start");
					String finish = json2.getString("finished");
					boolean isFinished = json2.getBoolean("isFinished");
					boolean signRequired = json2.getBoolean("signRequired");
					String vote = "";
					String overallVotes = "";
					String sign = "";
					
					while(y<=size2){
						JSONObject json3 = res2.getJSONObject("R"+y);
						if(json3.getString("poll_id").equals(id)){
							if(json3.getString("user_id").equals(uid)){
								vote = json3.getString("answer");
								sign = json3.getString("sign");
							}
							overallVotes = overallVotes + ";" + json3.getString("answer") + ";" + json3.getString("sign") + ";" + json3.getString("public_key");
						}
						y++;
					}
					
					if(isFinished){
						history_list.add(new Votes_list_item(question, desc, answers, start, finish,id, vote, sign, overallVotes, isFinished, signRequired));
					}else{
						polls_list.add(new Votes_list_item(question, desc, answers, start, finish,id, vote, sign, overallVotes, isFinished, signRequired));
					}
					x++;
				}
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
			VotesFragment v = (VotesFragment) mAdapter.getItem(0);
			v.receiveData(polls_list);
			HistoryFragment h = (HistoryFragment) mAdapter.getItem(1);
			h.receiveData(history_list);
		}
		init = false;
		
	}
	
	@Override
	public void onResume(){
		super.onResume();
		if(!init){
			LinearLayout buttonBar = (LinearLayout) findViewById(R.id.buttonbar);
			buttonBar.setVisibility(View.INVISIBLE);
			history_list = new ArrayList<Votes_list_item>();
	        polls_list = new ArrayList<Votes_list_item>();
			cm = new ConnectionManager(this,groupID,null,"getPollsList");
	        cm.NetAsync(getWindow().getDecorView().findViewById(android.R.id.content), listener);
		}
	}
	
	
	public void onPause() {
	     super.onPause();
	     overridePendingTransition(0, 0);
	 }

}
