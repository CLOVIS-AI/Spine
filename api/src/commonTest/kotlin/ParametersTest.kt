package opensavvy.spine.api

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import opensavvy.prepared.suite.SuiteDsl

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
		shouldThrow<NoSuchElementException> { params.private }

		params.data shouldBe mapOf(
			"archived" to "true",
		)
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

		params.data shouldBe mapOf(
			"archived" to "true",
		)
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

		params.data shouldBe mapOf(
			"archived" to "true",
		)
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
		}

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

		check(params.data == mapOf(
			"string" to "thing",
			"bool" to "true",

			"byte" to "1",
			"short" to "2",
			"int" to "3",
			"long" to "4",

			"u_byte" to "5",
			"u_short" to "6",
			"uint" to "7",
			"ulong" to "8",

			"float" to "${9.0}", // JVM: "9.0" — JS: "9"
			"double" to "${10.0}",
		))
	}
}
