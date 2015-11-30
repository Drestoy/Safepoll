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
public function forgotmail($forgotmail){
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
		//$result = mysql_query("CREATE TABLE $group_table(guid int(11) NOT NULL,group_name varchar(20) NOT NULL, picture varchar(100) NOT NULL, description varchar(100) NOT NULL, PRIMARY KEY (guid))");
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
		
    }
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
                // user authentication details are correct
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
				if(empty($result["subject"])){
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
    }
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