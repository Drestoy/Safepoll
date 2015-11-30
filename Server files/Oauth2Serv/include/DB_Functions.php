<?php
class DB_Functions {
    private $db;
    //put your code here
    // constructor
    function __construct() {
        require_once 'DB_Connect.php';
        // connecting to database
        $this->db = new DB_Connect();
        $this->db->connect();
    }
    // destructor
    function __destruct() {
    }
    /**
     * Random string which is sent by mail to reset password
     */
public function random_string()
{
    $character_set_array = array();
    $character_set_array[] = array('count' => 7, 'characters' => 'abcdefghijklmnopqrstuvwxyz');
    $character_set_array[] = array('count' => 1, 'characters' => '0123456789');
    $temp_array = array();
    foreach ($character_set_array as $character_set) {
        for ($i = 0; $i < $character_set['count']; $i++) {
            $temp_array[] = $character_set['characters'][rand(0, strlen($character_set['characters']) - 1)];
        }
    }
    shuffle($temp_array);
    return implode('', $temp_array);
}

public function chgPassword($email, $newpassword, $salt){
	$result = mysql_query("UPDATE `oauth_clients` SET `client_secret` = '$newpassword',`salt` = '$salt'
              WHERE `client_id` = '$email'");
	if ($result) {
		return true;
	}
	else
	{
		return false;
	}

}
public function forgotmail($forgotmail){		$result = mysql_query("SELECT client_id FROM oauth_clients WHERE subject='$forgotmail'");		if($result){		$result = mysql_fetch_array($result);				return $result;	}else{		return null;	}}
public function forgotPassword($forgotpassword, $newpassword, $salt, $newpass){
	$query = 'SELECT * FROM petitions WHERE user = ' . "'" . $forgotpassword. "'";
	$result = mysql_query($query) or die(mysql_error());
	if(isset($result)){
		$no_of_rows = mysql_num_rows($result);
		if ($no_of_rows > 0){
			$query = 'DELETE FROM petitions WHERE user = ' . "'" . $forgotpassword . "'";
			$result = mysql_query($query) or die(mysql_error());
		
		}
		$randomcode = $this->random_string();
		$hash = $this->hashSSHA($randomcode);
		$token = $hash["encrypted"];
		//$petition = "\"UPDATE `oauth_clients` SET `client_secret` = $newpassword,`salt` = $salt WHERE `client_id` = $forgotpassword\"";
		$petition = 'UPDATE `oauth_clients` SET `client_secret` = ' . "\'" . $newpassword . "\'" . ',`salt` = ' . "\'" . $salt . "\'" . ' WHERE `client_id` = ' . "\'" . $forgotpassword . "\'" ;
		$result = mysql_query("INSERT INTO petitions(token, petition,code,user) VALUES('$token','$petition','$newpass','$forgotpassword')") or die(mysql_error());
		//$result = mysql_query("INSERT INTO petitions(token, petition) VALUES('$token','bo que pasa')");
		if($result){
			return $token;
		}else{
			return null;
		}
	}else{
		return null;
	}
	
}
/*
	Exchange token for petition
*/
public function getPetition($token){
	$query = 'SELECT * FROM petitions WHERE token = ' . "'" . $token . "'";
	//echo $query;
	//$result = mysql_query("SELECT * FROM petitions WHERE token = '$token'") or die(mysql_error());
	$result = mysql_query($query) or die(mysql_error());	
	if(isset($result)){
		$no_of_rows = mysql_num_rows($result);
		if ($no_of_rows > 0){
			$petition = mysql_fetch_array($result);
			
			$pp = mysql_query($petition['petition']) or die(mysql_error());
			$query = 'DELETE FROM petitions WHERE token = ' . "'" . $token . "'";
			$result = mysql_query($query) or die(mysql_error());
			
			$newcode = $petition['code'];
			$user = $petition['user'];
			$subject = "Password Recovery";
			$message = "Hello User,\nYour Password is sucessfully changed. Your new Password is $newcode . Login with your new Password and change it in the User Panel.\nRegards,\nSafepoll Team.";
			$from = "contact@umaproject.grn.cc";
			$headers = "From:" . $from;
			mail($user,$subject,$message,$headers);
			
			
			return $pp;
		}else{
			
			return false;			
		}
	}else{
	
		return false;
	}
}




/**
     * Adding new user to mysql database
     * returns user details
     */
    public function storeUser($fname, $lname, $email, $password) {
        $uuid = uniqid('', false);
        $hash = $this->hashSSHA($password);
        $encrypted_password = $hash["encrypted"]; // encrypted password
        $salt = $hash["salt"]; // salt
		$group_table = "user".$uuid."_groups";
		//$result = mysql_query("CREATE TABLE $group_table(guid int(11) NOT NULL,group_name varchar(20) NOT NULL, picture varchar(100) NOT NULL, description varchar(100) NOT NULL, PRIMARY KEY (guid))");		$result = mysql_query("CREATE TABLE $group_table(guid varchar(23) NOT NULL,group_name varchar(23) NOT NULL, PRIMARY KEY (guid))");		
		if($result){
			$result = mysql_query("INSERT INTO oauth_clients(unique_id, firstname, lastname, client_id, client_secret, salt, created_at, groups_table,redirect_uri, public_key,subject) VALUES('$uuid','$fname', '$lname', '$email', '$encrypted_password', '$salt', NOW(), '$group_table','http://uri/','','')");
			// check for successful store
			if ($result) {
				// get user details
				$uid = mysql_insert_id(); // last inserted id
				$result = mysql_query("SELECT * FROM oauth_clients WHERE uid = $uid");
				// return user details
				return mysql_fetch_array($result);
			} else {
				mysql_query("DROP TABLE $group_table");
				return false;
			}
		}else{
			return false;
		}
		
    }		/*		Add public key	*/		public function addPK($user,$publickey,$subject){				$result = mysql_query("SELECT * FROM oauth_clients WHERE subject='$subject' AND client_id!='$user'") or die(mysql_error());				$result = mysql_fetch_array($result);				if(!(empty($result))){			return false;		}else{			$result = mysql_query("SELECT * FROM oauth_clients WHERE client_id='$user'") or die(mysql_error());					$result = mysql_fetch_array($result);					if(($result["subject"]=="")||(strcmp($result["subject"],$subject)==0)){				$result = mysql_query("UPDATE oauth_clients SET public_key='$publickey',subject='$subject' WHERE client_id='$user'") or die(mysql_error());					if($result){					return true;				}else{					return false;				}			}else{				return false;			}		}							}		/*		Get public key	*/		public function getPK($user){				$result = mysql_query("SELECT public_key FROM oauth_clients WHERE client_id = '$user'") or die(mysql_error());				if($result){			return mysql_fetch_array($result);		}else{			return false;		}		}		/*		Create a new group	*/		public function createGroup($creator,$gname,$pic,$desc,$options){			$uuid = uniqid('', false);						$query = 'SELECT * FROM groups WHERE guid = ' . "'" . $uuid . "'";		$result = mysql_query($query) or die(mysql_error());		if(isset($result)){			$no_of_rows = mysql_num_rows($result);			if ($no_of_rows > 0){				//Hay un grupo con esa id, crear otra nueva						return false;			}else{				$groupvotes_table = "group".$uuid."_votes";						$groupusers_table = "group".$uuid."_users";								$grouppolls_table = "group".$uuid."_polls";								$grouprequests_table = "group".$uuid."_requests";								//$result = mysql_query("CREATE TABLE $groupvotes_table(id int(11) NOT NULL AUTO_INCREMENT, uid varchar(23) NOT NULL, question varchar(200) NOT NULL, description varchar(100) NOT NULL, possible_answers varchar(1000) NOT NULL, start datetime NOT NULL, finished datetime DEFAULT NULL, isFinished BOOLEAN DEFAULT false, PRIMARY KEY (id))");				$result = mysql_query("CREATE TABLE $grouppolls_table(id int(11) NOT NULL AUTO_INCREMENT, question varchar(200) NOT NULL, description varchar(100) NOT NULL, possible_answers varchar(1000) NOT NULL, start datetime NOT NULL, finished datetime DEFAULT NULL, isFinished BOOLEAN DEFAULT false, signRequired BOOLEAN DEFAULT false, PRIMARY KEY (id))");				$result = $result & mysql_query("CREATE TABLE $groupvotes_table(id int(11) NOT NULL AUTO_INCREMENT, poll_id int(11) NOT NULL,user_id varchar(23) NOT NULL,answer VARCHAR(200) NOT NULL, sign VARCHAR(1000) CHARACTER SET ASCII COLLATE ascii_general_ci NOT NULL, PRIMARY KEY (id))");				$result = $result & mysql_query("CREATE TABLE $groupusers_table(id int(11) NOT NULL AUTO_INCREMENT, uid varchar(23) NOT NULL, is_adm BOOLEAN DEFAULT false, PRIMARY KEY (id))");				$result = $result & mysql_query("CREATE TABLE $grouprequests_table(id int(11) NOT NULL AUTO_INCREMENT, user varchar(80) NOT NULL, PRIMARY KEY (id))");								if(isset($result)){										$result = mysql_query("INSERT INTO groups(guid, group_name, picture, description,options) VALUES('$uuid','$gname','$pic','$desc','$options')");											$result = $this->userAddGroup($creator,$uuid,$gname);					$result = $this->groupAddUser($creator,$uuid,true);											$res = mysql_query("SELECT * FROM groups WHERE guid = '$uuid'");											// return user details					return mysql_fetch_array($res);										}else{					mysql_query("DROP TABLE $groupusers_table");					mysql_query("DROP TABLE $groupvotes_table");					mysql_query("DROP TABLE $grouppolls_table");					mysql_query("DROP TABLE $grouprequests_table");					return false;				}							}		}else{			return false;		}		 //$result = mysql_query("CREATE TABLE $group_table(id int (11) NOT NULL AUTO_INCREMENT, guid int(11) NOT NULL, group_name varchar(20) NOT NULL, picture varchar(100) NOT NULL, description varchar(100) NOT NULL, PRIMARY KEY (id))");		}		public function deleteGroup($uid, $guid){		$groupvotes_table = "group".$guid."_votes";		$groupusers_table = "group".$guid."_users";		$grouppolls_table = "group".$guid."_polls";		$grouprequests_table = "group".$guid."_requests";				$result = mysql_query("SELECT * from $groupusers_table") or die(mysql_error());				$no_of_rows = mysql_num_rows($result);				if ($no_of_rows > 0){			$this->userWithdrawGroup($uid, $guid);			$this->groupWithdrawUser($uid, $guid);			if ($no_of_rows > 1){				$x = 1;				while($x<$no_of_rows){					$res = mysql_fetch_array($result) or die(mysql_error());					$member_uid = $res["uid"];					$this->userWithdrawGroup($member_uid, $guid);					$this->groupWithdrawUser($member_uid, $guid);				}			}			mysql_query("DROP TABLE $groupvotes_table") or die(mysql_error());			mysql_query("DROP TABLE $groupusers_table") or die(mysql_error());			mysql_query("DROP TABLE $grouppolls_table") or die(mysql_error());			mysql_query("DROP TABLE $grouprequests_table") or die(mysql_error());			mysql_query("DELETE FROM groups WHERE guid = '$guid'") or die(mysql_error());			return true;		}else{					return false;		}			/////////	////////////	////////////	}		public function userAddGroup($uid, $guid, $gname){			$userTable = "user".$uid."_groups";			$result = mysql_query("SELECT * FROM $userTable") or die(mysql_error());			if((isset($result))&&($result)){									$result = mysql_query("INSERT INTO $userTable(guid, group_name) VALUES('$guid','$gname')") or die(mysql_error());						return true;			}else{				 return false;		 		} 	}		public function userWithdrawGroup($uid, $guid){			$userTable = "user".$uid."_groups";			$result = mysql_query("DELETE FROM $userTable WHERE guid = '$guid'") or die(mysql_error());			return $result; 	}		public function groupAddUser($uid, $guid, $is_adm){			$groupUserTable = "group".$guid."_users";				$result = mysql_query("INSERT INTO $groupUserTable(uid, is_adm) VALUES('$uid','$is_adm')") or die(mysql_error());			return $result;		}		public function groupWithdrawUser($uid, $guid){			$groupUserTable = "group".$guid."_users";				$result = mysql_query("DELETE FROM $groupUserTable WHERE uid = '$uid'") or die(mysql_error());			return $result;		}		public function userRequest($user,$guid){				$groupRequestsTable = "group".$guid."_requests";				$result = mysql_query("SELECT * FROM $groupRequestsTable WHERE user='$user'") or die(mysql_error());				$result = mysql_fetch_array($result);				if(empty($result)){			$result = mysql_query("INSERT INTO $groupRequestsTable(user) VALUES ('$user')") or die(mysql_error());						return $result;		}else{			return true;		}					}		public function getContRequests($guid){		$groupRequestsTable = "group".$guid."_requests";				$result = mysql_query("SELECT COUNT(user) FROM $groupRequestsTable") or die(mysql_error());				$result = mysql_fetch_array($result);				$result = $result["COUNT(user)"];				return $result;			}		public function getGroupRequests($guid){		$groupRequestsTable = "group".$guid."_requests";				//$result = mysql_query("SELECT * FROM $groupRequestsTable") or die(mysql_error());		$result = mysql_query("SELECT $groupRequestsTable.user,oauth_clients.firstname, oauth_clients.lastname,oauth_clients.subject,oauth_clients.unique_id FROM $groupRequestsTable JOIN oauth_clients ON $groupRequestsTable.user=oauth_clients.client_id") or die(mysql_error());				$no_of_rows = mysql_num_rows($result);		if ($no_of_rows > 0){			if ($no_of_rows > 1){				$x = 1;				//$res["R$x"] = mysql_fetch_array($result);				while($x<=$no_of_rows){										$res["R$x"] = mysql_fetch_array($result);					if(empty($res["R$x"]["subject"])){						$res["R$x"]["subject"] = false;						//$res["R$x"]["3"] = false;					}else{						$res["R$x"]["subject"] = true;						//$res["R$x"]["3"] = true;					}					$x = $x + 1;				}				return $res;			}else{				$res["R1"] = mysql_fetch_array($result);				if(empty($res["R1"]["subject"])){						$res["R1"]["subject"] = false;						//$res["R1"]["3"] = false;					}else{						$res["R1"]["subject"] = true;						//$res["R1"]["3"] = true;					}				return $res;				//return mysql_fetch_array($result);			}				}else{					return false;		}	}		public function decideRequest($guid,$user,$choice){				$groupRequestsTable = "group".$guid."_requests";				if(strcasecmp($choice,"true")){ //"true" == "true" = 0, "false" == "true" >0			//choice false			$result = mysql_query("DELETE FROM $groupRequestsTable WHERE user = '$user'") or die(mysql_error());		}else{			//choice true						$uid = mysql_query("SELECT unique_id FROM oauth_clients WHERE client_id='$user'") or die(mysql_error());			$uid = mysql_fetch_array($uid);			$uid = $uid["unique_id"];			$gname = mysql_query("SELECT group_name FROM groups WHERE guid='$guid'") or die(mysql_error());			$gname = mysql_fetch_array($gname);			$gname = $gname["group_name"];			$this->userAddGroup($uid, $guid, $gname);			$this->groupAddUser($uid, $guid, false);			$result = mysql_query("DELETE FROM $groupRequestsTable WHERE user = '$user'") or die(mysql_error());					}				return $result;	}		public function getGroupMembers($guid){		$groupRequestsTable = "group".$guid."_users";				$result = mysql_query("SELECT oauth_clients.unique_id,oauth_clients.firstname, oauth_clients.lastname, oauth_clients.client_id, oauth_clients.subject FROM $groupRequestsTable JOIN oauth_clients ON $groupRequestsTable.uid=oauth_clients.unique_id") or die(mysql_error());				$no_of_rows = mysql_num_rows($result);		if ($no_of_rows > 0){			if ($no_of_rows > 1){				$x = 1;				//$res["R$x"] = mysql_fetch_array($result);				while($x<=$no_of_rows){										$res["R$x"] = mysql_fetch_array($result);					if(empty($res["R$x"]["subject"])){						$res["R$x"]["subject"] = false;						//$res["R$x"]["4"] = false;					}else{						$res["R$x"]["subject"] = true;						//$res["R$x"]["4"] = true;					}					$x = $x + 1;									}				return $res;			}else{				$res["R1"] = mysql_fetch_array($result);				if(empty($res["R1"]["subject"])){						$res["R1"]["subject"] = false;						//$res["R1"]["4"] = false;					}else{						$res["R1"]["subject"] = true;						//$res["R1"]["4"] = true;					}				return $res;				//return mysql_fetch_array($result);			}				}else{					return false;		}	}		public function getGroupsList($uid){			$group_table = "user".$uid."_groups";				//$result = mysql_query("SELECT * FROM $group_table");		$result = mysql_query("SELECT $group_table.guid, $group_table.group_name, groups.options FROM $group_table JOIN groups ON $group_table.guid=groups.guid");				$no_of_rows = mysql_num_rows($result);		if ($no_of_rows > 0){			if ($no_of_rows > 1){				$x = 1;				$res["R$x"] = mysql_fetch_array($result);				while($x<$no_of_rows){					$x = $x + 1;					$res["R$x"] = mysql_fetch_array($result);				}				return $res;			}else{				$res["R1"] = mysql_fetch_array($result);				return $res;				//return mysql_fetch_array($result);			}				}else{					return false;		}						}		public function searchGroups($request,$uid){			$allWords = $this->getWordsFrom($request);				$user_group_table = "user".$uid."_groups";				$no_of_items = count($allWords);		$x = 1;		$query = "SELECT * FROM groups WHERE group_name LIKE '%$allWords[0]%' OR description LIKE '%$allWords[0]%'";										//$query = "SELECT * FROM groups WHERE group_name LIKE $allWords[0]";		while($x<$no_of_items){			 $query = $query . "OR group_name LIKE '%$allWords[$x]%' OR description LIKE '%$allWords[$x]%'";			 $x = $x+1;		}		$query2 = "SELECT * FROM ($query) AS a WHERE NOT EXISTS (SELECT * FROM $user_group_table AS b WHERE b.guid = a.guid)";				$result = mysql_query($query2);				$no_of_rows = mysql_num_rows($result);		if ($no_of_rows > 0){			if ($no_of_rows > 1){				$x = 1;				$res["R$x"] = mysql_fetch_array($result);				while($x<$no_of_rows){					$x = $x + 1;					$res["R$x"] = mysql_fetch_array($result);				}				return $res;			}else{				$res["R1"] = mysql_fetch_array($result);				return $res;			}				}else{					return false;		}	}		public function getWordsFrom($request){			return $words = preg_split('/\s+/', trim($request));		}			/*	* Votes functions	* //$requeriment = ($signRequired === "true");	*/		public function startPoll($question,$desc,$answers,$end,$guid,$signRequired){				$groupPollsTable = "group".$guid."_polls";		$format = 'Y-m-d H:i:s';		$date = date_create_from_format($format, $end);		date_sub($date,date_interval_create_from_date_string('6 hours'));		$date = $date->format('Y-m-d H:i:s');		if(strcasecmp($signRequired,"true")){ //"true" == "true" = 0, "false" == "true" >0			$result = mysql_query("INSERT INTO $groupPollsTable(question,description,possible_answers,start,finished,isFinished,signRequired) VALUES('$question','$desc','$answers',NOW(),'$date',false,false)") or die(mysql_error());		}else{			$result = mysql_query("INSERT INTO $groupPollsTable(question,description,possible_answers,start,finished,isFinished,signRequired) VALUES('$question','$desc','$answers',NOW(),'$date',false,true)") or die(mysql_error());		}		//$result = mysql_query("INSERT INTO $groupPollsTable(question,description,possible_answers,start,finished,isFinished,signRequired) VALUES('$question','$desc','$answers',NOW(),'$end',false,'$requeriment')") or die(mysql_error());		//$result = mysql_query("INSERT INTO $groupPollsTable(question,description,possible_answers,start,finished,isFinished,signRequired) VALUES('$question','$desc','$answers',NOW(),'$date',false,'$requeriment')") or die(mysql_error());		if($result){			return true;		}else{			return false;		}							}		public function getPollsList($guid){			$polls_table = "group".$guid."_polls";				$result = mysql_query("UPDATE $polls_table SET isFinished=true WHERE finished<NOW()");				$result = mysql_query("SELECT * FROM $polls_table");				$no_of_rows = mysql_num_rows($result);		if ($no_of_rows > 0){			$data = mysql_fetch_array($result);			if($data["isFinished"]==1){				$data["isFinished"]=true;			}else{				$data["isFinished"]=false;			}			if($data["signRequired"]==1){						$data["signRequired"]=true;					}else{						$data["signRequired"]=false;					}			$res["R1"] = $data;			if ($no_of_rows > 1){				$x = 1;				while($x<$no_of_rows){					$x = $x + 1;					$data = mysql_fetch_array($result);					if($data["isFinished"]==1){						$data["isFinished"]=true;					}else{						$data["isFinished"]=false;					}					if($data["signRequired"]==1){						$data["signRequired"]=true;					}else{						$data["signRequired"]=false;					}					$res["R$x"] = $data;				}				}			return $res;				}else if($no_of_rows == 0){			return -1;		}else{					return false;		}						}				public function getGroupOptions($guid){		$result = mysql_query("SELECT options FROM groups WHERE guid='$guid'");				$result = mysql_fetch_array($result);		return $result["options"];					}		public function setGroupOptions($guid,$options){		$result = mysql_query("UPDATE groups SET options='$options' WHERE guid='$guid'");				if($result){			return true;		}else{			return false;		}	}		public function getMyVotes($uid,$guid){				$votes_table = "group".$guid."_votes";				//$result = mysql_query("SELECT * from $votes_table WHERE user_id = '$uid'");		//$result = mysql_query("SELECT * from $votes_table");		$result = mysql_query("SELECT t1.*,t2.public_key from $votes_table AS t1 LEFT JOIN oauth_clients AS t2 ON t1.user_id=t2.unique_id");		$no_of_rows = mysql_num_rows($result);		if ($no_of_rows > 0){			$x = 0;			while($x<$no_of_rows){				$x = $x + 1;				$res["R$x"] = mysql_fetch_array($result);			}			return $res;		}else{			return false;		}	}		public function vote($uid,$guid,$poll,$vote,$sign){				$groupvotes_table = "group".$guid."_votes";				$groupusers_table = "group".$guid."_users";				$grouppolls_table = "group".$guid."_polls";				$result = mysql_query("INSERT INTO $groupvotes_table(poll_id,user_id,answer,sign) VALUES ('$poll','$uid','$vote','$sign')") or die(mysql_error());				if($result){			$num_users = mysql_query("SELECT COUNT(id) FROM $groupusers_table");			$num_votes = mysql_query("SELECT COUNT(poll_id) FROM $groupvotes_table WHERE poll_id='$poll'");			$num_users = mysql_fetch_array($num_users);			$num_votes = mysql_fetch_array($num_votes);			if($num_users["COUNT(id)"] == $num_votes["COUNT(poll_id)"]){				mysql_query("UPDATE $grouppolls_table SET isFinished=true, finished=NOW() WHERE id='$poll'");			}		}				return $result;	}	
	/**
    * Verifies user by email and password
    */
    public function getUserByEmailAndPassword($email, $password) {
        $result = mysql_query("SELECT * FROM oauth_clients WHERE client_id = '$email'") or die(mysql_error());
        // check for result
        $no_of_rows = mysql_num_rows($result);
        if ($no_of_rows > 0) {
            $result = mysql_fetch_array($result);
            $salt = $result['salt'];
            $encrypted_password = $result['client_secret'];
            $hash = $this->checkhashSSHA($salt, $password);
            // check for password equality
            if ($encrypted_password == $hash) {
                // user authentication details are correct								if(empty($result["subject"])){					$result["subject"] = false;				}else{					$result["subject"] = true;				}
                return $result;
            }
        } else {
            // user not found
            return false;
        }
    }
	/**
     * Verifies user by email
     */
    public function getUserByEmail($email) {
        $result = mysql_query("SELECT * FROM oauth_clients WHERE client_id = '$email'") or die(mysql_error());
        // check for result
        $no_of_rows = mysql_num_rows($result);
        if ($no_of_rows > 0) {
            $result = mysql_fetch_array($result);
				if(empty($result["subject"])){					$result["subject"] = false;				}else{					$result["subject"] = true;				}
                return $result;
            
        } else {
            // user not found
            return false;
        }
    }
 /**
     * Checks whether the email is valid or fake
     */
public function validEmail($email)
{
   $isValid = true;
   $atIndex = strrpos($email, "@");
   if (is_bool($atIndex) && !$atIndex)
   {
      $isValid = false;
   }
   else
   {
      $domain = substr($email, $atIndex+1);
      $local = substr($email, 0, $atIndex);
      $localLen = strlen($local);
      $domainLen = strlen($domain);
      if ($localLen < 1 || $localLen > 64)
      {
         // local part length exceeded
         $isValid = false;
      }
      else if ($domainLen < 1 || $domainLen > 255)
      {
         // domain part length exceeded
         $isValid = false;
      }
      else if ($local[0] == '.' || $local[$localLen-1] == '.')
      {
         // local part starts or ends with '.'
         $isValid = false;
      }
      else if (preg_match('/\.\./', $local))
      {
         // local part has two consecutive dots
         $isValid = false;
      }
      else if (!preg_match('/^[A-Za-z0-9\-\.]+$/', $domain))
      {
         // character not valid in domain part
         $isValid = false;
      }
      else if (preg_match('/\.\./', $domain))
      {
         // domain part has two consecutive dots
         $isValid = false;
      }
      else if(!preg_match('/^(\\.|[A-Za-z0-9!#%&`_=\/$\'*+?^{}|~.-])+$/', str_replace("\\","",$local)))
      {
         // character not valid in local part unless
         // local part is quoted
         if (!preg_match('/^"(\\"|[^"])+"$/', str_replace("\\","",$local)))
         {
            $isValid = false;
         }
      }
      if ($isValid && !(checkdnsrr($domain,"MX") || checkdnsrr($domain,"A")))
      {
         // domain not found in DNS
         $isValid = false;
      }
   }
   return $isValid;
}
 /**
     * Check user is existed or not
     */
    public function isUserExisted($email) {
        $result = mysql_query("SELECT client_id from oauth_clients WHERE client_id = '$email'");
        $no_of_rows = mysql_num_rows($result);
        if ($no_of_rows > 0) {
            // user existed
            return true;
        } else {
            // user not existed
            return false;
        }
    }		/**     * Check group is existed or not     */    public function isGroupExisted($gname) {        $result = mysql_query("SELECT group_name from groups WHERE group_name = '$gname'");        $no_of_rows = mysql_num_rows($result);        if ($no_of_rows > 0) {            // group existed            return true;        } else {            // group not existed            return false;        }    }		public function isUserAdmin($uid, $guid){				$groupUserTable = "group".$guid."_users";				$result = mysql_query("SELECT * from $groupUserTable WHERE uid = '$uid'");				$no_of_rows = mysql_num_rows($result);        if ($no_of_rows > 0) {			$result = mysql_fetch_array($result);			if ($result["is_adm"] == 1){				return true;			}else{				return false;			}        } else {            return false;        }	}		public function changeAdm($uid,$adm,$guid){				$groupUserTable = "group".$guid."_users";				$result = mysql_query("UPDATE $groupUserTable SET is_adm=true WHERE uid='$uid'") or die(mysql_error());				if($result){			$result = mysql_query("UPDATE $groupUserTable SET is_adm=false WHERE uid='$adm'") or die(mysql_error());		}						return $result;	}		public function deleteAT($access_token){			mysql_query("DELETE FROM oauth_access_tokens WHERE access_token='$access_token'") or die(mysql_error());		}
    /**
     * Encrypting password
     * returns salt and encrypted password
     */
    public function hashSSHA($password) {
        $salt = sha1(rand());
        $salt = substr($salt, 0, 10);
        $encrypted = base64_encode(sha1($password . $salt, true) . $salt);
        $hash = array("salt" => $salt, "encrypted" => $encrypted);
        return $hash;
    }
    /**
     * Decrypting password
     * returns hash string
     */
    public function checkhashSSHA($salt, $password) {
        $hash = base64_encode(sha1($password . $salt, true) . $salt);
        return $hash;
    }
}
?>