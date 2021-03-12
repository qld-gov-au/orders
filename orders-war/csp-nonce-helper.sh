#!/bin/bash

THIS_FILE=`basename "$0"`
INCLUDE_FILES_EGREP_PATTERN="[.](js|html|ftl)$"
EXCLUDE_FILES_EGREP_PATTERN="(node_modules|gulpfile[.]js|$THIS_FILE)$"
SCRIPT_TAGS_PATTERN="(<)(script|link|style)"
NONCE_ATTRIB_VAL='nonce="${__csp_nonce}"'
SCRIPT_TAGS_SUBSTITUTION="\1\2 $NONCE_ATTRIB_VAL"

# remove all nonce attribs before bulk scan and additions
find ./ -type f \
  | egrep $INCLUDE_FILES_EGREP_PATTERN \
  | egrep -v $EXCLUDE_FILES_EGREP_PATTERN \
  | xargs egrep -l $SCRIPT_TAGS_PATTERN \
  | xargs sed -i -- "s/ $NONCE_ATTRIB_VAL//g"

# bulk scan and add nonce attribs
find ./ -type f \
  | egrep $INCLUDE_FILES_EGREP_PATTERN \
  | egrep -v $EXCLUDE_FILES_EGREP_PATTERN \
  | xargs egrep -l $SCRIPT_TAGS_PATTERN \
  | xargs sed -r -i -- "s/$SCRIPT_TAGS_PATTERN/\1\2 $NONCE_ATTRIB_VAL/g"
