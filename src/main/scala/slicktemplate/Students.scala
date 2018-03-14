package slicktemplate

import slicktemplate.model.{Lineage, Student}

object Students {

  def Harry(houseId: Long) = Student(name = "Harry Potter", lineage = Lineage.HalfBlood, houseId = houseId)

  def Ron(houseId: Long) = Student(name = "Ron Weasley", lineage = Lineage.PureBlood, houseId = houseId)

  def Hermione(houseId: Long) = Student(name = "Hermione Granger", lineage = Lineage.MuggleBorn, houseId = houseId)

  def Cedric(houseId: Long) = Student(name = "Cedric Diggory", lineage = Lineage.PureBlood, houseId = houseId)

  def Luna(houseId: Long) = Student(name = "Luna Lovegood", lineage = Lineage.HalfBlood, houseId = houseId)

  def Draco(houseId: Long) = Student(name = "Draco Malfoy", lineage = Lineage.PureBlood, houseId = houseId)

  def AllGryffindor(houseId: Long): List[Student] = Harry(houseId) :: Ron(houseId) :: Hermione(houseId) :: Nil

  def AllHufflepuff(houseId: Long): List[Student] = Cedric(houseId) :: Nil

  def AllRavenclaw(houseId: Long): List[Student] = Luna(houseId) :: Nil

  def AllSlytherin(houseId: Long): List[Student] = Draco(houseId) :: Nil
}
