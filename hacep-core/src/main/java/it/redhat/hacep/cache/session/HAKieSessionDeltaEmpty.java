/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.redhat.hacep.cache.session;

import it.redhat.hacep.configuration.DroolsConfiguration;
import it.redhat.hacep.drools.KieSessionByteArraySerializer;
import org.infinispan.atomic.Delta;
import org.infinispan.atomic.DeltaAware;
import org.infinispan.commons.marshall.AdvancedExternalizer;
import org.infinispan.commons.util.Util;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Set;
import java.util.concurrent.Executor;

import static it.redhat.hacep.cache.session.JDGExternalizerIDs.HASessionDeltaID;

public class HAKieSessionDeltaEmpty implements Delta {

    private final KieSessionByteArraySerializer serializer;
    private final Executor executor;
    private final DroolsConfiguration droolsConfiguration;

    public HAKieSessionDeltaEmpty() {
        this(null, null, null);
    }

    public HAKieSessionDeltaEmpty(DroolsConfiguration droolsConfiguration, KieSessionByteArraySerializer serializer, Executor executor) {
        this.serializer = serializer;
        this.executor = executor;
        this.droolsConfiguration = droolsConfiguration;
    }

    @Override
    public DeltaAware merge(DeltaAware d) {
        if (d != null) {
            return d;
        }
        return new HAKieSerializedSession(droolsConfiguration, serializer, executor);
    }

    public static class HASessionDeltaEmptyExternalizer implements AdvancedExternalizer<HAKieSessionDeltaEmpty> {

        private final KieSessionByteArraySerializer serializer;
        private final Executor executor;
        private final DroolsConfiguration droolsConfiguration;

        public HASessionDeltaEmptyExternalizer(DroolsConfiguration droolsConfiguration, KieSessionByteArraySerializer serializer,
                                               Executor executor) {
            this.serializer = serializer;
            this.executor = executor;
            this.droolsConfiguration = droolsConfiguration;
        }

        @Override
        public Set<Class<? extends HAKieSessionDeltaEmpty>> getTypeClasses() {
            return Util.asSet(HAKieSessionDeltaEmpty.class);
        }

        @Override
        public Integer getId() {
            return HASessionDeltaID.getId();
        }

        @Override
        public void writeObject(ObjectOutput output, HAKieSessionDeltaEmpty object) throws IOException {
        }

        @Override
        public HAKieSessionDeltaEmpty readObject(ObjectInput input) throws IOException, ClassNotFoundException {
            return new HAKieSessionDeltaEmpty(droolsConfiguration, serializer, executor);
        }
    }
}
