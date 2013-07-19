#!env zsh

source curl_rest.sh

file=$1

file_location_line=$(POST $SRV/file -F file=@$file 2>&1| grep 'Location: ' |sed 's///')
file_uri=$(echo $file_location_line | grep -o 'http.*')
echo $file_uri
