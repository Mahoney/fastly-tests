package tests.support

import client.HttpClient
import com.google.common.io.Files
import jodd.http.HttpException

import java.util.concurrent.TimeUnit

import static Exec.exec

class WireMockOnHeroku {

    static void setup(String appName) {
        def client = new HttpClient("http://${appName}.herokuapp.com/")
        if (wiremockExists(client)) {
            def tempdir = Files.createTempDir()
            tempdir.deleteOnExit()
            exec(tempdir, "git clone git@github.com:Mahoney/wiremock-heroku.git")
            exec(new File(tempdir, 'wiremock-heroku'), "./wiremock-heroku.sh $appName")
            exec("heroku domains:add ${appName}.global.ssl.fastly.net --app $appName")

            if (!System.getenv('HEROKU_APP_NAME')) {
                Runtime.runtime.addShutdownHook {
                    exec("heroku apps:destroy --app $appName --confirm $appName")
                }
            }
        }
    }

    private static boolean wiremockExists(HttpClient client, int retries = 0) {
        try {
            return client.get("/__admin/").statusCode() == 404
        } catch (HttpException e) {
            if (retries < 5) {
                TimeUnit.SECONDS.sleep(1);
                return wiremockExists(client, retries + 1)
            } else {
                throw e
            }
        }
    }
}
