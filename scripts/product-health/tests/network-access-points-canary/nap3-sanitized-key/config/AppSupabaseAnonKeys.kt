// (no package decl: fixture files are read by the gate, not compiled — a package would
// make detekt's InvalidPackageDeclaration fire on their fixture path.)
object AppSupabaseAnonKeys {
    private val byId: Map<String, String> = mapOf(
        "sb" to kpt.core.network.BuildKonfig.SB_ANON_KEY,
    )
    fun forId(id: String): String = byId[id].orEmpty()
}
