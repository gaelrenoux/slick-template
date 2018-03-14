package psug20180322.model

case class House(
                  id: Option[Long] = None,
                  name: String,
                  primaryColor: Color,
                  secondaryColor: Color,
                  points: Long
                )

object House {

  case class Filter(
                     name: Option[String] = None,
                     anyColor: Option[Color] = None
                   )

  object Filter {
    val Empty = Filter()
  }

}
