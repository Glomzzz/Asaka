package com.skillw.asaka.core.error

import com.skillw.asaka.core.Span

class AsahiSyntaxException(message: String, vararg sources: Span) : AsahiException(message, *sources)
