package tests.support

class Exec {

    static void exec(String command) {
        exec(new File('.'), command)
    }

    static void exec(File dir, String command) {
        println "Running $command"
        def proc = command.execute((String[]) null, dir)
        proc.waitForProcessOutput(System.out, System.err)
        assert proc.exitValue() == 0
    }
}
