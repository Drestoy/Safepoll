/**
 * @author Daniel Domínguez Restoy
 * @version 1.0
 */
package uma.finalproject.database;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import android.content.Context;
public class UserFunctions {
    private JSONParser jsonParser;
    private Context ctx;
    
    private static String loginURL = "http://url.aqui/";
    private static String registerURL = "http://url.aqui/";
    private static String forpassURL = "http://url.aqui/";
    private static String chgpassURL = "http://url.aqui/";
    private static String URL = "http://url.aqui/";
    private static String login_tag = "login";
    private static String register_tag = "register";
    private static String forpass_tag = "forpass";
    private static String chgpass_tag = "chgpass";
    private static String session_tag = "session";
    private static String getGroupList_tag = "getGroupList";
    private static String creategroup_tag = "createGroup";
    private static String searchGroup_tag = "searchGroup";
    private static String userAddgroup_tag = "userAddGroup";
    private static String userWithdrawgroup_tag = "userWithdrawGroup";
    private static String deletegroup_tag = "deleteGroup";
    private static String getPollsList_tag = "getPollsList";
    private static String sendVote_tag = "sendVote";
    private static String createPoll_tag = "createPoll";
    private static String setPublicKey_tag = "setPublicKey";
    private static String getPublicKey_tag = "getPublicKey";
    private static String changeOptions_tag = "changeOptions";
    private static String userRequest_tag = "userRequest";
    private static String getRequests_tag = "getRequests";
    private static String getMembers_tag = "getMembers";
    private static String decideRequest_tag = "decideRequest";
    private static String changeAdm_tag = "changeAdm";
    
    
    public UserFunctions(){
        jsonParser = new JSONParser();
    }
    
    public UserFunctions(Context ctx){
    	jsonParser = new JSONParser();
    	this.ctx = ctx;
    }
    
    public JSONObject loginUser(String email, String password, String mode){
        List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
        if(mode=="Login"){
        	params.add(new BasicNameValuePair("tag", login_tag));
        }else{
        	params.add(new BasicNameValuePair("tag", session_tag));
        }
        
        params.add(new BasicNameValuePair("email", email));
        params.add(new BasicNameValuePair("password", password));
        JSONObject json = jsonParser.getJSONFromUrl(loginURL, params,mode,ctx);
        return json;
    }
    
    public JSONObject chgPass(String newpas, String email, String token){
        List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
        params.add(new BasicNameValuePair("tag", chgpass_tag));
        params.add(new BasicNameValuePair("email", email));
        params.add(new BasicNameValuePair("token", token));
        params.add(new BasicNameValuePair("newpas", newpas));
        JSONObject json = jsonParser.getJSONFromUrl(chgpassURL, params,"chgPass",ctx);
        return json;
    }
    
    public JSONObject forPass(String field1, String field2){
        List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
        params.add(new BasicNameValuePair("tag", forpass_tag));
        params.add(new BasicNameValuePair("forgotpassword", field1));
        params.add(new BasicNameValuePair("forgotmail", field2));
        JSONObject json = jsonParser.getJSONFromUrl(forpassURL, params,"resetPass",ctx);
        return json;
    }
    
    public JSONObject registerUser(String fname, String lname, String email, String password){
        List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
        params.add(new BasicNameValuePair("tag", register_tag));
        params.add(new BasicNameValuePair("fname", fname));
        params.add(new BasicNameValuePair("lname", lname));
        params.add(new BasicNameValuePair("email", email));
        params.add(new BasicNameValuePair("password", password));
        JSONObject json = jsonParser.getJSONFromUrl(registerURL,params,"register",ctx);
        return json;
    }
    
   public JSONObject newGroup(String user, String userToken, String uid, List<BasicNameValuePair> paramslist){
       List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
       params.add(new BasicNameValuePair("tag", creategroup_tag));
       params.add(new BasicNameValuePair("email", user));
       params.add(new BasicNameValuePair("token", userToken));
       params.add(new BasicNameValuePair("user", uid));
       for(int x=0;x<paramslist.size();x++){
    		params.add(paramslist.get(x));
    	}
       JSONObject json = jsonParser.getJSONFromUrl(URL,params,"newGroup",ctx);
       return json;
   }
   
