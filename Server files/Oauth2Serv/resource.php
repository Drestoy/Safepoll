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
    $db = new DB_Functions();	$access_token = $_POST['access_token'];	$db->deleteAT($access_token);
	
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
			$response["user"]["uid"] = $user["unique_id"];						$response["user"]["subject"] = $user["subject"];
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
			$response["user"]["uid"] = $user["unique_id"];						$response["user"]["subject"] = $user["subject"];
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
	else if ($tag == 'forpass'){		////////		//ESTE NO SE USA PORQUE ESTA EL DE NEWUSER		////////
		$forgotpassword = $_POST['forgotpassword'];
		$randomcode = $db->random_string();
		$hash = $db->hashSSHA($randomcode);
        $encrypted_password = $hash["encrypted"]; // encrypted password
        $salt = $hash["salt"];
		
		if ($db->isUserExisted($forgotpassword)) {
			$user = $db->forgotPassword($forgotpassword, $encrypted_password, $salt,$randomcode);
			if ($user) {							$subject = "Recuperar contraseña";				//$message = "Hello User,\nYour Password is sucessfully changed. Your new Password is $randomcode . Login with your new Password and change it in the User Panel.\nRegards,\nSafepoll Team.";												$message = "Saludos,\nWe received a request to reset your SafePoll account password. If you want to do so, please click on the next link:\n\n $user \nRegards,\nSafepoll Team.";				$from = "contact@umaproject.grn.cc";				$headers = "From:" . $from;
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
	else if ($tag == 'register') {		// NO SE USA POR USARSE EL DE NEWUSER
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
    } else if ($tag == 'createGroup') {        // Request type is Register new group		        $creator = $_POST['user'];		$gname = utf8_encode($_POST['gname']);        $desc = utf8_encode($_POST['desc']);				$options = $_POST['options'];		        // check if group is already existed        if ($db->isGroupExisted($gname)) {            // user is already existed - error response            $response["error"] = 2;            $response["error_msg"] = "Este grupo ya existe.";            echo json_encode($response);        }else {            // create group            $group = $db->createGroup($creator,$gname,"",$desc,$options);            if ($group) {                // group stored successfully				$response["success"] = 1;				$response["gid"] = $group["guid"];								$response["group"] = $group["group_name"];				$response["pic"] = $group["picture"];								$response["desc"] = $group["description"];								$response["options"] = $group["options"];                echo json_encode($response);            } else {                // user failed to store                $response["error"] = 1;                $response["error_msg"] = "Error al intentar crear el grupo.";                echo json_encode($response);            }        }    } else if ($tag == 'getGroupList') {		$uid = $_POST['uid'];				$result = $db->getGroupsList($uid);				if($result){						$response["success"] = 1;						$response["result"] = $result;						echo json_encode($response);						}else{					// user failed to store                $response["error"] = 1;                $response["error_msg"] = "Usted no participa en ningun grupo.";                echo json_encode($response);						}                     } else if ($tag == 'sendVote') {		$guid = $_POST['guid'];				$uid = $_POST['uid'];				$poll = utf8_encode($_POST['poll']);				$vote = utf8_encode($_POST['vote']);				$sign = $_POST['sign'];				$result = $db->vote($uid,$guid,$poll,$vote,$sign);				if($result){						$response["success"] = 1;						$response["result"] = $result;						echo json_encode($response);						}else{					// user failed to store                $response["error"] = 1;                $response["error_msg"] = "Error al registrar el voto.";                echo json_encode($response);						}                     } else if ($tag == 'createPoll') {		$guid = $_POST['guid'];				$question = utf8_encode($_POST['question']);				$desc = utf8_encode($_POST['desc']);				$answers = utf8_encode($_POST['answers']);				$end = $_POST['timedate'];				$signRequired = $_POST['signRequired'];				$result = $db->startPoll($question,$desc,$answers,$end,$guid,$signRequired);				if($result){						$response["success"] = 1;						$response["result"] = $result;						echo json_encode($response);						}else{					// user failed to store                $response["error"] = 1;                $response["error_msg"] = "Error al crear votacion.";                echo json_encode($response);						}                     } else if ($tag == 'getPollsList') {		$guid = $_POST['guid'];				$uid = $_POST['uid'];				$result = $db->getPollsList($guid);				$result2 = $db->getMyVotes($uid,$guid);				$result3 = $db->isUserAdmin($uid, $guid);				$result4 = $db->getGroupOptions($guid);				$result5 = $db->getContRequests($guid);				if($result){						$response["success"] = 1;						if($result == -1){							$response["result"] = false;						}else{								$response["result"] = $result;							}						$response["result2"] = $result2;						$response["result3"] = $result3;						$response["result4"] = $result4;						$response["result5"] = $result5;							echo json_encode($response);				}else{					// user failed to store                $response["error"] = 1;                $response["error_msg"] = "No hay votaciones activas.";                echo json_encode($response);						}                     } else if ($tag == 'searchGroup') {			$uid = $_POST['uid'];		$words = utf8_encode($_POST['request']);				$result = $db->searchGroups($words,$uid);				if($result){						$response["success"] = 1;						$response["result"] = $result;						echo json_encode($response);						}else{					// user failed to store                $response["error"] = 1;                $response["error_msg"] = "No se encontraron grupos.";                echo json_encode($response);						}                     } else if ($tag == 'userAddGroup') {		$guid = $_POST['groupid'];				$uid = $_POST['user'];				$gname = utf8_encode($_POST['gname']);				$result = $db->userAddGroup($uid, $guid, $gname);		$result2 = $db->groupAddUser($uid, $guid, false);				if($result){						$response["success"] = 1;						$response["result"] = $result;						echo json_encode($response);						}else{					// user failed to store                $response["error"] = 1;                $response["error_msg"] = "Error al añadir usuario al grupo.";                echo json_encode($response);						}                     } else if ($tag == 'userRequest') {		$guid = $_POST['guid'];				$user = utf8_encode($_POST['user']);				$result = $db->userRequest($user, $guid);				if($result){						$response["success"] = 1;						//$response["result"] = $result;						echo json_encode($response);						}else{					// user failed to store                $response["error"] = 1;                $response["error_msg"] = "Error al procesar la peticion.";                echo json_encode($response);						}                     } else if ($tag == 'getRequests') {		$guid = $_POST['guid'];				$result = $db->getGroupRequests($guid);				if($result){						$response["success"] = 1;						$response["result"] = $result;						echo json_encode($response);						}else{					// user failed to store                $response["error"] = 1;                $response["error_msg"] = "Error al recuperar peticiones pendientes.";                echo json_encode($response);						}                     } else if ($tag == 'getMembers') {		$guid = $_POST['guid'];				$result = $db->getGroupMembers($guid);				if($result){						$response["success"] = 1;						$response["result"] = $result;						echo json_encode($response);						}else{					// user failed to store                $response["error"] = 1;                $response["error_msg"] = "Error al recuperar la lista de miembros del grupo.";                echo json_encode($response);						}                     } else if ($tag == 'decideRequest') {		$guid = $_POST['guid'];				$user = utf8_encode($_POST['user']);				$choice = $_POST['choice'];				$result = $db->decideRequest($guid,$user,$choice);				if($result){						$response["success"] = 1;						$response["result"] = $result;						echo json_encode($response);						}else{					// user failed to store                $response["error"] = 1;                $response["error_msg"] = "Error al procesar la peticion.";                echo json_encode($response);						}                     } else if ($tag == 'userWithdrawGroup') {		$guid = $_POST['groupid'];				$uid = $_POST['user'];				$result = $db->isUserAdmin($uid, $guid);				if(!$result){			$result = $db->userWithdrawGroup($uid, $guid);			$result2 = $db->groupWithdrawUser($uid, $guid);			//$result = $db->getGroupsList($uid); 						if($result){								$response["success"] = 1;								//$response["result"] = $result;								echo json_encode($response);									}else{							// user failed to store					$response["error"] = 1;					$response["error_msg"] = "Error al eliminar el usuario.";					echo json_encode($response);									}		}else{						// user is admin and cannot leave the group!!                $response["error"] = 2;                $response["error_msg"] = "El administrador no puede abandonar el grupo.";                echo json_encode($response);		}				                     }  else if ($tag == 'deleteGroup') {		$guid = $_POST['groupid'];				$uid = $_POST['user'];				$result = $db->isUserAdmin($uid, $guid);				if($result){					$result = $db->deleteGroup($uid, $guid);									if($result){								$response["success"] = 1;								echo json_encode($response);									}else{							// user failed to store					$response["error"] = 1;					$response["error_msg"] = "Error al borrar el grupo.";					echo json_encode($response);									}		}else{						// user is admin and cannot leave the group!!                $response["error"] = 2;                $response["error_msg"] = "Solo el administrador puede borrar el grupo.";                echo json_encode($response);		}				                     } else if ($tag == 'setPublicKey') {		$ukey = $_POST['key'];				$uid = $_POST['user'];				$subject = utf8_encode($_POST['subject']);				$result = $db->addPK($uid,$ukey,$subject);				if($result){					$response["success"] = 1;							echo json_encode($response);					}else{						// user failed to store			$response["error"] = 1;			$response["error_msg"] = "Error al procesar el certificado.";			echo json_encode($response);					}				} else if ($tag == 'getPublicKey') {						$uid = $_POST['user'];				$result = $db->getPK($uid);				if($result){						$response["success"] = 1;						$response["result"] = $result;								echo json_encode($response);					}else{						// user failed to store			$response["error"] = 1;			$response["error_msg"] = "Error al recuperar la clave publica.";			echo json_encode($response);					}		}  else if ($tag == 'changeAdm') {						$uid = $_POST['user'];				$guid = $_POST['guid'];				$adm = $_POST['admAccount'];				if($db->isUserAdmin($adm, $guid)){			$result = $db->changeAdm($uid,$adm,$guid);					if($result){							$response["success"] = 1;									echo json_encode($response);						}else{							// user failed to store				$response["error"] = 1;				$response["error_msg"] = "Error al cambiar el administrador del grupo.";				echo json_encode($response);						}		}else{			$response["error"] = 1;				$response["error_msg"] = "Usted no es administrador del grupo.";				echo json_encode($response);				}						} else if ($tag == 'changeOptions'){				$guid = $_POST['guid'];				$options = $_POST['options'];				$result = $db->setGroupOptions($guid,$options);				if($result){						$response["success"] = 1;								echo json_encode($response);					}else{						// user failed to store			$response["error"] = 1;			$response["error_msg"] = "Error al cambiar las opciones de grupo.";			echo json_encode($response);					}			}else {
         $response["error"] = 3;
         $response["error_msg"] = "JSON ERROR";
        echo json_encode($response);
    }
} else {
    echo "Safepoll API";
}

//echo json_encode(array('success' => true, 'message' => 'You accessed my APIs!'));
?>