FROM quay.io/wildfly/wildfly-runtime-centos7:latest

# COPY --from=helloworld-wildfly-centos7 /s2i-output/server $JBOSS_HOME
COPY --from=helloworld-wildfly-centos7-galleon /s2i-output/server $JBOSS_HOME
USER root
RUN chown -R jboss:root $JBOSS_HOME && chmod -R ug+rwX $JBOSS_HOME
RUN ln -s $JBOSS_HOME /wildfly
USER jboss
CMD $JBOSS_HOME/bin/openshift-launch.sh
