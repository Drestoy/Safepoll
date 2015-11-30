<?php
// include our OAuth2 Server object
require_once __DIR__.'/server.php';

// Handle a request for an OAuth2.0 Access Token and send the response to the client
if (!$server->verifyResourceRequest(OAuth2\Request::createFromGlobals())) {
    $server->getResponse()->send();
    die;
}
/**
 PHP API for Login, Register, Changepassword, Resetpassword Requests and for Email Notifications.
 **/
if (isset($_POST['tag']) && $_POST['tag'] != '') {
    // Get tag
    $tag = $_POST['tag'];
    // Include Database handler
    require_once 'include/DB_Functions.php';
    $db = new DB_Functions();
	
    // response Array
    $response = array("tag" => $tag, "success" => 0, "error" => 0);
    // check for tag type
    if ($tag == 'login') {
        // Request type is check Login
        $email = utf8_encode($_POST['email']);
        $password = utf8_encode($_POST['password']);
        // check for user
        $user = $db->getUserByEmailAndPassword($email, $password);
        if ($user != false) {
            // user found
            // echo json with success = 1
            $response["success"] = 1;
            $response["user"]["fname"] = $user["firstname"];
            $response["user"]["lname"] = $user["lastname"];
			$response["user"]["uid"] = $user["unique_id"];
            echo json_encode($response);
        } else {
            // user not found
            // echo json with error = 1
            $response["error"] = 1;
			$response["user"] = $email;
			$response["pass"] = $password;
            $response["error_msg"] = "Email o contraseña invalidos.";
            echo json_encode($response);
        }
    }
	else if ($tag == 'session') {
        // Request type is check Login
        $email = utf8_encode($_POST['email']);
        //$password = $_POST['password'];
        // check for user
        $user = $db->getUserByEmail($email);
        if ($user != false) {
            // user found
            // echo json with success = 1
            $response["success"] = 1;
            $response["user"]["fname"] = $user["firstname"];
            $response["user"]["lname"] = $user["lastname"];
			$response["user"]["uid"] = $user["unique_id"];
            echo json_encode($response);
        } else {
            // user not found
            // echo json with error = 1
            $response["error"] = 1;
            $response["error_msg"] = "Email o contraseña invalidos.";
            echo json_encode($response);
        }
    }
	else if ($tag == 'chgpass'){
		$email = utf8_encode($_POST['email']);
		$newpassword = utf8_encode($_POST['newpas']);
		$hash = $db->hashSSHA($newpassword);
        $encrypted_password = $hash["encrypted"]; // encrypted password
        $salt = $hash["salt"];
		$subject = "Notificacion de cambio de contraseña";
         $message = "Saludos,\nTu contraseña se ha cambiado con exito. Tu nueva contraseña es $newpassword .\nSaludos,\nAdministracion de Safepoll.";
          $from = "contact@umaproject.grn.cc";
          $headers = "From:" . $from;
		if ($db->isUserExisted($email)) {
			$user = $db->chgPassword($email, $encrypted_password, $salt);
			if ($user) {
				$response["success"] = 1;
				mail($email,$subject,$message,$headers);
				echo json_encode($response);
			}
			else {
				$response["error"] = 1;
				echo json_encode($response);
			}
            // user is already existed - error response
        }
        else {
            $response["error"] = 2;
            $response["error_msg"] = "El usuario no existe.";
             echo json_encode($response);
		}
	}
	else if ($tag == 'forpass'){
		$forgotpassword = $_POST['forgotpassword'];
		$randomcode = $db->random_string();
		$hash = $db->hashSSHA($randomcode);
        $encrypted_password = $hash["encrypted"]; // encrypted password
        $salt = $hash["salt"];
		
		if ($db->isUserExisted($forgotpassword)) {
			$user = $db->forgotPassword($forgotpassword, $encrypted_password, $salt,$randomcode);
			if ($user) {
				$response["success"] = 1;
				mail($forgotpassword,$subject,$message,$headers);
				echo json_encode($response);
			}
			else {
				$response["error"] = 1;
				echo json_encode($response);
			}
            // user is already existed - error response
        }
        else {
            $response["error"] = 2;
            $response["error_msg"] = "El usuario no existe.";
             echo json_encode($response);
		}
	}
	else if ($tag == 'register') {
        // Request type is Register new user
        $fname = $_POST['fname'];
		$lname = $_POST['lname'];
        $email = $_POST['email'];
		$uname = $_POST['uname'];
        $password = $_POST['password'];
          $subject = "Registro ";
         $message = "Hello $fname,\nYou have sucessfully registered to our service.\nRegards,\nAdmin.";
          $from = "contact@umaproject.grn.cc";
          $headers = "From:" . $from;
        // check if user is already existed
        if ($db->isUserExisted($email)) {
            // user is already existed - error response
            $response["error"] = 2;
            $response["error_msg"] = "Usuario ya existente.";
            echo json_encode($response);
        }
        else if(!$db->validEmail($email)){
            $response["error"] = 3;
            $response["error_msg"] = "Email invalido.";
            echo json_encode($response);
		}
		else {
            // store user
            $user = $db->storeUser($fname, $lname, $email, $uname, $password);
            if ($user) {
                // user stored successfully
				$response["success"] = 1;
				$response["user"]["fname"] = $user["firstname"];
				$response["user"]["lname"] = $user["lastname"];
				$response["user"]["email"] = $user["email"];
				$response["user"]["uname"] = $user["username"];
				$response["user"]["created_at"] = $user["created_at"];
               mail($email,$subject,$message,$headers);
                echo json_encode($response);
            } else {
                // user failed to store
                $response["error"] = 1;
                $response["error_msg"] = "Error al intentar registrar la cuenta.";
                echo json_encode($response);
            }
        }
    } else if ($tag == 'createGroup') {
         $response["error"] = 3;
         $response["error_msg"] = "JSON ERROR";
        echo json_encode($response);
    }
} else {
    echo "Safepoll API";
}

//echo json_encode(array('success' => true, 'message' => 'You accessed my APIs!'));
?>