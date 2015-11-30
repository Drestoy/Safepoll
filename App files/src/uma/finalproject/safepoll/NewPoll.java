/**
 * @author Daniel Domínguez Restoy
 * @version 1.0
 */

package uma.finalproject.safepoll;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.apache.http.message.BasicNameValuePair;

import uma.finalproject.crypto.CertificateManager;
import uma.finalproject.database.ConnectionManager;
import uma.finalproject.database.OnTaskCompleted;
import uma.finalproject.support.DatePickerFragment;
import uma.finalproject.support.TimePickerFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class NewPoll extends ActionBarActivity implements OnTaskCompleted{
	
	private ConnectionManager cm;
	private EditText desc, question;
	private String answers, options, daytime;
	private List<EditText> answers_list;
	private ImageView imageAdd,imageRemove;
	private RelativeLayout rl;
	private Context ctx;
	private int hour,minute,year,month,day;
	private Button timeButton, dateButton;
	private Calendar c;
	private OnTaskCompleted listener;
	private String guid;
	private CheckBox requireSign;
	private CertificateManager certMan;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newpoll_layout);
        
        ctx = this;
        listener = this;
        
        Bundle group_info = getIntent().getExtras();
        guid = group_info.getString("guid");
        options = group_info.getString("options");
        
        desc = (EditText) findViewById(R.id.newpoll_description);
        question = (EditText) findViewById(R.id.newpoll_question);
        
        requireSign = (CheckBox) findViewById(R.id.newpoll_signed);
        if(options.charAt(1) == '1'){
        	requireSign.setChecked(true);
        	requireSign.setClickable(false);
        }
        
        SharedPreferences pref = ctx.getSharedPreferences("SafePollSession", 0);
        String userName = pref.getString("CurrentUser", "Error");
        certMan = new CertificateManager(this,userName);
        
        timeButton = (Button) findViewById(R.id.showTimePicker_button);
        dateButton = (Button) findViewById(R.id.showDatePicker_button);
        
        c = Calendar.getInstance();
		hour = c.get(Calendar.HOUR_OF_DAY);
		minute = c.get(Calendar.MINUTE);
		year = c.get(Calendar.YEAR);
		month = c.get(Calendar.MONTH) + 1;
		day = c.get(Calendar.DAY_OF_MONTH);
		showTime();
		showDate();
        
        answers_list = new ArrayList<EditText>();
        
        rl = (RelativeLayout) findViewById(R.id.answers_layout);
        
        imageAdd = new ImageView(this);
        imageAdd.setImageResource(R.drawable.ic_action_new);
        
        imageRemove = new ImageView(this);
        imageRemove.setImageResource(R.drawable.ic_action_remove);
        
        addEditText();
        addEditText();
        
        imageAdd.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				addEditText();
			}
		});
        
        imageRemove.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				removeEditText();
			}
		});
        	    
        Button createPoll = (Button) findViewById(R.id.newpoll_Button);
        createPoll.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				String qString = question.getText().toString();
				String descString = desc.getText().toString();
				answers = listAnswers();
				if((!qString.equals(""))&&(!descString.equals(""))&&(!answers.equals(""))&&validateTimeDate()){
					
					if((!DateFormat.is24HourFormat(ctx))&&(daytime.equals("PM"))){
						hour = hour + 12;
					}
					String timedate = String.format(year +"-"+"%02d"+"-"+"%02d %02d"+":"+"%02d:00", month, day, hour, minute);
					List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
                    params.add(new BasicNameValuePair("question", qString));
                    params.add(new BasicNameValuePair("desc",descString ));
                    params.add(new BasicNameValuePair("answers",answers ));
                    params.add(new BasicNameValuePair("timedate",timedate ));
                    params.add(new BasicNameValuePair("guid", guid));
                    if(requireSign.isChecked()){
                    	if(certMan.isChained()){
                    		params.add(new BasicNameValuePair("signRequired", "true"));
                    		cm = new ConnectionManager(ctx,params,"createPoll");
        					cm.NetAsync(v, listener);
                    	}else{
                    		Toast.makeText(ctx, "No se puede exigir el uso de certificados si usted no usa uno.", Toast.LENGTH_SHORT).show();
                    	}
                    }else{
                    	params.add(new BasicNameValuePair("signRequired", "false"));
                    	cm = new ConnectionManager(ctx,params,"createPoll");
    					cm.NetAsync(v, listener);
                    }
				}else if(qString.equals("")){
					Toast.makeText(ctx, "No se ha escrito una pregunta", Toast.LENGTH_SHORT).show();
				}else if(descString.equals("")){
					Toast.makeText(ctx, "No existe descripción", Toast.LENGTH_SHORT).show();
				}else if(answers.equals("")){
					Toast.makeText(ctx, "Faltan campos de respuesta por rellenar.", Toast.LENGTH_SHORT).show();
				}else{
					Toast.makeText(ctx, "Fecha límite incorrecta", Toast.LENGTH_SHORT).show();
				}
			}});
	}
	
	private String listAnswers(){
		String result = String.valueOf(answers_list.size());
		String elem = "";
		int x = 0;
		while(x<answers_list.size()){
			elem = answers_list.get(x).getText().toString();
			if(elem.equals("")){
				result = "";
				x = answers_list.size();
			}else{
				result = result + ";SFP%L;" + elem;
			}
			x++;
		}
		
		return result;
	}
	
	private boolean validateTimeDate(){
		boolean res = false;
		int tmp = hour;
		
		c = Calendar.getInstance();
		
		if((!DateFormat.is24HourFormat(this))&&(daytime.equals("PM"))){
			tmp = hour + 12;
		}
		
		if(year > c.get(Calendar.YEAR)){
			res = true;
		}else if (year == c.get(Calendar.YEAR)){
			if(month > c.get(Calendar.MONTH)+1){
				res = true;
			}else if(month == c.get(Calendar.MONTH)+1){
				if(day > c.get(Calendar.DAY_OF_MONTH)){
					res = true;
				}else if(day == c.get(Calendar.DAY_OF_MONTH)){
					if(tmp > c.get(Calendar.HOUR_OF_DAY)){
						res = true;
					}else if(tmp == c.get(Calendar.HOUR_OF_DAY)){
						if(minute > c.get(Calendar.MINUTE)){
							res = true;
						}
					}
				}
			}
		}
		return res;
	}
	
	private void addEditText(){
		
        
        EditText et = new EditText(this);
                
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        if(answers_list.size()>=1){
	    	params.addRule(RelativeLayout.BELOW,answers_list.get(answers_list.size()-1).getId());
	    }
        params.setMargins(0, 0, 0, 15);
	    
	    et.setHint(R.string.newpoll_add_answer);
	    et.setEms(10);
	    et.setId(answers_list.size()+1);
	    et.setMaxLines(3);
	    et.setLayoutParams(params);
	    
	    answers_list.add(et);
	    rl.addView(answers_list.get(answers_list.size()-1));
	    
	    
	    printButtons();
	}
	
	private void printButtons(){
		
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
	    if(answers_list.size()>1){
	    	params.addRule(RelativeLayout.BELOW,answers_list.get(answers_list.size()-2).getId());
	    	rl.removeView(imageAdd);
	    }
	    params.addRule(RelativeLayout.LEFT_OF, answers_list.get(answers_list.size()-1).getId());
	    imageAdd.setLayoutParams(params);
	    imageAdd.setId(answers_list.size()+3);
	    rl.addView(imageAdd);
	    
	    if(answers_list.size()>2){
	    	params = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		    	params.addRule(RelativeLayout.BELOW,answers_list.get(answers_list.size()-2).getId());
		    	rl.removeView(imageRemove);
		    params.addRule(RelativeLayout.RIGHT_OF, answers_list.get(answers_list.size()-1).getId());
		    imageRemove.setLayoutParams(params);
		    rl.addView(imageRemove);
	    }else{
	    	rl.removeView(imageRemove);
	    } 
	}
	
	private void removeEditText(){
		rl.removeView(answers_list.get(answers_list.size()-1));
		answers_list.remove(answers_list.size()-1);
		printButtons();
	}
	
	public void setTime(int new_hour, int new_minute){
		hour = new_hour;
		minute = new_minute;
		showTime();
	}
	
	public void showTime(){
		if(!DateFormat.is24HourFormat(this)){
			if(hour>12){
				hour = hour - 12;
				timeButton.setText(String.format(hour+":%02d"+" PM", minute));
				daytime = "PM";
			}else{
				timeButton.setText(String.format(hour+":%02d"+" AM", minute));
				daytime = "AM";
			}
		}else{
			timeButton.setText(String.format(hour+":%02d", minute));
		}
		
	}
	
	public void showTimePickerDialog(View v) {
	    DialogFragment newFragment = new TimePickerFragment();
	    newFragment.show(getSupportFragmentManager(), "timePicker");
	}
	
	public void setDate(int new_year, int new_month, int new_day){
		day = new_day;
		month = new_month + 1;
		year = new_year;
		showDate();
	}
	
	public void showDate(){
		dateButton.setText(year+"/"+month+"/"+day);
	}
		
	public void showDatePickerDialog(View v) {
	    DialogFragment newFragment = new DatePickerFragment();
	    newFragment.show(getSupportFragmentManager(), "datePicker");
	}
	
	@Override
	public void onTaskCompleted() {
		if(!cm.Error()){
			finish();
		}
	}
	
	public void onPause() {
	     super.onPause();
	     overridePendingTransition(0, 0);
	 }
}
