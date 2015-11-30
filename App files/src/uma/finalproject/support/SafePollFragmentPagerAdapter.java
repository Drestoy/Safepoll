/**
 * @author Daniel Domínguez Restoy
 * @version 1.0
 */
package uma.finalproject.support;

import uma.finalproject.safepoll.HistoryFragment;
import uma.finalproject.safepoll.VotesFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class SafePollFragmentPagerAdapter extends FragmentPagerAdapter{
	static final int NUM_ITEMS = 2;
	Fragment voteFragment = new VotesFragment();
	Fragment historyFragment = new HistoryFragment();
	
	public SafePollFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return NUM_ITEMS;
    }

    @Override
    public Fragment getItem(int position) {
    	if(position==0){
    		return voteFragment;
    	}else{
    		return historyFragment;
    	}
    }
}
