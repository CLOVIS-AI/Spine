package opensavvy.spine.typed.server

import io.ktor.server.application.*
import opensavvy.spine.typed.Parameters

interface TypedResponseScope<In : Any, Params : Parameters> {
	val call: ApplicationCall

	val body: In

	val parameters: Params
}

@PublishedApi
internal class TypedResponseScopeImpl<In : Any, Params : Parameters>(
	override val call: ApplicationCall,
	override val body: In,
	override val parameters: Params,
) : TypedResponseScope<In, Params>
