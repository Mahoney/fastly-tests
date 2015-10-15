package tests

import spock.lang.Unroll

import static java.util.concurrent.TimeUnit.SECONDS

class StaleIfErrorTest extends WiremockCdnTest {

    @Unroll
    def 'when origin broken and #invalidationActionName, #cacheSettings returns #expectedResponse'() {

        given:
            origin.get(path).returns(
                'Surrogate-Key': path,
                'Cache-Control': "max-age=$secondsToStale, $cacheSettings",
                'cached response'
            )

        and:
            cdn.warm(path)

        when:
            origin
                .get(path)
                .returns(503, "broken response")

        and:
            invalidationAction.call(path)

        then:
            cdn.get(path).body() == expectedResponse

        where:
            invalidationActionName  | invalidationAction                  | cacheSettings       | expectedResponse
            'cached response stale' | { SECONDS.sleep(secondsToStale+1) } | ''                  | 'broken response'
            'cached response stale' | { SECONDS.sleep(secondsToStale+1) } | 'stale-if-error=30' | 'cached response'
            'cached response stale' | { SECONDS.sleep(secondsToStale+1) } | 'stale-if-error=1'  | 'broken response'
            'cache soft purged'     | { fastly.softPurgeByKey(it)}        | ''                  | 'broken response'
            'cache soft purged'     | { fastly.softPurgeByKey(it)}        | 'stale-if-error=30' | 'cached response'
            'cache soft purged'     | { fastly.softPurgeByKey(it)}        | 'stale-if-error=1'  | 'broken response'
    }
}
