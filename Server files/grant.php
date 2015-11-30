<?php
// include our OAuth2 Server object
require_once __DIR__.'/Oauth2Serv/server.php';
require_once 'Oauth2Serv/include/DB_Functions.php';
$db = new DB_Functions();

$token = $_GET["token"];
$petition = $db->getPetition($token);
if($petition){
	echo "done.";
}else{
	echo "error.";
}
?>