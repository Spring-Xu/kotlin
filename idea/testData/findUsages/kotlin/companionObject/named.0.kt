// PSI_ELEMENT: org.jetbrains.kotlin.psi.JetObjectDeclaration
// OPTIONS: usages
class Foo {
    companion object <caret>Bar {
        fun f() {
        }
    }
}