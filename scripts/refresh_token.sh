#!/bin/bash
SCRIPT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

source $SCRIPT_DIR/.keycloak_settings.sh

export val=$(http --verify=no --ssl=ssl2.3 -f -a $KC_CLIENTID_SECRET POST $KC_TOKEN_URL grant_type=refresh_token client_id=postman refresh_token="$KC_REFRESH_TOKEN")

echo export KC_REFRESH_TOKEN=\"$(echo $val | jq -r '.refresh_token')\"
echo export KC_ACCESS_TOKEN=\"$(echo $val | jq -r '.access_token')\"
