package it.redhat.hacep.rules;

import it.redhat.hacep.cluster.TestDroolsConfiguration;
import it.redhat.hacep.drools.KieSessionByteArraySerializer;
import it.redhat.hacep.model.Fact;
import it.redhat.hacep.rules.model.Gameplay;
import repeatable.Repeat;
import repeatable.RepeatRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.runtime.Channel;
import org.kie.api.runtime.KieSession;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.Date;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TestSerializedRetractRules {

    @Rule
    public RepeatRule repeatRule = new RepeatRule();

    private final static Logger logger = LoggerFactory.getLogger(TestSerializedRetractRules.class);

    private static TestDroolsConfiguration droolsConfiguration = TestDroolsConfiguration.buildRulesWithGamePlayRetract();

    private static KieSessionByteArraySerializer serializer = new KieSessionByteArraySerializer(droolsConfiguration);

    private ZonedDateTime now;

    @Mock
    private Channel outcomesChannel;

    @Before
    public void init() {
        now = ZonedDateTime.now();
    }

    @Test
    @Repeat(100)
    public void testSessionSerialization() {
        logger.info("Start test serialized rules");
        reset(outcomesChannel);

        KieSession kieSession = droolsConfiguration.getKieSession();
        kieSession.registerChannel("outcomes", outcomesChannel);

        kieSession.insert(generateFactTenSecondsAfter(1));
        kieSession.fireAllRules();

        kieSession.insert(generateFactTenSecondsAfter(1));
        kieSession.fireAllRules();

        kieSession.insert(generateFactTenSecondsAfter(1));
        kieSession.fireAllRules();

        kieSession.insert(generateFactTenSecondsAfter(1));
        kieSession.fireAllRules();

        kieSession.insert(generateFactTenSecondsAfter(1));
        kieSession.fireAllRules();

        verify(outcomesChannel, times(5)).send(any());
        verifyNoMoreInteractions(outcomesChannel);

        reset(outcomesChannel);

        byte[] kieSessionBytes = serializer.writeObject(kieSession);
        Assert.assertTrue(kieSessionBytes.length > 0);
        kieSession.dispose();

        KieSession kieSessionDeserialized = serializer.readSession(kieSessionBytes);
        kieSessionDeserialized.registerChannel("outcomes", outcomesChannel);

        kieSessionDeserialized.insert(generateFactTenSecondsAfter(1));
        kieSessionDeserialized.fireAllRules();

        kieSessionDeserialized.insert(generateFactTenSecondsAfter(1));
        kieSessionDeserialized.fireAllRules();

        kieSessionDeserialized.insert(generateFactTenSecondsAfter(1));
        kieSessionDeserialized.fireAllRules();

        kieSessionDeserialized.insert(generateFactTenSecondsAfter(1));
        kieSessionDeserialized.fireAllRules();

        kieSessionDeserialized.insert(generateFactTenSecondsAfter(1));
        kieSessionDeserialized.fireAllRules();

        verify(outcomesChannel, times(5)).send(any());
        verifyNoMoreInteractions(outcomesChannel);

        logger.info("End test serialized rules");
    }

    private Fact generateFactTenSecondsAfter(long ppid) {
        now = now.plusSeconds(10);
        return new Gameplay(ppid, ppid, new Date(now.toInstant().toEpochMilli()));
    }
}