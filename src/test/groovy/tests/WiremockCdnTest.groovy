package tests

import client.FastlyClient
import client.HttpClient
import com.github.tomakehurst.wiremock.client.WireMock
import spock.lang.Shared
import spock.lang.Specification
import tests.support.FastlyToHeroku
import tests.support.WireMockClient
import tests.support.WireMockOnHeroku

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic
import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric

abstract class WiremockCdnTest extends Specification {

    static final String appName = "wiremock-fastly-tests-${randomAlphanumeric(5).toLowerCase()}"
    static final String fastlyApiKey = System.getenv('FASTLY_API_KEY')

    @Shared WireMockClient origin
    @Shared HttpClient cdn
    @Shared FastlyClient fastly

    static def secondsToStale = 5

    String path

    def setupSpec() {

        fastly = setupFastly(fastlyApiKey)

        WireMockOnHeroku.setup(appName)

        origin = new WireMockClient(new WireMock("${appName}.herokuapp.com", 80))

        cdn = new HttpClient("https://${appName}.global.ssl.fastly.net/")

        while (cdn.get("/__admin/").statusCode() != 200) {
            fastly.purgeAll()
            Thread.sleep(1000L)
        }
    }

    FastlyClient setupFastly(String apiKey) {
        String serviceId = FastlyToHeroku.configureFor(apiKey, appName)
        fastly = new FastlyClient(serviceId, apiKey)
    }

    def setup() {
        path = '/'+randomAlphabetic(10)
        origin.reset()
    }

    def cleanupSpec() {
        origin.reset()
        fastly.purgeAll()
    }
}