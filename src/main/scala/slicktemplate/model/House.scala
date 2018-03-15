package slicktemplate.model

final case class House(
                        id: Long = 0L,
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