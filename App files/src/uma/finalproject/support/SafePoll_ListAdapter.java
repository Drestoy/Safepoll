/**
 * @author Daniel Domínguez Restoy
 * @version 1.0
 */
package uma.finalproject.support;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.message.BasicNameValuePair;
import uma.finalproject.crypto.CertificateManager;
import uma.finalproject.database.ConnectionManager;
import uma.finalproject.database.OnTaskCompleted;
import uma.finalproject.safepoll.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class SafePoll_ListAdapter<T> extends ArrayAdapter<T>{
	
	private int resource;
    private LayoutInflater inflater;
    private Context context;
    private String type,user,userName,uid;
    private ConnectionManager cm;
    private View view;
    private CertificateManager certMan;
    private List<T> myList;
    private OnClickListener callback;
    private OnTaskCompleted listener;
    
    public SafePoll_ListAdapter(Context ctx, int resourceId, List<T> objects, String item_type, OnClickListener callback) {
		super(ctx, resourceId, objects);
		myList = objects;
		type = item_type;
		resource = resourceId;
        inflater = LayoutInflater.from( ctx );
        context=ctx;
        
        this.callback = callback;
        
        SharedPreferences pref = ctx.getSharedPreferences("SafePollSession", 0);
        userName = pref.getString("CurrentUser", "Error");
        uid = pref.getString("userID", "Error");
        
        certMan = new CertificateManager(null,userName);
	}
    
    public SafePoll_ListAdapter(Context ctx, int resourceId, List<T> objects, String item_type, OnClickListener callback, OnTaskCompleted listener){
    	super(ctx, resourceId, objects);
    	myList = objects;
		type = item_type;
		resource = resourceId;
        inflater = LayoutInflater.from( ctx );
        context=ctx;
        
        this.callback = callback;
        
        SharedPreferences pref = ctx.getSharedPreferences("SafePollSession", 0);
        userName = pref.getString("CurrentUser", "Error");
        
        certMan = new CertificateManager(null,userName);
        this.listener = listener;
    }
      
    public void notifyDataSetChanged(){
    	super.notifyDataSetChanged();
    }

	public View getView ( int position, View convertView, ViewGroup parent ) {
		view = convertView;
        convertView = (RelativeLayout) inflater.inflate(resource, null);
        
        if(type=="Main_list_item"){
        	Main_list_item item = (Main_list_item) getItem( position );
        	ImageView group_image = (ImageView) convertView.findViewById(R.id.group_Image);
        	group_image.setImageResource(R.drawable.ic_image_nogroup);
        	
            TextView group_name = (TextView) convertView.findViewById(R.id.group_Name);
            group_name.setText(item.getName());
        }else if(type =="Votes_list_item"){
        	Votes_list_item item = (Votes_list_item) getItem( position );
            TextView group_name = (TextView) convertView.findViewById(R.id.poll_Name);
            group_name.setText(item.getQuestion());
             
            TextView group_info1 = (TextView) convertView.findViewById(R.id.poll_Info1);
            group_info1.setText("Finaliza en: " + item.getEnd(6));
            
            TextView group_info2 = (TextView) convertView.findViewById(R.id.poll_Info2);
            if(item.getVote()!=""){
                group_info2.setText("Ya ha participado en esta votación");
            }else{
            	group_info2.setText("¡Todavía no ha participado!");
            }
        }else if(type =="History_list_item"){
        	Votes_list_item item = (Votes_list_item) getItem( position );
            TextView group_name = (TextView) convertView.findViewById(R.id.poll_Name);
            group_name.setText(item.getQuestion());
             
            TextView group_info = (TextView) convertView.findViewById(R.id.poll_Info1);
            group_info.setText("Finalizada en: " + item.getEnd(6));
        }else if (type == "Search_list_item"){
        	final Main_list_item item = (Main_list_item) getItem( position );
        	convertView.setOnClickListener ( callback );
        	convertView.setTag(position);
        	
        	
        	ImageView group_image = (ImageView) convertView.findViewById(R.id.group_Image);
        	group_image.setImageResource(R.drawable.ic_image_nogroup);
        	
            TextView group_name = (TextView) convertView.findViewById(R.id.group_Name);
            group_name.setText(item.getName());
             
            TextView group_info = (TextView) convertView.findViewById(R.id.group_Info);
            group_info.setText(item.getDesc());
            
            Button addButton = (Button) convertView.findViewById(R.id.add_group_button);
            
            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                	String options = item.getOptions();
                	if(options.charAt(0) == '0'){
                		//el grupo es publico
                		if(options.charAt(1) == '0'){
                			//no se requiere certificado
                			cm = new ConnectionManager(context,item.getID(),item.getName(),"userAddGroup");
                        	cm.NetAsync(view, null);
                        	myList.remove(item);
                			notifyDataSetChanged();
                		}else{
                			//se requiere certificado
                			if(certMan.isChained()){
                				cm = new ConnectionManager(context,item.getID(),item.getName(),"userAddGroup");
                            	cm.NetAsync(view, null);
                            	myList.remove(item);
                    			notifyDataSetChanged();
                			}else{
                				Toast.makeText(context, "Debe tener un certificado enlazado a su cuenta.", Toast.LENGTH_SHORT).show();
                			}
                			
                		}
                	}else{
                		//el grupo es privado
                		if(options.charAt(1) == '0'){
                			//no se requiere certificado
                			cm = new ConnectionManager(context,user, item.getID(),"userRequest");
                			cm.NetAsync(view, null);
                			Toast.makeText(context, "Petición enviada al grupo.", Toast.LENGTH_SHORT).show();
                			myList.remove(item);
                			notifyDataSetChanged();
                		}else{
                			//se requiere certificado
                			if(certMan.isChained()){
                				cm = new ConnectionManager(context,user, item.getID(),"userRequest");
                    			cm.NetAsync(view, null);
                    			myList.remove(item);
                    			notifyDataSetChanged();
                            	Toast.makeText(context, "Petición enviada al grupo.", Toast.LENGTH_SHORT).show();
                			}else{
                				Toast.makeText(context, "Debe tener un certificado enlazado a su cuenta.", Toast.LENGTH_SHORT).show();
                			}
                			
                		}
                	}
                	
                }
            });
        }else if (type == "Requests_list_item"){
        	final Users_list_item item = (Users_list_item) getItem( position );
        	convertView.setOnClickListener ( callback );
        	convertView.setTag(position);
        	
        	ImageView user_image = (ImageView) convertView.findViewById(R.id.user_Image);
        	user_image.setImageResource(R.drawable.ic_image_nogroup);
        	
            TextView user_name = (TextView) convertView.findViewById(R.id.user_Name);
            user_name.setText(item.getName());
             
            TextView user_info = (TextView) convertView.findViewById(R.id.user_Info);
            user_info.setText("Click para más información");
            
            ImageButton acceptButton = (ImageButton) convertView.findViewById(R.id.request_accept_button);
            acceptButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					new AlertDialog.Builder(context)
				    .setTitle("Confirmación")
				    .setMessage("¿Aceptar la solicitud de adimisión de " + item.getName() + "?")
				    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int which) { 
				        	List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
				            params.add(new BasicNameValuePair("user", item.getAccount()));
				            params.add(new BasicNameValuePair("guid", item.getGUID()));
				            params.add(new BasicNameValuePair("choice","true"));
				        	cm = new ConnectionManager(context,params,"decideRequest");
                			cm.NetAsync(view, null);
                			myList.remove(item);
                			notifyDataSetChanged();
				        }
				     })
				    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int which) { 
				            // do nothing
				        }
				     })
				    .setIcon(android.R.drawable.ic_dialog_alert)
				     .show();
				}
            	
            });
            
            ImageButton discardButton = (ImageButton) convertView.findViewById(R.id.request_discard_button);
            discardButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					new AlertDialog.Builder(context)
				    .setTitle("Confirmación")
				    .setMessage("¿Rechazar la solicitud de admisión de " + item.getName() + "?")
				    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int which) { 
				        	List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
				            params.add(new BasicNameValuePair("user", item.getAccount()));
				            params.add(new BasicNameValuePair("guid", item.getGUID()));
				            params.add(new BasicNameValuePair("choice","false"));
				        	cm = new ConnectionManager(context,params,"decideRequest");
                			cm.NetAsync(view, null);
                			myList.remove(item);
                			notifyDataSetChanged();
				        }
				     })
				    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int which) { 
				            // do nothing
				        }
				     })
				    .setIcon(android.R.drawable.ic_dialog_alert)
				     .show();
					
				}
            	
            });
        	
        }else if (type == "members_list_item"){
        	final Users_list_item item = (Users_list_item) getItem( position );
        	convertView.setOnClickListener ( callback );
        	convertView.setTag(position);
        	
        	ImageView user_image = (ImageView) convertView.findViewById(R.id.user_Image);
        	user_image.setImageResource(R.drawable.ic_image_nogroup);
        	
            TextView user_name = (TextView) convertView.findViewById(R.id.user_Name);
            user_name.setText(item.getName());
             
            TextView user_info = (TextView) convertView.findViewById(R.id.user_Info);
            user_info.setText("Click para más información");
            
            ImageButton upgradeButton = (ImageButton) convertView.findViewById(R.id.members_upgrade_button);
            ImageButton discardButton = (ImageButton) convertView.findViewById(R.id.members_discard_button);
            
            if((!userName.equals("error"))&&(userName.equals(item.getAccount()))){
            	upgradeButton.setVisibility(View.INVISIBLE);
            	discardButton.setVisibility(View.INVISIBLE);
            }else{
            	upgradeButton.setOnClickListener(new View.OnClickListener() {

    				@Override
    				public void onClick(View arg0) {
    					new AlertDialog.Builder(context)
    				    .setTitle("Confirmación")
    				    .setMessage("¿Convertir a " + item.getName() + " en administrador?(Usted dejará de ser el administrador)")
    				    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
    				        public void onClick(DialogInterface dialog, int which) { 
    				        	List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
    				            params.add(new BasicNameValuePair("user", item.getID()));
    				            params.add(new BasicNameValuePair("guid", item.getGUID()));
    				            params.add(new BasicNameValuePair("admAccount", uid));
    				        	cm = new ConnectionManager(context,params,"changeAdm");
                    			cm.NetAsync(view, listener);
    				        }
    				     })
    				    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
    				        public void onClick(DialogInterface dialog, int which) { 
    				            // do nothing
    				        }
    				     })
    				    .setIcon(android.R.drawable.ic_dialog_alert)
    				     .show();
    				}
                	
                });
                
                
                discardButton.setOnClickListener(new View.OnClickListener() {

    				@Override
    				public void onClick(View arg0) {
    					new AlertDialog.Builder(context)
    				    .setTitle("Confirmación")
    				    .setMessage("¿Echar del grupo a " + item.getName() + "?")
    				    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
    				        public void onClick(DialogInterface dialog, int which) { 
    				        	List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
    				            params.add(new BasicNameValuePair("user", item.getName()));
    				            params.add(new BasicNameValuePair("guid", item.getGUID()));
    				            params.add(new BasicNameValuePair("choice","false"));
    				            cm = new ConnectionManager(context, item.getID(),null,"WithdrawGroup");
    				  		    cm.NetAsync(view, null);
                    			myList.remove(item);
                    			notifyDataSetChanged();
    				        }
    				     })
    				    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
    				        public void onClick(DialogInterface dialog, int which) { 
    				            // do nothing
    				        }
    				     })
    				    .setIcon(android.R.drawable.ic_dialog_alert)
    				     .show();
    					
    				}
                	
                });
            }
            
            
            
        }else{
        	Users_list_item item = (Users_list_item) getItem( position );
            TextView group_name = (TextView) convertView.findViewById(R.id.group_Name);
            group_name.setText(item.getName());
             
            TextView group_info = (TextView) convertView.findViewById(R.id.group_Info);
            group_info.setText(item.getID());
        }
        
        return convertView;
	}
	
}
