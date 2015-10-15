package tests.support

import client.FastlyClient

class FastlyToHeroku {

    static String configureFor(String apiKey, String appName) {
        def fastly = new FastlyClient('', apiKey)

        def service = fastly.services().find { it.name == appName }

        if (!service) {
            service = fastly.createService(appName, "${appName}.herokuapp.com")
            if (!System.getenv('APP_NAME')) {
                Runtime.runtime.addShutdownHook {
                    fastly.destroyService(service.id)
                }
            }
        }

        service.id
    }
}
