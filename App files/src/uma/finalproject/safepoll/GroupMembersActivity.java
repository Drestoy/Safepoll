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
import uma.finalproject.support.SafePoll_ListAdapter;
import uma.finalproject.support.Users_list_item;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

public class GroupMembersActivity extends ActionBarActivity implements OnTaskCompleted, OnClickListener{
	private List<Users_list_item> group_members_list;
	private ListView members_ListView;
	private ConnectionManager cm;
	private String groupID;
	private Context ctx;
	private OnTaskCompleted listener;
	private boolean init = true;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.requests_layout);
		
		ctx = this;
		listener = this;
		
		Bundle group_info = getIntent().getExtras();
        groupID = group_info.getString("guid");
		
		group_members_list = new ArrayList<Users_list_item>();
		
		members_ListView = (ListView) findViewById(R.id.requests_screen_list);
		        
        cm = new ConnectionManager(this,groupID,"","getMembers");
        cm.NetAsync(getWindow().getDecorView().findViewById(android.R.id.content), this);
	
	}
	
	public void onClick(View view){
		showPopup(this, view);
	}
	
	@SuppressWarnings("deprecation")
	private void showPopup(final Activity context, View view) {
		   int popupWidth = 200;
		   int popupHeight = 200;
		 
		   LinearLayout viewGroup = (LinearLayout) context.findViewById(R.id.popup);
		   LayoutInflater layoutInflater = (LayoutInflater) context
		     .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
		   
		   field1.setText(((Users_list_item)group_members_list.get(pos)).getName());
		   field2.setText(((Users_list_item)group_members_list.get(pos)).getAccount());
		   if(((Users_list_item)group_members_list.get(pos)).hasCertificate()){
			   field3.setText("\nInformación del certificado:\n\n" + ((Users_list_item)group_members_list.get(pos)).getCertificate());
		   }else{
			   field3.setText("Este usuario no tiene certificado.");
		   }
		   
		   layout.setOnClickListener(new LinearLayout.OnClickListener(){

			@Override
			public void onClick(View arg0) {
				popup.dismiss();
			}
			   
		   });
				   
		   
		}

	@Override
	public void onTaskCompleted() {
		if(!cm.Error()){
			if(init){
				try {
					JSONObject json = cm.getJson().getJSONObject("result");
					
					int x = 1;
					int size = json.length();
					while(x<=size){
						JSONObject json2 = json.getJSONObject("R"+x);
						
						String uid = json2.getString("unique_id");
						String firstname = json2.getString("firstname");
						String lastname = json2.getString("lastname");
						boolean useCert = json2.getBoolean("subject");
						String mail = json2.getString("client_id");
						String cert = json2.getString("4");
						
						group_members_list.add(new Users_list_item(firstname + " " + lastname,"PIC",uid,groupID,mail,useCert,cert));
						
						x++;
					}
					
					
					
					
																		 
					members_ListView.setAdapter(new SafePoll_ListAdapter<Users_list_item>(ctx,R.layout.group_members_list_row,group_members_list,"members_list_item",this,listener));
					
					ProgressBar pBar = (ProgressBar) findViewById(R.id.progressbar_requests);
					pBar.setVisibility(View.INVISIBLE);
					init = false;
					
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}else{
				finish();
			}
			
		}
	}
}
