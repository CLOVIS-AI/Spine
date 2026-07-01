package opensavvy.spine.api

import kotlin.uuid.Uuid
import opensavvy.prepared.suite.SuiteDsl
import opensavvy.prepared.suite.assertions.checkThrows

fun SuiteDsl.parameters() = suite("Endpoint parameters") {
	test("Mandatory parameters") {
		class MandatoryParams(data: ParameterStorage) : Parameters(data) {
			var archived: Boolean by parameter("archived")
			var private: Boolean by parameter("private")
		}

		val params = buildParameters(::MandatoryParams) {
			archived = true
		}

		check(params.archived)
		checkThrows<NoSuchElementException> { params.private }

		check(params.data == mapOf("archived" to listOf("true")))
	}

	test("Optional parameters") {
		class OptionalParams(data: ParameterStorage) : Parameters(data) {
			var archived: Boolean? by parameter()
			var private: Boolean? by parameter()
		}

		val params = buildParameters(::OptionalParams) {
			archived = true
		}

		check(params.archived == true)
		check(params.private == null)

		check(params.data == mapOf("archived" to listOf("true")))
	}

	test("Optional parameters with explicit null values") {
		class OptionalParams(data: ParameterStorage) : Parameters(data) {
			var archived: Boolean? by parameter()
			var private: Boolean? by parameter()
		}

		val params = buildParameters(::OptionalParams) {
			archived = true
			private = null
		}

		check(params.archived == true)
		check(params.private == null)

		check(params.data == mapOf("archived" to listOf("true")))
	}

	test("Optional parameters with default values") {
		class DefaultParams(data: ParameterStorage) : Parameters(data) {
			var archived: Boolean by parameter(false)
			var private: Boolean by parameter(false)
		}

		val params = buildParameters(::DefaultParams) {
			archived = true
		}

		check(params.archived)
		check(!params.private)

		check(params.data == mapOf("archived" to listOf("true")))
	}

	test("List parameters") {
		class MandatoryParams(data: ParameterStorage) : Parameters(data) {
			var categories: List<String> by listParameter("search_categories")
			var tags by listParameter<String>()
		}

		val params = buildParameters(::MandatoryParams) {
			categories = listOf("category a")
		}

		check(params.categories == listOf("category a"))
		check(params.tags.isEmpty())

		check(params.data == mapOf("search_categories" to listOf("category a")))
	}

	test("List parameters with empty value") {
		class DefaultParams(data: ParameterStorage) : Parameters(data) {
			var categories: List<String> by listParameter("categories")
			var tags by listParameter<String>()
		}

		val params = buildParameters(::DefaultParams) {
			tags = emptyList()
		}

		check(params.categories == emptyList<String>())
		check(params.tags == emptyList<String>())

		check(params.data == emptyMap<String, String>())
	}

	test("Supported types are mapped correctly") {
		class Types(data: ParameterStorage) : Parameters(data) {
			var string: String by parameter("string")
			var bool: Boolean by parameter()

			var byte: Byte by parameter()
			var short: Short by parameter()
			var int: Int by parameter()
			var long: Long by parameter()

			var ubyte: UByte by parameter("u_byte")
			var ushort: UShort by parameter("u_short")
			var uint: UInt by parameter()
			var ulong: ULong by parameter()

			var float: Float by parameter()
			var double: Double by parameter()
			var uuid: Uuid by parameter()
		}

		val expectedUuid = Uuid.parse("123e4567-e89b-12d3-a456-426614174000")
		val params = buildParameters(::Types) {
			string = "thing"
			bool = true
			byte = 1
			short = 2
			int = 3
			long = 4

			ubyte = 5u
			ushort = 6u
			uint = 7u
			ulong = 8u

			float = 9f
			double = 10.0
			uuid = expectedUuid
		}

		check(params.string == "thing")
		check(params.bool)

		check(params.byte == 1.toByte())
		check(params.short == 2.toShort())
		check(params.int == 3)
		check(params.long == 4.toLong())

		check(params.ubyte == 5.toUByte())
		check(params.ushort == 6.toUShort())
		check(params.uint == 7.toUInt())
		check(params.ulong == 8.toULong())

		check(params.float == 9f)
		check(params.double == 10.0)
		check(params.uuid == expectedUuid)

		check(params.data == mapOf(
			"string" to listOf("thing"),
			"bool" to listOf("true"),

			"byte" to listOf("1"),
			"short" to listOf("2"),
			"int" to listOf("3"),
			"long" to listOf("4"),

			"u_byte" to listOf("5"),
			"u_short" to listOf("6"),
			"uint" to listOf("7"),
			"ulong" to listOf("8"),

			"float" to listOf("${9.0}"), // JVM: "9.0" — JS: "9"
			"double" to listOf("${10.0}"),
			"uuid" to listOf("123e4567-e89b-12d3-a456-426614174000"),
		))
	}
}