   public JSONObject getGroupsList(String user, String userToken, String uid){
       List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
       params.add(new BasicNameValuePair("tag", getGroupList_tag));
       params.add(new BasicNameValuePair("email", user));
       params.add(new BasicNameValuePair("token", userToken));
       params.add(new BasicNameValuePair("uid", uid));
       JSONObject json = jsonParser.getJSONFromUrl(URL,params,"getGroupsList",ctx);
       return json;
   }
   
   public JSONObject searchGroups(String user, String userToken, String uid, String request){
       List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
       params.add(new BasicNameValuePair("tag", searchGroup_tag));
       params.add(new BasicNameValuePair("email", user));
       params.add(new BasicNameValuePair("token", userToken));
       params.add(new BasicNameValuePair("uid", uid));
       params.add(new BasicNameValuePair("request", request));
       JSONObject json = jsonParser.getJSONFromUrl(URL,params,"searchGroups",ctx);
       return json;
   }
   
   public JSONObject userAddGroup(String user, String userToken, String groupid, String uid, String gname){
       List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
       params.add(new BasicNameValuePair("tag", userAddgroup_tag));
       params.add(new BasicNameValuePair("email", user));
       params.add(new BasicNameValuePair("token", userToken));
       params.add(new BasicNameValuePair("groupid", groupid));
       params.add(new BasicNameValuePair("user", uid));
       params.add(new BasicNameValuePair("gname", gname));
       JSONObject json = jsonParser.getJSONFromUrl(URL,params,"userAddGroup",ctx);
       return json;
   }
   
   public JSONObject userWithdrawGroup(String user, String userToken, String groupid, String uid){
       List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
       params.add(new BasicNameValuePair("tag", userWithdrawgroup_tag));
       params.add(new BasicNameValuePair("email", user));
       params.add(new BasicNameValuePair("token", userToken));
       params.add(new BasicNameValuePair("groupid", groupid));
       params.add(new BasicNameValuePair("user", uid));
       JSONObject json = jsonParser.getJSONFromUrl(URL,params,"userWithdrawGroup",ctx);
       return json;
   }
   
   public JSONObject deleteGroup(String user, String userToken, String groupid, String uid){
       List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
       params.add(new BasicNameValuePair("tag", deletegroup_tag));
       params.add(new BasicNameValuePair("email", user));
       params.add(new BasicNameValuePair("token", userToken));
       params.add(new BasicNameValuePair("groupid", groupid));
       params.add(new BasicNameValuePair("user", uid));
       JSONObject json = jsonParser.getJSONFromUrl(URL,params,"deleteGroup",ctx);
       return json;
   }
   
   public JSONObject getPollsList(String user, String userToken, String guid, String uid){
       List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
       params.add(new BasicNameValuePair("tag", getPollsList_tag));
       params.add(new BasicNameValuePair("email", user));
       params.add(new BasicNameValuePair("token", userToken));
       params.add(new BasicNameValuePair("guid", guid));
       params.add(new BasicNameValuePair("uid", uid));
       JSONObject json = jsonParser.getJSONFromUrl(URL,params,"getPollsList",ctx);
       return json;
   }
   
   public JSONObject sendVote(String mail, String userToken, String uid, List<BasicNameValuePair> paramslist){
       List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
       params.add(new BasicNameValuePair("tag", sendVote_tag));
       params.add(new BasicNameValuePair("email", mail));
       params.add(new BasicNameValuePair("token", userToken));
       params.add(new BasicNameValuePair("uid", uid));
       for(int x=0;x<paramslist.size();x++){
     		params.add(paramslist.get(x));
     	}
       JSONObject json = jsonParser.getJSONFromUrl(URL,params,"sendVote",ctx);
       return json;
   }
   
   public JSONObject createPoll(String mail, String userToken, List<BasicNameValuePair> paramslist){
	   List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
       params.add(new BasicNameValuePair("tag", createPoll_tag));
       params.add(new BasicNameValuePair("email", mail));
       params.add(new BasicNameValuePair("token", userToken));
       for(int x=0;x<paramslist.size();x++){
      		params.add(paramslist.get(x));
      	}
       JSONObject json = jsonParser.getJSONFromUrl(URL,params,"createPoll",ctx);
       return json;
   }
   
