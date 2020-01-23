#!/usr/bin/env bash
echo "Executing clustering.cli"
$JBOSS_HOME/bin/jboss-cli.sh --file=$JBOSS_HOME/extensions/clustering.cli
