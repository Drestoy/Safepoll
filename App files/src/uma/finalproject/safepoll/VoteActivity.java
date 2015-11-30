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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class VoteActivity extends ActionBarActivity implements OnTaskCompleted{
	private String question, iD, description, answers, start, end, gUID;
	private TextView questionTV, descTV, foot;
	private RadioGroup rg;
	private int num_answers = 0, selected_answer=0;
	private ConnectionManager cm;
	private Context ctx;
	private OnTaskCompleted listener;
	private boolean signRequired;
	
	private CertificateManager certMan;
	
	public void onCreate(Bundle savedInstanceState) {
	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.votinglayout);
        
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        ctx = this;
        listener = this;
        
        Bundle group_info = getIntent().getExtras();
        question = group_info.getString("Question");
        iD = group_info.getString("ID");
        description = group_info.getString("Description");
        answers = group_info.getString("Answers");
        start = group_info.getString("Start");
        end = group_info.getString("End");
        gUID = group_info.getString("GUID");
        signRequired = group_info.getBoolean("signRequired");
        
        SharedPreferences pref = ctx.getSharedPreferences("SafePollSession", 0);
        String userName = pref.getString("CurrentUser", "Error");
        
        if(userName!="Error"){
        	certMan = new CertificateManager(this, userName);
        }else{
        	Toast.makeText(getApplicationContext(), "Fallo al obtener id de usuario.", Toast.LENGTH_SHORT).show();
			finish();;
        }
        
        rg = (RadioGroup) findViewById(R.id.radioGroup1);
        
        questionTV = (TextView) findViewById(R.id.vote_question);
        descTV = (TextView) findViewById(R.id.vote_text);
        foot = (TextView) findViewById(R.id.vote_foot);
        
        questionTV.setText(question);
        descTV.setText(description);
        foot.setText("Votación iniciada el: " + start + "\nTermina el: " + end); 
        
        Button vote_send = (Button)findViewById(R.id.vote_sendButton);
        vote_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	if(selected_answer!=0){
            		
            		new AsyncTask<Void, Void, Boolean>() {
            			private int error = 0;
            			
						@Override
						protected Boolean doInBackground(Void... arg0) {
							String vote = String.valueOf(selected_answer) + "-" +getAnswer(selected_answer);
							List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
			                params.add(new BasicNameValuePair("poll", iD));
			                params.add(new BasicNameValuePair("guid",gUID ));
			                params.add(new BasicNameValuePair("vote",vote ));
			                if(signRequired){
			                    if(certMan.isChained()){
			                    	String sign = certMan.sign(vote);
			                    	if((sign!=null)&&(sign!="")){
			                    		params.add(new BasicNameValuePair("sign",sign));
				                    	cm = new ConnectionManager(ctx,params,"sendVote");
										return true;
			                    	}else{
			                    		error = 1;
			                    		return false;
			                    	}
			                    	
			                    }else{
			                    	error = 2;
									return false;
								}
			                    	
			                }else{
			                    params.add(new BasicNameValuePair("sign",""));
			                    cm = new ConnectionManager(ctx,params,"sendVote");
								return true;
			                }
			                    
							
							
						}
						
						protected void onPostExecute(Boolean valid) {
							if(valid){
								startCM();
							}else{
								if(error == 1){
									Toast.makeText(ctx, "Fallo al firma su voto. Reinstale su certificado digital.", Toast.LENGTH_SHORT).show();
								}else{
									Toast.makeText(ctx, "Debe tener un certificado enlazado a la cuenta para poder votar.", Toast.LENGTH_SHORT).show();
								}
							}
						}
            			
            		}.execute();
            	}else{
            		Toast.makeText(ctx, "No se ha elegido ninguna opción", Toast.LENGTH_SHORT).show();
            	}
            	
            }
        });
        writeAnswers(answers);      
    }
	
	private void startCM(){
		if(signRequired){
			new AlertDialog.Builder(ctx)
		    .setTitle("Firma digital obligatoria")
		    .setMessage("Para esta votación se requiere usar su certificado digital. ¿Está de acuerdo?")
		    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) { 
		        	cm.NetAsync(getWindow().getDecorView().findViewById(android.R.id.content), listener);
		        }
		     })
		    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) { 
		            // do nothing
		        }
		     })
		    .setIcon(android.R.drawable.ic_dialog_alert)
		     .show();
		}else{
			cm.NetAsync(getWindow().getDecorView().findViewById(android.R.id.content), listener);
		}
		
	}
	
	OnClickListener radio_listener = new OnClickListener() {
	    public void onClick(View v) {
	        onRadioButtonClicked(v);
	    }
	};
	
	public void onRadioButtonClicked(View view) {
	    int x=0;
	    boolean found = false;
	    while((!found)&&(x<rg.getChildCount())){
	    	View child = rg.getChildAt(x); 
	    	if((child!=null)&&(((RadioButton)child).isChecked())){
	    		selected_answer = x+1;
	    	}
	    	x++;
	    }
	}
	
	
	
	private void writeAnswers(String answers){
		String[] res = null;
		int index = 0,i=0;
		for(int x=0;x<answers.length();x++){
			if((answers.charAt(x)==';')&&(answers.length()-x>=7)&&(answers.substring(x, x+7).equals(";SFP%L;"))){
				if(index==0){
					num_answers = Integer.parseInt(answers.substring(index, x));
					res = new String[num_answers];
					index = x+7;
				}else{
					res[i] = answers.substring(index, x);
					i = i+1;
					index = x+7;
				}
				
				
			}
		}
		res[i]= answers.substring(index, answers.length());
		
		for(int x=0;x<num_answers;x++){
			RadioButton rb = new RadioButton(this);
		    rb.setText(res[x]);
		    rb.setOnClickListener(radio_listener);
		    rg.addView(rb);
		}
		
	}

	private String getAnswer(int position){
		RadioButton rb = (RadioButton)rg.getChildAt(position-1);
		return rb.getText().toString();
	}


	@Override
	public void onTaskCompleted() {
		finish();
	}
	
	public void onPause() {
	     super.onPause();
	     overridePendingTransition(0, 0);
	 }
}
