@Deprecated("text")
annotation class obsolete()

@Deprecated("text")
annotation class obsoleteWithParam(val text: String)

@<!DEPRECATED_SYMBOL_WITH_MESSAGE!>obsolete<!> class Obsolete

@<!DEPRECATED_SYMBOL_WITH_MESSAGE!>obsoleteWithParam<!>("text") class Obsolete2