package com.skillw.asaka.core.error

import com.skillw.asaka.core.Span

class AsahiTypeException(message: String, vararg sources: Span) : AsahiException(message, *sources)