// (no package decl: fixture files are read by the gate, not compiled — a package would
// make detekt's InvalidPackageDeclaration fire on their fixture path.)
object AppSupabaseAnonKeys {
    private val byId: Map<String, String> = mapOf(
        "sb" to "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.PLACEHOLDER",
    )
    fun forId(id: String): String = byId[id].orEmpty()
}
