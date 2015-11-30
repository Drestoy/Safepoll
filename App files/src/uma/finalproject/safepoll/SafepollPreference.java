/**
 * @author Daniel Domínguez Restoy
 * @version 1.0
 */

package uma.finalproject.safepoll;

import uma.finalproject.database.ConnectionManager;
import uma.finalproject.database.OnTaskCompleted;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class SafepollPreference extends DialogPreference implements OnTaskCompleted{

	private EditText oldpass, newpass, repeatpass;
	private Context ctx;
	private ConnectionManager cm;
	private AccountManager am;
	private Account[] ac;
	private OnTaskCompleted listener;
	
	public SafepollPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setDialogLayoutResource(R.layout.chgpass_dialog);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
        
        ctx = context;
        am = AccountManager.get(ctx);
        ac = am.getAccountsByType("com.safepoll");
        listener = this;
        setDialogIcon(null);
        
	}
	
	@Override
    public void onBindDialogView(View view) {
      super.onBindDialogView(view);
      
      oldpass = (EditText) view.findViewById(R.id.dialog_oldpass);
      newpass = (EditText) view.findViewById(R.id.dialog_newpass);
      repeatpass =  (EditText) view.findViewById(R.id.dialog_repeatpass);
    }
	
	@Override
	protected void showDialog(Bundle state) {
	    super.showDialog(state);
	    
        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(
	    		new View.OnClickListener()
		          {            
		              @Override
		              public void onClick(View view)
		              {
		            	 String olD = oldpass.getText().toString();
		            	 String neW = newpass.getText().toString();
		            	 String repeaT = repeatpass.getText().toString();
		            	 
		            	 cm = new ConnectionManager(ctx,ac[0].name,neW,"ChangePass");
		            	  
		            	 if( ( !olD.equals("")) && ( !neW.equals("")) && ( !repeaT.equals(""))){
		            		 if(neW.equals(repeaT)){
		            			 if(olD.equals(am.getPassword(ac[0]))){
		            				cm.NetAsync(((AlertDialog) getDialog()).getListView(),listener);
			            			((AlertDialog) getDialog()).dismiss();
		            			 }else{
		            				 Toast.makeText(((AlertDialog) getDialog()).getContext(),"La contraseña antigua no es correcta", Toast.LENGTH_SHORT).show();
		            			 }
		            		 }else{
		            			 Toast.makeText(((AlertDialog) getDialog()).getContext(),"El último campo no es igual a la nueva contraseña", Toast.LENGTH_SHORT).show();
		            		 }
		            	 }else{
		            		 Toast.makeText(((AlertDialog) getDialog()).getContext(),"Faltan campos por rellenar", Toast.LENGTH_SHORT).show();
		            	 }
		            	 
		            	  
		              }
		          });
	}
	

	@Override
	public void onTaskCompleted() {
		if(!cm.Error()){
			am.setPassword(ac[0], newpass.getText().toString());
		}
		
	}
	
}
