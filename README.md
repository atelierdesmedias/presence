# About
=======

Application used at Atelier des Médias to gather various statistics about people at the office and display then.

## NetworkStat
=============

Android application getting Wifi logs from neufbox server and sending then to Atelier des Medias remote server.

## UI
====

Receive and store log in the database and display then.

* PresenceCode.StoreService: receive and store wifi statistics in the xwiki.adm_presence_stat table

TODO:
* Presence.WebHome: application entry point

# Resources
===========
## Wifiscan.php
Scan the number of connections to a neufbox

You need to update the user/password info 

What the script does :

    # Log a user into the neufbox

    # Get the auth token from the neufbox
    curl http://neufbox/api/1.0/?method=auth.getToken

    # Parse the XML answer
    <?xml version="1.0" encoding="UTF-8"?>
    <rsp stat="ok" version="1.0">
         <auth token="bc9e501ade45c0fb2a50f48***********" method="all" />
    </rsp>

    # Then get the number of clients using auth token
    http://neufbox/api/1.0/?method=hotspot.getClientList\&token=bc9e501ade45c0fb2a50f48***********
    
    # Return json with some info

