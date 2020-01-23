FROM quay.io/wildfly/wildfly-runtime-centos7:latest

COPY --from=demo-server-build /s2i-output/server $JBOSS_HOME
USER root
RUN chown -R jboss:root $JBOSS_HOME && chmod -R ug+rwX $JBOSS_HOME
RUN ln -s $JBOSS_HOME /wildfly
USER jboss

# ENV JAVA_OPTS_APPEND="-Dwildfly.config.url=$JBOSS_HOME/standalone/configuration/custom-config.xml"

CMD $JBOSS_HOME/bin/openshift-launch.sh
