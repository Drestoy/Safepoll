/**
 * @author Daniel Domínguez Restoy
 * @version 1.0
 */

package uma.finalproject.safepoll;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import uma.finalproject.crypto.CertificateManager;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;
import android.widget.Toast;

public class HistoryActivity extends ActionBarActivity{
	private String question, description, answers, start, end, vote, sign, overall, userName, res;
	private boolean signRequired = false;
	private TextView questionTV, descTV, head, results;
	private CertificateManager certMan;
	
	public void onCreate(Bundle savedInstanceState) {
	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pollinfolayout);
        
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        SharedPreferences pref = this.getSharedPreferences("SafePollSession", 0);
        userName = pref.getString("CurrentUser", "Error");
        
        if(userName!="Error"){
        	certMan = new CertificateManager(this, userName);
        }else{
        	Toast.makeText(getApplicationContext(), "Fallo al obtener id de usuario.", Toast.LENGTH_SHORT).show();
			finish();
        }
        
        Bundle group_info = getIntent().getExtras();
        question = group_info.getString("Question");
        description = group_info.getString("Description");
        answers = group_info.getString("Answers");
        start = group_info.getString("Start");
        end = group_info.getString("End");
        vote = group_info.getString("Vote");
        sign = group_info.getString("Sign");
        overall = group_info.getString("Overall");
        signRequired = group_info.getBoolean("SignRequired");
        
        questionTV = (TextView) findViewById(R.id.history_question);
        descTV = (TextView) findViewById(R.id.history_text);
        head = (TextView) findViewById(R.id.history_head);
        results = (TextView) findViewById(R.id.history_results);
        
        questionTV.setText(question);
        descTV.setText(description);
        head.setText("Fecha de inicio: " + start + "\nFecha de finalización: " + end);
        
        new AsyncTask<Void, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(Void... arg0) {
				int num_answers = 0;
		    	res = "Posibles respuestas: \n";
		    	int index = 0,i=1;
		    	String[] results = null;
		    	for(int x=0;x<answers.length();x++){
		    		if((answers.charAt(x)==';')&&(answers.length()-x>=7)&&(answers.substring(x, x+7).equals(";SFP%L;"))){
		    			if(index==0){
		    				num_answers = Integer.parseInt(answers.substring(index, x));
		    				results = new String[num_answers];
		    				index = x+7;
		    			}else{
		    				res = res + i + "- " + answers.substring(index, x) + "\n";
		    				results[i-1] = i + "- " + answers.substring(index, x);
		    				i = i+1;
		    				index = x+7;
		    			}
		    		}
		    	}
		    	res = res + i + "- " + answers.substring(index, answers.length()) + "\n";
		    	results[i-1] = i + "- " + answers.substring(index, answers.length());
		    	
		    	if(vote.equals("")){
		    		res = res + "\nUsted no participó en esta votación.\n\n";
		    	}else{
		    		if(sign.equals("")){
		    			res = res+ "\nUsted eligió:"+ vote +".\n\nEl voto no se firmó digitalmente.\n\n";
		    		}else{
		    			if(certMan.verify(vote, certMan.getPublicKey(certMan.getAlias()), sign)){
		    				res = res + "\nUsted eligió: "+ vote +".\n\nSu voto se verificó con la firma digital.\n\n";
		    			}else{
		    				res = res + "\nUsted eligió: "+ vote +".\n\nSu voto no se ha verificado con la firma digital.\n\n";
		    			}
		    			
		    		}
		    		
		    	}
				
		    	int[] answers_Array = new int[num_answers];
		    	for(int x=0;x<answers_Array.length;x++){
		    		answers_Array[x]=0;
		    	}
		    	index = 0;
		    	i=1;
		    	int total_votes = 0;
		    	boolean allVerified = true;
		    	for(int x=0;x<overall.length();x++){
		    		if(overall.charAt(x)==';'){
		    			if(index>0){
		    				if(i==1){
		    					answers_Array[getVote(overall.substring(index, x))-1] = answers_Array[getVote(overall.substring(index, x))-1] + 1;
		    					vote = overall.substring(index, x);
		    					total_votes++;
		    					index = x+1;
		    					i=2;
		    				}else{
		    					if(signRequired){
		    						if(i==2){
		    							sign = overall.substring(index, x);
		    							index = x+1;
		    							i=3;
		    						}else{
		    							
		    							
		    							byte[] pkbyte = certMan.stringToByte(overall.substring(index, x));
				   			        	X509EncodedKeySpec spec = new X509EncodedKeySpec(pkbyte);
				   			        	KeyFactory keyFactory;
										try {
											keyFactory = KeyFactory.getInstance("RSA");
											PublicKey key = keyFactory.generatePublic(spec);
											certMan.verify(vote, key, sign);
										} catch (NoSuchAlgorithmException e) {
											allVerified = false;
											e.printStackTrace();
										} catch (InvalidKeySpecException e) {
											allVerified = false;
											e.printStackTrace();
										}
		    							index = x+1;
		    							i=1;
		    						}
		    					}else{
		    						if(i==2){
		    							index = x+1;
		    							i=3;
		    						}else{
		    							index = x+1;
		    							i=1;
		    						}
		    					}
		    				}
		    			}else{
		    				index = x+1;
		    			}
		    		}
		    	}
		    	res = res + "Resultados de la votación: \n\n";
		    	if(total_votes>0){
		    		for(int x=0;x<answers_Array.length;x++){
			    		res = res + results[x] + ":" + answers_Array[x] + " (" + ((answers_Array[x]*100)/total_votes) + "%)\n";
			    	}
		    	}else{
		    		res = res + "Esta votación no ha tenido participación.\n";
		    	}
		    	
				
		    	if((allVerified)&&(signRequired)){
		    		res = res + "\nTodas las firmas han sido verificadas.\n";
		    	}
				return true;
			}
			
			protected void onPostExecute(Boolean valid) {
				results.setText(res);
			}
        	
        }.execute();
       
    }
	
	private int getVote(String vote){
		int x = 0;
		while((x<vote.length())&&(vote.charAt(x)!='-')){
			x++;
		}
		
		if(x<vote.length()){
			return Integer.parseInt(vote.substring(0, x));
		}else{
			return -1;
		}
	}
	
	public void onPause() {
	     super.onPause();
	     overridePendingTransition(0, 0);
	 }
}
