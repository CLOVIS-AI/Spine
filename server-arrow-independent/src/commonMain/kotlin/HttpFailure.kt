package opensavvy.spine.server.arrow.independent

import io.ktor.http.*
import io.ktor.util.reflect.*

class HttpFailure(
	val body: Any,
	val code: HttpStatusCode,
	val type: TypeInfo,
)

inline fun <reified Out : Any> HttpFailure(
	body: Out,
	code: HttpStatusCode,
) = HttpFailure(body, code, typeInfo<Out>())
