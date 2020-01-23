#!/usr/bin/env bash
echo "Executing remote-configuration.cli"
$JBOSS_HOME/bin/jboss-cli.sh --file=$JBOSS_HOME/extensions/remote-configuration.cli
