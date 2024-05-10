package com.skillw.asaka.core.error

import com.skillw.asaka.core.Span
import javax.script.ScriptException

abstract class AsahiException
protected constructor(message: String, vararg sources: Span) : ScriptException(message + generateMessage(*sources))
