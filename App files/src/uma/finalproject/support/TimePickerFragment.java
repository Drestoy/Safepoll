/**
 * @author Daniel Dom�nguez Restoy
 * @version 1.0
 */
package uma.finalproject.support;

import java.util.Calendar;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;
import uma.finalproject.safepoll.NewPoll;



public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final Calendar c = Calendar.getInstance();
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);
		
		return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
	}
	
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		((NewPoll)getActivity()).setTime(hourOfDay, minute);
	}
	
}
