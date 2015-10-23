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
                (CacheControl): "max-age=$secondsToStale,$cacheSettings",
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
            invalidationActionName  | invalidationAction                                | cacheSettings                                                               | expectedResponse

            'cache soft purged'     | { fastly.softPurgeByKey(it) }                     | ''                                                                          | 'new response'
            'cache soft purged'     | { fastly.softPurgeByKey(it) && SECONDS.sleep(2) } | 'stale-while-revalidate=1'                                                  | 'new response'
            'cache soft purged'     | { fastly.softPurgeByKey(it) && SECONDS.sleep(3) } | 'stale-while-revalidate=1,stale-if-error=1'                                 | 'new response'
            'cache soft purged'     | { fastly.softPurgeByKey(it) }                     | "stale-while-revalidate=${secondsToStale}"                                  | 'cached response'
            'cache soft purged'     | { fastly.softPurgeByKey(it) }                     | "stale-while-revalidate=${secondsToStale},stale-if-error=${secondsToStale}" | 'cached response'

            'cache hard purged'     | { fastly.purgeByKey(it) }                         | ''                                                                          | 'new response'
            'cache hard purged'     | { fastly.purgeByKey(it) && SECONDS.sleep(2) }     | 'stale-while-revalidate=1'                                                  | 'new response'
            'cache hard purged'     | { fastly.purgeByKey(it) && SECONDS.sleep(3) }     | 'stale-while-revalidate=1,stale-if-error=1'                                 | 'new response'
            'cache hard purged'     | { fastly.purgeByKey(it) }                         | "stale-while-revalidate=${secondsToStale}"                                  | 'new response'
            'cache hard purged'     | { fastly.purgeByKey(it) }                         | "stale-while-revalidate=${secondsToStale},stale-if-error=${secondsToStale}" | 'new response'

            'cached response stale' | { SECONDS.sleep(secondsToStale+2) }               | ''                                                                          | 'new response'
            'cached response stale' | { SECONDS.sleep(secondsToStale+2) }               | 'stale-while-revalidate=1'                                                  | 'new response'
            'cached response stale' | { SECONDS.sleep(secondsToStale+3) }               | 'stale-while-revalidate=1,stale-if-error=1'                                 | 'new response'
            'cached response stale' | { SECONDS.sleep(secondsToStale+2) }               | "stale-while-revalidate=${secondsToStale}"                                  | 'cached response'
            'cached response stale' | { SECONDS.sleep(secondsToStale+2) }               | "stale-while-revalidate=${secondsToStale},stale-if-error=${secondsToStale}" | 'cached response'
    }
}
