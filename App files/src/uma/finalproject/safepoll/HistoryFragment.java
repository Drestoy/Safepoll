/**
 * @author Daniel Domínguez Restoy
 * @version 1.0
 */

package uma.finalproject.safepoll;

import java.util.ArrayList;
import java.util.List;
import uma.finalproject.support.Votes_list_item;
import uma.finalproject.support.SafePoll_ListAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class HistoryFragment extends Fragment{
	private ListView history_ListView;
    private Context ctx;
    private List<Votes_list_item> history_fragment_list;
    private ProgressBar pBar;
    private TextView msg_no_Rows;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View createdView = inflater.inflate(R.layout.oldpollslayout, container, false);
		
		pBar = (ProgressBar) createdView.findViewById(R.id.progressbar_history);
		msg_no_Rows = (TextView) createdView.findViewById(R.id.msg_no_history);
		msg_no_Rows.setVisibility(View.INVISIBLE);
		ctx = getActivity().getBaseContext(); 
		history_fragment_list = new ArrayList<Votes_list_item>();
		
        history_ListView = (ListView) createdView.findViewById(R.id.history_fragment_list);
        
        history_ListView.setOnItemClickListener(new ListView.OnItemClickListener(){
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                	Votes_list_item item = (Votes_list_item) parent.getItemAtPosition(position);
                	Bundle group_data = new Bundle();
                	group_data.putString("Question", item.getQuestion());
                	group_data.putString("ID", item.getID());
                	group_data.putString("Description", item.getDescription());
                	group_data.putString("Answers", item.getAnswers());
                	group_data.putString("Start", item.getStart(6));
                	group_data.putString("End", item.getEnd(6));
                	group_data.putString("Vote", item.getVote());
                	group_data.putString("Sign", item.getSign());
                	group_data.putString("Overall", item.getOverall());
                	group_data.putBoolean("SignRequired", item.isSignRequired());
                	//
                	Intent intent=new Intent(ctx,HistoryActivity.class);
            		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            		intent.putExtras(group_data);
            		startActivity(intent);
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }    	
        });
        
	    return createdView; 
    }
	
	protected void receiveData(List<Votes_list_item> list){
		history_fragment_list.clear();
		history_fragment_list = list;
        pBar.setVisibility(View.INVISIBLE);
        if((history_fragment_list==null)||(history_fragment_list.isEmpty())){
        	msg_no_Rows.setVisibility(View.VISIBLE);
        }
        history_ListView.setAdapter(new SafePoll_ListAdapter<Votes_list_item>(ctx,R.layout.polls_list_row,history_fragment_list,"History_list_item",null));
	}
}
