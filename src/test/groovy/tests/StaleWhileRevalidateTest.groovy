package tests

import spock.lang.Unroll

import static java.time.Duration.ofMillis
import static java.util.concurrent.TimeUnit.SECONDS

class StaleWhileRevalidateTest extends WiremockCdnTest {

    @Unroll
    def 'when origin updated but slow and #invalidationActionName, #cacheSettings returns #expectedResponse'() {

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
                .after(ofMillis(500))
                .returns("new response")

        and:
            invalidationAction.call(path)

        then:
            cdn.get(path).body() == expectedResponse

        where:
            invalidationActionName  | invalidationAction                  | cacheSettings               | expectedResponse
            'cached response stale' | { SECONDS.sleep(secondsToStale+1) } | ''                          | 'new response'
            'cached response stale' | { SECONDS.sleep(secondsToStale+1) } | 'stale-while-revalidate=30' | 'cached response'
            'cached response stale' | { SECONDS.sleep(secondsToStale+1) } | 'stale-while-revalidate=1'  | 'new response'
            'cache soft purged'     | { fastly.softPurgeByKey(it)}        | ''                          | 'new response'
            'cache soft purged'     | { fastly.softPurgeByKey(it)}        | 'stale-while-revalidate=30' | 'cached response'
            'cache soft purged'     | { fastly.softPurgeByKey(it)}        | 'stale-while-revalidate=1'  | 'new response'
    }
}
