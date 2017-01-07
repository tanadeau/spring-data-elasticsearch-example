#!/bin/bash

http POST localhost:8080/post AUTHORIZATION:"BEARER $KC_ACCESS_TOKEN" tags:='[]' visibilities:='["user", "something"]'
