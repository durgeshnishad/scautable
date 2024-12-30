package io.github.quafadas.scautable

import scalatags.Text.all.*
import java.time.LocalDate

import scala.annotation.experimental
import NamedTuple.*
import CSV.*

@experimental
class CSVSuite extends munit.FunSuite:

  import scautable.*
  import scautable.given

  test("extendable") {
    given dateT: HtmlTableRender[LocalDate] = new HtmlTableRender[LocalDate]:
      override def tableCell(a: LocalDate) = td(
        s"$a"
      )
    case class Customize(t: LocalDate, i: Int)
    val custom = Seq(Customize(LocalDate.of(2025, 1, 1), 1))
    assertEquals(
      """<table id="scautable" class="display"><thead><tr><th>t</th><th>i</th></tr></thead><tbody><tr><td>2025-01-01</td><td>1</td></tr></tbody></table>""",
      scautable(custom).toString()
    )
  }

  test("csv from resource compiles and typechecks") {
    val csv: CsvIterator[("col1", "col2", "col3")] = CSV.absolutePath(Generated.resourceDir0 + "\\simple.csv")
    
    val titanic = CSV.absolutePath(Generated.resourceDir0 + "\\titanic.csv")
  }

  test("reading data") {
    val csv: CsvIterator[("col1", "col2", "col3")] = CSV.absolutePath(Generated.resourceDir0 + "\\simple.csv")

    assertEquals(csv.drop(1).toArray.mkString(","), """(1,2,7),(3,4,8),(5,6,9)"""  )
  }

  test("map rows") {
    val csv: CsvIterator[("col1", "col2", "col3")] = CSV.absolutePath(Generated.resourceDir0 + "\\simple.csv")

    val column2 = csv.mapRows(x => x.col2.toInt).toArray
    
    assertEquals(column2.head, 2)
    assertEquals(column2.tail.head, 4)
    assertEquals(column2.last, 6)

    val columns2n3 = csv.mapRows(x => (x.col2.toInt, x.col3.toInt)).toArray

    assertEquals(columns2n3.head, (2, 7))
    assertEquals(columns2n3.tail.head, (4, 8))
    assertEquals(columns2n3.last, (6, 9))
  }


  test("parse types preserve rows") {
    val csv: CsvIterator[("col1", "col2", "col3")] = CSV.absolutePath(Generated.resourceDir0 + "\\simple.csv")

    val values = csv
      .drop(1)
      .map { x =>
        x.toTuple
          .copy(
            _1 = x.col1.toInt,             
            _3 = x.col3.toDouble
          )
          .withNames[csv.COLUMNS]
      }
      .toArray

    assertEquals(values.head, (1, "2", 7.0))
    assertEquals(values.tail.head, (3, "4", 8.0))
    assertEquals(values.last, (5, "6", 9.0))
  }

  test("console print") {
    val csv: CsvIterator[("col1", "col2", "col3")] = CSV.absolutePath(Generated.resourceDir0 + "\\simple.csv")    
    assertNoDiff(
      csv.drop(1).toArray.consolePrint(csv.headers),
      """| |col1|col2|col3|
+-+----+----+----+
|0|   1|   2|   7|
|1|   3|   4|   8|
|2|   5|   6|   9|
+-+----+----+----+""".trim()
    )

  }

  test("header indexes") {
    val csv: CsvIterator[("col1", "col2", "col3")] = CSV.absolutePath(Generated.resourceDir0 + "\\simple.csv")
    assertEquals(csv.headerIndex("col1"), 0)
    assertEquals(csv.headerIndex("col2"), 1)
    assertEquals(csv.headerIndex("col3"), 2)
  }

  test("Copy iterator") {
    val csv: CsvIterator[("col1", "col2", "col3")] = CSV.absolutePath(Generated.resourceDir0 + "\\simple.csv")
    val newItr: CsvIterator[("col1", "col2", "col3")] = csv.copy()
    assertNoDiff(
      newItr.drop(1).toArray.consolePrint(csv.headers),
      """| |col1|col2|col3|
+-+----+----+----+
|0|   1|   2|   7|
|1|   3|   4|   8|
|2|   5|   6|   9|
+-+----+----+----+""")
  }

  // test("url") {
  //   val csv = CSV.url("https://raw.githubusercontent.com/datasciencedojo/datasets/refs/heads/master/titanic.csv")    
  // }
