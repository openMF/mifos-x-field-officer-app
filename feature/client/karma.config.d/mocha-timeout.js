// Karma runs the browser web tests through Mocha, whose default per-test timeout is 2000ms.
// The UI tests that read string resources wait in REAL time for them: the browser fetches each
// module's composeResources asynchronously, so a 2s test can time out before the first string
// arrives — which reads like a hung browser rather than a slow resource load.
//
// Set on the Karma CLIENT, as core/database does (`useMocha {}` in Gradle would swap the browser out).
config.set({
    client: {
        mocha: {
            timeout: 30000,
        },
    },
});
