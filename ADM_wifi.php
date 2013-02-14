<?php

ini_set('display_errors', 'On');
error_reporting(E_ALL | E_STRICT);

# access login to neufbox 6
$username = "admin";
$password = "****";

# récupération du token
$tokenURL= "http://192.168.1.1/api/1.0/?method=auth.getToken";
$tokenXML = simplexml_load_file( $tokenURL );
$token = $tokenXML->auth[0]->attributes()->token; 

# gestion du username
$username_hash = hash("sha256",$username);
$username_hmac = hash_hmac ("sha256", $username_hash, $token);
 
# gestion du mot de passe
$password_hash= hash("sha256",$password);
$password_hmac= hash_hmac ("sha256", $password_hash, $token);
 
# hash final
$hash = $username_hmac.$password_hmac;

# authentification
$authURL = "http://192.168.1.1/api/1.0/?method=auth.checkToken&token=".$token."&hash=".$hash;
$authXML = simplexml_load_file( $authURL );

# logged in !
if( $authXML->attributes()->stat == "ok" ) {
    
    #final token for further authentification 
    $authToken = $authXML->auth->attributes()->token;

    // Connexion au hostpost
        /*
        $clientsHotspotURL = "http://neufbox/api/1.0/?method=hotspot.getClientList&token=".$authToken;
        $clientsHotspotXML = simplexml_load_file( $clientsHotspotURL );
        print_r($clientsHotspotXML);
        $clientsHotspot = $clientsHotspotXML->host;
        echo "Il y a ".count($clientsHotspot)." clients connectés au wlan.";
        */

    // Connexion au WLAN
        /*
            $clientsWlanURL = "http://neufbox/api/1.0/?method=wlan.getClientList&token=".$authToken;
            $clientsWlanXML = simplexml_load_file( $clientsWlanURL );
            $clientsLan = $clientsWlanXML->client;
            print_r($clientsWlanXML);
            echo "Il y a ".count($clientsLan)." clients connectés au wlan.";
        */

    // // Connexion au LAN
    $clientsLanURL = "http://neufbox/api/1.0/?method=lan.getHostsList&token=".$authToken;
    $clientsLanXML = simplexml_load_file( $clientsLanURL );
    // print_r($clientsLanXML);
    
    $totalLAN = 0; #total number of people actually connected
    $connectedClients = array();

    # remove all offline hosts from count
    foreach($clientsLanXML->host as $client){
        // echo $client->attributes()->status . "<br />";
        if ( $client->attributes()->status == "online") {
            array_push( $connectedClients, $client->attributes() );
            $totalLAN++;
        }
    }
    
    // echo "Il y a ".$totalLAN ." clients actuellement connectés au lan.";
    // print_r($clientsWifi);
    // echo "</pre>";

    $output = $connectedClients;

    // JSON output
    header("Expires: Mon, 26 Jul 1997 05:00:00 GMT"); 
    header("Last-Modified: " . gmdate( "D, d M Y H:i:s" ) . "GMT"); 
    header("Cache-Control: no-cache, must-revalidate"); 
    header("Pragma: no-cache");
    header("Content-type: application/json");

    echo json_encode($output);
    
} else {
    echo "login failed !";
    echo "<pre>";
    print_r($authXML);
    echo "</pre>";
}



?>
