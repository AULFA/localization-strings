#!/bin/sh

fatal()
{
  echo "fatal: $1" 1>&2
  exit 1
}

if [ $# -ne 2 ]
then
  cat 1>&2 <<EOF
Usage: core-path lfa-path
  Where: core-path is the path to the Simplified-Android-Core project
         lfa-path is the path to the LFA-Android project
EOF
  exit 1
fi

CORE_PATH="$1"
shift
LFA_ANDROID_PATH="$1"
shift

if [ ! -d "${CORE_PATH}" ]
then
  fatal "${CORE_PATH} is not a directory"
fi
if [ ! -d "${LFA_ANDROID_PATH}" ]
then
  fatal "${LFA_ANDROID_PATH} is not a directory"
fi

CORE_PATH=$(dirname "${CORE_PATH}") ||
  fatal "could not determine parent of core path"
LFA_ANDROID_PATH=$(dirname "${LFA_ANDROID_PATH}") ||
  fatal "could not determine parent of core path"

JAR_NAME=target/one.lfa.localization-strings-0.0.1-SNAPSHOT-main.jar

LFA_ANDROID_FILES="
LFA-Android/one.lfa.android.services/src/main/res/values/strings.xml
LFA-Android/one.lfa.android.services/src/main/res/values/stringsProfile.xml
"

CORE_FILES="
Simplified-Android-Core/simplified-accounts-source-spi/src/main/res/values/strings.xml
Simplified-Android-Core/simplified-app-vanilla/src/main/res/values/strings.xml
Simplified-Android-Core/simplified-app-vanilla/src/vanillaWithProfiles/res/values/strings.xml
Simplified-Android-Core/simplified-cardcreator/src/main/res/values/strings.xml
Simplified-Android-Core/simplified-main/src/main/res/values/stringsBookRevoke.xml
Simplified-Android-Core/simplified-main/src/main/res/values/stringsBoot.xml
Simplified-Android-Core/simplified-main/src/main/res/values/stringsLogin.xml
Simplified-Android-Core/simplified-main/src/main/res/values/stringsLogout.xml
Simplified-Android-Core/simplified-main/src/main/res/values/stringsMisc.xml
Simplified-Android-Core/simplified-main/src/main/res/values/stringsNotifications.xml
Simplified-Android-Core/simplified-main/src/main/res/values/stringsProfiles.xml
Simplified-Android-Core/simplified-migration-from3master/src/main/res/values/strings.xml
Simplified-Android-Core/simplified-ui-accounts/src/main/res/values/strings.xml
Simplified-Android-Core/simplified-ui-catalog/src/main/res/values/stringsCatalog.xml
Simplified-Android-Core/simplified-ui-catalog/src/main/res/values/stringsFeed.xml
Simplified-Android-Core/simplified-ui-catalog/src/main/res/values/stringsLogin.xml
Simplified-Android-Core/simplified-ui-errorpage/src/main/res/values/strings.xml
Simplified-Android-Core/simplified-ui-navigation-tabs/src/main/res/values/strings.xml
Simplified-Android-Core/simplified-ui-profiles/src/main/res/values/stringsProfiles.xml
Simplified-Android-Core/simplified-ui-settings/src/main/res/values/strings.xml
Simplified-Android-Core/simplified-ui-splash/src/main/res/values/strings.xml
Simplified-Android-Core/simplified-viewer-audiobook/src/main/res/values/strings.xml
Simplified-Android-Core/simplified-viewer-epub-readium1/src/main/res/values/strings.xml
Simplified-Android-Core/simplified-viewer-epub-readium2/src/main/res/values/strings.xml
Simplified-Android-Core/simplified-viewer-pdf/src/main/res/values/strings.xml
"

FILES=""
for FILE in ${LFA_ANDROID_FILES}
do
  FILES="${FILES} ${LFA_ANDROID_PATH}/${FILE}"
done
for FILE in ${CORE_FILES}
do
  FILES="${FILES} ${CORE_PATH}/${FILE}"
done

exec java -jar "${JAR_NAME}" ${FILES}
