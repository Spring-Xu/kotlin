// PSI_ELEMENT: org.jetbrains.kotlin.psi.JetNamedFunction
// OPTIONS: usages
// FIND_BY_REF
// WITH_FILE_NAME
package usages

import library.*

class J {
    static void test() {
        LibraryPackage.foo();
    }
}