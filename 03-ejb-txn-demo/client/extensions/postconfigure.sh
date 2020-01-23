#!/usr/bin/env bash
echo "Executing kubernetes-ejb-configuration.cli"
$JBOSS_HOME/bin/jboss-cli.sh --file=$JBOSS_HOME/extensions/kubernetes-ejb-configuration.cli
echo "Executing kubernetes-jms-configuration.cli"
$JBOSS_HOME/bin/jboss-cli.sh --file=$JBOSS_HOME/extensions/kubernetes-jms-configuration.cli