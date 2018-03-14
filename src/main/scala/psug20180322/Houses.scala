package psug20180322

import psug20180322.model.{Color, House}

object Houses {

  val Gryffindor = House(
    name = "Gryffindor",
    primaryColor = Color.Scarlet,
    secondaryColor = Color.Gold,
    points = 0
  )

  val Hufflepuff = House(
    name = "Hufflepuff",
    primaryColor = Color.Yellow,
    secondaryColor = Color.Black,
    points = 0
  )

  val Ravenclaw = House(
    name = "Ravenclaw",
    primaryColor = Color.Blue,
    secondaryColor = Color.Bronze,
    points = 0
  )

  val Slytherin = House(
    name = "Slytherin",
    primaryColor = Color.Green,
    secondaryColor = Color.Silver,
    points = 0
  )

  val All = Gryffindor :: Hufflepuff :: Ravenclaw :: Slytherin :: Nil

}