   public JSONObject setPublicKey(String mail, String userToken, String key, String subject){
	   List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
       params.add(new BasicNameValuePair("tag", setPublicKey_tag));
       params.add(new BasicNameValuePair("email", mail));
       params.add(new BasicNameValuePair("token", userToken));
       params.add(new BasicNameValuePair("user", mail));
       params.add(new BasicNameValuePair("key", key));
       params.add(new BasicNameValuePair("subject", subject));
       JSONObject json = jsonParser.getJSONFromUrl(URL,params,"setPublicKey",ctx);
       return json;
   }
   
   public JSONObject getPublicKey(String mail, String userToken){
	   List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
       params.add(new BasicNameValuePair("tag", getPublicKey_tag));
       params.add(new BasicNameValuePair("email", mail));
       params.add(new BasicNameValuePair("token", userToken));
       params.add(new BasicNameValuePair("user", mail));
       JSONObject json = jsonParser.getJSONFromUrl(URL,params,"getPublicKey",ctx);
       return json;
   }
   
   public JSONObject changeOptions(String mail, String userToken, String guid, String options){
	   List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
       params.add(new BasicNameValuePair("tag", changeOptions_tag));
       params.add(new BasicNameValuePair("email", mail));
       params.add(new BasicNameValuePair("token", userToken));
       params.add(new BasicNameValuePair("guid", guid));
       params.add(new BasicNameValuePair("options", options));
       JSONObject json = jsonParser.getJSONFromUrl(URL,params,"changeOptions",ctx);
       return json;
   }
   
   public JSONObject userRequest(String mail, String userToken, String user, String guid){
	   List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
       params.add(new BasicNameValuePair("tag", userRequest_tag));
       params.add(new BasicNameValuePair("email", mail));
       params.add(new BasicNameValuePair("token", userToken));
       params.add(new BasicNameValuePair("guid", guid));
       params.add(new BasicNameValuePair("user", user));
       JSONObject json = jsonParser.getJSONFromUrl(URL,params,"userRequest",ctx);
       return json;
   }
   
   public JSONObject getRequests(String mail, String userToken, String guid){
	   List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
       params.add(new BasicNameValuePair("tag", getRequests_tag));
       params.add(new BasicNameValuePair("email", mail));
       params.add(new BasicNameValuePair("token", userToken));
       params.add(new BasicNameValuePair("guid", guid));
       JSONObject json = jsonParser.getJSONFromUrl(URL,params,"getRequests",ctx);
       return json;
   }
   
   public JSONObject getMembers(String mail, String userToken, String guid){
	   List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
       params.add(new BasicNameValuePair("tag", getMembers_tag));
       params.add(new BasicNameValuePair("email", mail));
       params.add(new BasicNameValuePair("token", userToken));
       params.add(new BasicNameValuePair("guid", guid));
       JSONObject json = jsonParser.getJSONFromUrl(URL,params,"getRequests",ctx);
       return json;
   }
   
   public JSONObject decideRequest(String mail, String userToken, List<BasicNameValuePair> paramslist){
	   List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
       params.add(new BasicNameValuePair("tag", decideRequest_tag));
       params.add(new BasicNameValuePair("email", mail));
       params.add(new BasicNameValuePair("token", userToken));
       for(int x=0;x<paramslist.size();x++){
     		params.add(paramslist.get(x));
     	}
       JSONObject json = jsonParser.getJSONFromUrl(URL,params,"decideRequest",ctx);
       return json;
   }
   
   public JSONObject changeAdm(String mail, String userToken, List<BasicNameValuePair> paramslist){
	   List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
       params.add(new BasicNameValuePair("tag", changeAdm_tag));
       params.add(new BasicNameValuePair("email", mail));
       params.add(new BasicNameValuePair("token", userToken));
       for(int x=0;x<paramslist.size();x++){
     		params.add(paramslist.get(x));
     	}
       JSONObject json = jsonParser.getJSONFromUrl(URL,params,"changeAdm",ctx);
       return json;
   }
}
