# Module Server-side typesafe Spine schema usage (with Arrow typed errors)

Implement a Ktor API described with type-safety in common code, using Arrow typed errors.

<a href="https://central.sonatype.com/artifact/dev.opensavvy.spine/server-arrow"><img src="https://img.shields.io/maven-central/v/dev.opensavvy.spine/server-arrow.svg?label=Maven%20Central"></a>
<a href="https://opensavvy.dev/open-source/stability.html"><img src="https://badgen.net/static/Stability/alpha/purple"></a>
<a href="https://javadoc.io/doc/dev.opensavvy.spine/server-arrow"><img src="https://badgen.net/static/Other%20versions/javadoc.io/blue"></a>

This module introduces the [`routeWithRaise`][opensavvy.spine.server.arrow.routeWithRaise] helper to declare a Ktor endpoint handler which has the possibility to raise typed errors.
