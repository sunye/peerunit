/*
This file is part of PeerUnit.

PeerUnit is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

PeerUnit is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with PeerUnit.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.inria.peerunit.scala.base

/**
 * User: Junior
 * Date: 3/19/11
 * Time: 6:44 PM
 * To change this template use File | Settings | File Templates.
 */

abstract class Range {
  def includes(index: Int): Boolean
}

object Range {

  private val separator = "-"

  def fromString(str: String): Range = {
    if (str equals "*") new AllValues

    else {
      val values: Array[String] = str split separator

      if (values.length == 1) new SingleValue(values(0) toInt)
      else new Interval(values(0) toInt, values(1) toInt)
    }
  }

  private class SingleValue(private val value: Int) extends Range {
    require(value >= 0)
    override def includes(index: Int): Boolean = index == value
  }

  private class Interval(private val from: Int, private val to: Int) extends Range {
    require(from < to)
    override def includes(index: Int): Boolean = (index >= from && index <= to)
  }

  class AllValues extends Range {
    override def includes(index: Int): Boolean = index >= 0
  }
}