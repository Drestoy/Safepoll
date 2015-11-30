/**
 * @author Daniel Domínguez Restoy
 * @version 1.0
 */
package uma.finalproject.support;

import android.annotation.SuppressLint;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Votes_list_item {
	private String pollQuestion;
	private String bbdd_ID;
	private String description;
	private String answers;
	private boolean isFinished;
	private boolean signRequired;
	private String vote;
	private String sign;
	private String overall;
	
	private Calendar start,end;
	public Votes_list_item(String question, String desc, String possible_answers, String poll_start, String poll_end, String id, String vote, String sign, String overall, boolean finished, boolean requeriment){
		pollQuestion = question;
		description = desc;
		answers = possible_answers;
		bbdd_ID = id;
		this.vote = vote;
		this.sign = sign;
		this.overall = overall;
		isFinished = finished;
		signRequired = requeriment;
		
		start = Calendar.getInstance();
		start.set(Integer.valueOf(poll_start.substring(0, 4)),Integer.valueOf(poll_start.substring(5, 7))-1,Integer.valueOf(poll_start.substring(8, 10)),Integer.valueOf(poll_start.substring(11, 13)),Integer.valueOf(poll_start.substring(14, 16)),Integer.valueOf(poll_start.substring(17, 19)));
		
		end = Calendar.getInstance();
		end.set(Integer.valueOf(poll_end.substring(0, 4)),Integer.valueOf(poll_end.substring(5, 7))-1,Integer.valueOf(poll_end.substring(8, 10)),Integer.valueOf(poll_end.substring(11, 13)),Integer.valueOf(poll_end.substring(14, 16)),Integer.valueOf(poll_end.substring(17, 19)));
		
	}
	
	public String getQuestion(){
		return pollQuestion;
	}
	
	public String getDescription(){
		return description;
	}
	
	public String getAnswers(){
		return answers;
	}
	
	@SuppressLint("SimpleDateFormat")
	public String getStart(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(start.getTime());
	}
	
	@SuppressLint("SimpleDateFormat")
	public String getStart(int timezone){
		Calendar tmp = Calendar.getInstance();
		tmp.setTime(start.getTime());
		tmp.add(Calendar.HOUR, timezone);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(tmp.getTime());
	}
	
	@SuppressLint("SimpleDateFormat") 
	public String getEnd(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(end.getTime());
	}
	
	@SuppressLint("SimpleDateFormat")
	public String getEnd(int timezone){
		Calendar tmp = Calendar.getInstance();
		tmp.setTime(end.getTime());
		tmp.add(Calendar.HOUR, timezone);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(tmp.getTime());
	}
	
	public String getID(){
		return bbdd_ID;
	}
	
	public String getVote(){
		return vote;
	}
	
	public String getSign(){
		return sign;
	}
	
	public String getOverall(){
		return overall;
	}
	
	public boolean isFinished(){
		return isFinished;
	}
	
	public boolean isSignRequired(){
		return signRequired;
	}
}
