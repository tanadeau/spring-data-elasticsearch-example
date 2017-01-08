#!/bin/bash
SCRIPT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

source $SCRIPT_DIR/.keycloak_settings.sh

read -p "Enter Username: " myname
read -s -p "Enter Password: " mypass

export val=$(http --verify=no --ssl=ssl2.3 -f -a $KC_CLIENTID_SECRET POST $KC_TOKEN_URL grant_type=password client_id=postman username=$myname password=$mypass scope=offline_access)

echo
echo export KC_REFRESH_TOKEN=\"$(echo $val | jq -r '.refresh_token')\"
