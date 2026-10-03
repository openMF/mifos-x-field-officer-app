// (no package decl — see note in the config/ fixtures.)
val ProjectNetworkModule = module {
    includes(GeneratedApiBindings)
    restApi("main") { it.createMainApi() }
}
