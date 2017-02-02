package pl.edu.icm.oxides

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import spock.lang.Specification

import static java.util.Objects.nonNull

@ContextConfiguration
@SpringBootTest
@TestPropertySource('classpath:application-test.properties')
class OxidesGridPortalTest extends Specification {
    @Autowired
    ApplicationContext context

    def 'should exists application context'() {
        expect:
        nonNull(context)
    }
}
