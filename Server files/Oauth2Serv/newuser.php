<?php
// include our OAuth2 Server object
require_once __DIR__.'/server.php';
/**
 PHP API for Register Requests and for Email Notifications.
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
    if ($tag == 'register') {
		// Request type is Register new user
        //$fname = $_POST['fname'];
		//$fname = htmlentities($_POST['fname'], ENT_QUOTES,'UTF-8');
		$fname = utf8_encode($_POST['fname']);
		$lname = utf8_encode($_POST['lname']);
        $email = utf8_encode($_POST['email']);
		$pass = utf8_encode($_POST['password']);
        $subject = "Registro de usuario en SafePoll";
        $message = "Hola $fname,\nSe ha registrado con exito esta cuenta en nuestro servicio.\nSaludos,\nAdministración de SafePoll.";
        $from = "contact@umaproject.grn.cc";
        $headers = "From:" . $from;
        // check if user is already existed
        if ($db->isUserExisted($email)) {
            // user is already existed - error response
            $response["error"] = 2;
            $response["error_msg"] = "El usuario ya existe.";
            echo json_encode($response);
        }
        else if(!$db->validEmail($email)){
            $response["error"] = 3;
            $response["error_msg"] = "Email invalido.";
            echo json_encode($response);
		}
		else {
            // store user
            $user = $db->storeUser($fname, $lname, $email, $pass);
            if ($user) {
                // user stored successfully
				$response["success"] = 1;
				$response["email"] = $user["client_id"];
				$response["pass"] = $pass;
				
				mail($email,$subject,$message,$headers);
                echo json_encode($response);
            } else {
                // user failed to store
                $response["error"] = 1;
                $response["error_msg"] = "Error al intentar el registro.";
                echo json_encode($response);
            }
        }
    } else if ($tag == 'forpass'){
		$forgotpassword = $_POST['forgotpassword'];
		$forgotmail = $_POST['forgotmail'];
		if(empty($forgotpassword)){
			$result = $db->forgotmail($forgotmail);
			
			if($result){
				$response["success"] = 1;
				
				$response["result"] = $result["client_id"];
				
				echo json_encode($response);
			}else{
				$response["error"] = 1;
				echo json_encode($response);
			}
		}else{
			$randomcode = $db->random_string();
			$hash = $db->hashSSHA($randomcode);
			$encrypted_password = $hash["encrypted"]; // encrypted password
			$salt = $hash["salt"];
			$subject = "Recuperacion de contraseña";
			//$message = "Hello User,\nYour Password is sucessfully changed. Your new Password is $randomcode . Login with your new Password and change it in the User Panel.\nRegards,\nSafepoll Team.";
		
			if ($db->isUserExisted($forgotpassword)) {
			
				$token = $db->forgotPassword($forgotpassword, $encrypted_password, $salt, $randomcode);
				if (isset($token)) {
					///////////////////
					$scheme = (isset($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off') ? 'https' : 'http';
					$url = $scheme . '://' . $_SERVER['HTTP_HOST'] . '/grant.php?token=' . $token; // . $_SERVER['REQUEST_URI']
					$message = "Saludos,\nHemos recibido una peticion para reiniciar tu contraseña. Si usted realizó la petición haga click en el link que aparece a continuación:\n\n $url  \n\nTras clickear en el enlace, su contraseña será reiniciada y recibirá un correo con su nueva contraseña.\n\nSaludos,\nAdministración de Safepoll.";
					$from = "contact@umaproject.grn.cc";
					$headers = "From:" . $from;
					////////////////////
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
		
	} else {
         $response["error"] = 3;
         $response["error_msg"] = "JSON ERROR";
        echo json_encode($response);
    }
}
?>