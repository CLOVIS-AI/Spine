package opensavvy.spine.api

// This file is an API example that's meant to have a bit of everything
// It is used as a data source in other tests

// It represents a simple API in which users are assigned to one or more departments

object Root : RootResource("api") {
	object Users : StaticResource<Root>("users", Root) {

		val create by post()
		val list by get()

		object User : DynamicResource<Users>("user", Users) {

			val get by get()
			val head by head()
			val update by patch()
			val preferences by get("preferences")

			object Departments : StaticResource<User>("departments", User) {

				val add by put()
				val assigned by get()

				object Department : DynamicResource<Departments>("department", Departments) {

					val get by get()
					val remove by delete()
				}
			}
		}
	}

	object Departments : StaticResource<Root>("departments", Root) {

		val create by post()
		val list by get()

		object Department : DynamicResource<Departments>("department", Departments) {

			val get by get()
			val delete by delete()
		}
	}
}
