package test

// imports should be resolved
import test.topLevelFun
import test.topLevelProperty

@HiddenDeclaration
fun topLevelFun(){}

@HiddenDeclaration
var topLevelProperty = 1

@HiddenDeclaration
fun String.topLevelExtensionFun(){}

@HiddenDeclaration
val String.topLevelExtensionProperty: Int get() = 1

open class A {
    constructor(<!UNUSED_PARAMETER!>p<!>: Int) : this(<!TYPE_MISMATCH!>""<!>) {}

    @HiddenDeclaration
    constructor(<!UNUSED_PARAMETER!>s<!>: String){}

    @HiddenDeclaration
    open fun memberFun(){}

    @HiddenDeclaration
    val memberProperty = 1

    @HiddenDeclaration
    fun String.memberExtensionFun(){}

    @HiddenDeclaration
    val String.memberExtensionProperty: Int get() = 1

    fun foo() {
        <!UNRESOLVED_REFERENCE!>topLevelFun<!>()
        <!UNRESOLVED_REFERENCE, VARIABLE_EXPECTED!>topLevelProperty<!><!DEBUG_INFO_ELEMENT_WITH_ERROR_TYPE!>++<!>
        "".<!UNRESOLVED_REFERENCE!>topLevelExtensionFun<!>()
        "".<!UNRESOLVED_REFERENCE!>topLevelExtensionProperty<!>

        <!UNRESOLVED_REFERENCE!>memberFun<!>()
        <!UNRESOLVED_REFERENCE!>memberProperty<!>
        "".<!UNRESOLVED_REFERENCE!>memberExtensionFun<!>()
        "".<!UNRESOLVED_REFERENCE!>memberExtensionProperty<!>

        A(<!TYPE_MISMATCH!>""<!>)
    }
}

interface I {
    @HiddenDeclaration
    fun foo1()

    @HiddenDeclaration
    fun foo2()
}

<!ABSTRACT_MEMBER_NOT_IMPLEMENTED!>class X<!> : I {
    override fun foo1() {
    }
}

class B : A(<!TYPE_MISMATCH!>""<!>) {
    // still can override it
    override fun memberFun() {
        super.<!UNRESOLVED_REFERENCE!>memberFun<!>() // but cannot call super :)
    }
}

class C : A {
    constructor() : super(<!TYPE_MISMATCH!>""<!>)
}