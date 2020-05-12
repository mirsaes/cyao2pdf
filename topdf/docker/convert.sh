#!/bin/bash

#user name, toConvertFile
userToConvertAs=$1
fileToConvert=$2
chown root:cyao2pdf $fileToConvert
echo "starting to convert file=$fileToConvert"
su $userToConvertAs -s /bin/sh <<EOF
set -e
soffice --nologo --headless --convert-to pdf /topdf/upload-dir/$fileToConvert --outdir /topdf/upload-dir
EOF