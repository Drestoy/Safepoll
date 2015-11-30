/**
 * Created with IntelliJ IDEA.
 * User: Udini
 * Date: 19/03/13
 * Time: 19:10
 */

package uma.finalproject.auth;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SafePollAuthenticatorService extends Service {
    @Override
    public IBinder onBind(Intent intent) {

        SafePollAuthenticator authenticator = new SafePollAuthenticator(this);
        return authenticator.getIBinder();
    }
}
