// Serve the test bundle's Compose resources. The generated karma.conf.js serves only the test
// bundle, so every `composeResources/...` fetch 404'd: strings came back empty and fonts never
// loaded, and a test asserting on visible text could not pass in the browser.
// Karma serves files under its basePath at `/base/`, so the resource URL is proxied there.
const path = require("path");
config.files.push({
    pattern: path.join(config.basePath, "kotlin/composeResources/**/*"),
    included: false,
    served: true,
    watched: false,
});
config.proxies["/composeResources/"] = "/base/kotlin/composeResources/";
