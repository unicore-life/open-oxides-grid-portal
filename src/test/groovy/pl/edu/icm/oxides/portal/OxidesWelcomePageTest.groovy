package pl.edu.icm.oxides.portal

import geb.spock.GebReportingSpec
import org.springframework.boot.context.embedded.LocalServerPort
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener
import org.springframework.test.context.transaction.TransactionalTestExecutionListener

@ContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestExecutionListeners([DependencyInjectionTestExecutionListener, TransactionalTestExecutionListener])
@TestPropertySource('classpath:application-test.properties')
class OxidesWelcomePageTest extends GebReportingSpec {
    @LocalServerPort
    protected int port

    def 'should welcome with page'() {
        when:
        go "http://localhost:${port}"

        then:
        title == 'Oxides Grid Portal'
    }
}
