@file:Suppress("DEPRECATION_ERROR")

package opensavvy.spine.server.arrow

import arrow.core.raise.Raise
import arrow.core.raise.recover
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import opensavvy.spine.api.Endpoint
import opensavvy.spine.api.FailureSpec.ByCode
import opensavvy.spine.api.FailureSpec.Or
import opensavvy.spine.api.Parameters
import opensavvy.spine.server.TypedResponseScope
import opensavvy.spine.server.fail
import opensavvy.spine.server.route
import kotlin.jvm.JvmName

@KtorDsl
@JvmName("routeOrRaise1")
inline fun <reified In : Any, reified Out : Any, reified F1 : Any, reified Params : Parameters> Route.routeWithRaise(
	endpoint: Endpoint<In, Out, Or<*, ByCode<F1>>, Params>,
	crossinline block: suspend context(Raise<F1>) TypedResponseScope<In, Out, Or<*, ByCode<F1>>, Params>.() -> Unit,
) {
	route(endpoint) response@{
		recover(
			block = { block() },
			recover = { it: F1 -> fail(it) }
		)
	}
}

@KtorDsl
@JvmName("routeOrRaise2")
inline fun <reified In : Any, reified Out : Any, reified F1 : Any, reified F2 : Any, reified Params : Parameters> Route.routeWithRaise(
	endpoint: Endpoint<In, Out, Or<Or<*, ByCode<F1>>, ByCode<F2>>, Params>,
	crossinline block: suspend context(Raise<F1>, Raise<F2>) TypedResponseScope<In, Out, Or<Or<*, ByCode<F1>>, ByCode<F2>>, Params>.() -> Unit,
) {
	route(endpoint) response@{
		recover(
			block = {
				recover(
					block = { block() },
					recover = { it: F2 -> fail(it) }
				)
			},
			recover = { it: F1 -> fail(it) }
		)
	}
}

@KtorDsl
@JvmName("routeOrRaise3")
inline fun <reified In : Any, reified Out : Any, reified F1 : Any, reified F2 : Any, reified F3 : Any, reified Params : Parameters> Route.routeWithRaise(
	endpoint: Endpoint<In, Out, Or<Or<Or<*, ByCode<F1>>, ByCode<F2>>, ByCode<F3>>, Params>,
	crossinline block: suspend context(Raise<F1>, Raise<F2>, Raise<F3>) TypedResponseScope<In, Out, Or<Or<Or<*, ByCode<F1>>, ByCode<F2>>, ByCode<F3>>, Params>.() -> Unit,
) {
	route(endpoint) response@{
		recover(
			block = {
				recover(
					block = {
						recover(
							block = { block() },
							recover = { it: F3 -> fail(it) }
						)
					},
					recover = { it: F2 -> fail(it) }
				)
			},
			recover = { it: F1 -> fail(it) }
		)
	}
}

@KtorDsl
@JvmName("routeOrRaise4")
inline fun <reified In : Any, reified Out : Any, reified F1 : Any, reified F2 : Any, reified F3 : Any, reified F4 : Any, reified Params : Parameters> Route.routeWithRaise(
	endpoint: Endpoint<In, Out, Or<Or<Or<Or<*, ByCode<F1>>, ByCode<F2>>, ByCode<F3>>, ByCode<F4>>, Params>,
	crossinline block: suspend context(Raise<F1>, Raise<F2>, Raise<F3>, Raise<F4>) TypedResponseScope<In, Out, Or<Or<Or<Or<*, ByCode<F1>>, ByCode<F2>>, ByCode<F3>>, ByCode<F4>>, Params>.() -> Unit,
) {
	route(endpoint) response@{
		recover(
			block = {
				recover(
					block = {
						recover(
							block = {
								recover(
									block = { block() },
									recover = { it: F4 -> fail(it) }
								)
							},
							recover = { it: F3 -> fail(it) }
						)
					},
					recover = { it: F2 -> fail(it) }
				)
			},
			recover = { it: F1 -> fail(it) }
		)
	}
}

@KtorDsl
@JvmName("routeOrRaise5")
inline fun <reified In : Any, reified Out : Any, reified F1 : Any, reified F2 : Any, reified F3 : Any, reified F4 : Any, reified F5 : Any, reified Params : Parameters> Route.routeWithRaise(
	endpoint: Endpoint<In, Out, Or<Or<Or<Or<Or<*, ByCode<F1>>, ByCode<F2>>, ByCode<F3>>, ByCode<F4>>, ByCode<F5>>, Params>,
	crossinline block: suspend context(Raise<F1>, Raise<F2>, Raise<F3>, Raise<F4>, Raise<F5>) TypedResponseScope<In, Out, Or<Or<Or<Or<Or<*, ByCode<F1>>, ByCode<F2>>, ByCode<F3>>, ByCode<F4>>, ByCode<F5>>, Params>.() -> Unit,
) {
	route(endpoint) response@{
		recover(
			block = {
				recover(
					block = {
						recover(
							block = {
								recover(
									block = {
										recover(
											block = { block() },
											recover = { it: F5 -> fail(it) }
										)
									},
									recover = { it: F4 -> fail(it) }
								)
							},
							recover = { it: F3 -> fail(it) }
						)
					},
					recover = { it: F2 -> fail(it) }
				)
			},
			recover = { it: F1 -> fail(it) }
		)
	}
}

@KtorDsl
@JvmName("routeOrRaise6")
inline fun <reified In : Any, reified Out : Any, reified F1 : Any, reified F2 : Any, reified F3 : Any, reified F4 : Any, reified F5 : Any, reified F6 : Any, reified Params : Parameters> Route.routeWithRaise(
	endpoint: Endpoint<In, Out, Or<Or<Or<Or<Or<Or<*, ByCode<F1>>, ByCode<F2>>, ByCode<F3>>, ByCode<F4>>, ByCode<F5>>, ByCode<F6>>, Params>,
	crossinline block: suspend context(Raise<F1>, Raise<F2>, Raise<F3>, Raise<F4>, Raise<F5>, Raise<F6>) TypedResponseScope<In, Out, Or<Or<Or<Or<Or<Or<*, ByCode<F1>>, ByCode<F2>>, ByCode<F3>>, ByCode<F4>>, ByCode<F5>>, ByCode<F6>>, Params>.() -> Unit,
) {
	route(endpoint) response@{
		recover(
			block = {
				recover(
					block = {
						recover(
							block = {
								recover(
									block = {
										recover(
											block = {
												recover(
													block = { block() },
													recover = { it: F6 -> fail(it) }
												)
											},
											recover = { it: F5 -> fail(it) }
										)
									},
									recover = { it: F4 -> fail(it) }
								)
							},
							recover = { it: F3 -> fail(it) }
						)
					},
					recover = { it: F2 -> fail(it) }
				)
			},
			recover = { it: F1 -> fail(it) }
		)
	}
}
