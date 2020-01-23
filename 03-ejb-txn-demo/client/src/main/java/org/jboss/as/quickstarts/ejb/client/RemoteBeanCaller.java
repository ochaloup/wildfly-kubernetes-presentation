/*
 * JBoss, Home of Professional Open Source
 * Copyright 2019, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.as.quickstarts.ejb.client;

import org.jboss.as.quickstarts.ejb.server.RemoteBeanInterface;
import org.jboss.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.Queue;
import javax.naming.NamingException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

/**
 * EJB which runs the remote calls to the EJBs.
 * We use the EJB here for benefit of automatic transaction management.
 */
@Stateless
public class RemoteBeanCaller {
    private static final Logger log = Logger.getLogger(RemoteBeanCaller.class);

    @Resource(mappedName = "java:/JmsXA")
    ConnectionFactory connectionFactory;

    @Resource(mappedName = "java:jboss/exported/jms/queue/quequeB")
    Queue queue;

    /**
     * Stateless no transaction.
     */
    @TransactionAttribute(value = TransactionAttributeType.NOT_SUPPORTED)
    public List<String> statelessNoTxBeanCall() throws NamingException, RemoteException {
        log.debugf("Calling without transaction to StatelessBean.successOnCall()");

        RemoteBeanInterface bean = RemoteLookupHelper.lookupRemoteEJBOutbound("StatelessBean", RemoteBeanInterface.class, false);
        return Arrays.asList(
                bean.successOnCall(),
                bean.successOnCall(),
                bean.successOnCall(),
                bean.successOnCall(),
                bean.successOnCall(),
                bean.successOnCall(),
                bean.successOnCall()
        );
    }

    /**
     * Stateless with transaction.
     */
    public List<String> statelessBeanCall() throws NamingException, RemoteException {
        log.debugf("Calling with transaction to StatelessBean.successOnCall()");

        try (JMSContext context = connectionFactory.createContext();){
            context.createProducer().send(queue,"Bilbo Baggins");
        }

        RemoteBeanInterface bean = RemoteLookupHelper.lookupRemoteEJBOutbound("StatelessBean", RemoteBeanInterface.class, false);
        return Arrays.asList(
            bean.successOnCall(),
            bean.successOnCall()
        );
    }


    /**
     * Stateful not transaction.
     */
    @TransactionAttribute(value = TransactionAttributeType.NOT_SUPPORTED)
    public List<String> statefulNoTxBeanCall() throws NamingException, RemoteException {
        log.debugf("Calling without transaction to StatefulBean.successOnCall()");

        RemoteBeanInterface bean = RemoteLookupHelper.lookupRemoteEJBOutbound("StatefulBean", RemoteBeanInterface.class, true);
        return Arrays.asList(
                bean.successOnCall(),
                bean.successOnCall()
        );
    }

    /**
     * Crashing remote server.
     */
    public String fail() throws NamingException, RemoteException {
        log.debugf("Calling with failure to StatelessBean.failOnCall()");

        try (JMSContext context = connectionFactory.createContext();){
            context.createProducer().send(queue,"Fell Rider");
        }

        RemoteBeanInterface bean = RemoteLookupHelper.lookupRemoteEJBOutbound("StatelessBean", RemoteBeanInterface.class, false);
        return bean.failOnCall();
    }
}
