/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.holdenkarau.spark.testing

import org.apache.spark.sql.Row

class SampleDataFrameTest extends DataFrameSuiteBase {
  val byteArray = new Array[Byte](1)
  val diffByteArray = Array[Byte](192.toByte)
  val inputList = List(Magic("panda", 9001.0, byteArray),
    Magic("coffee", 9002.0, byteArray))
  val inputList2 = List(Magic("panda", 9001.0 + 1E-6, byteArray),
    Magic("coffee", 9002.0, byteArray))
  test("dataframe should be equal to its self") {
    val sqlCtx = sqlContext
    import sqlCtx.implicits._
    val input = sc.parallelize(inputList).toDF
    equalDataFrames(input, input)
  }

  test("unequal dataframes should not be equal") {
    val sqlCtx = sqlContext
    import sqlCtx.implicits._
    val input = sc.parallelize(inputList).toDF
    val input2 = sc.parallelize(inputList2).toDF
    intercept[org.scalatest.exceptions.TestFailedException] {
      equalDataFrames(input, input2)
    }
  }

  test("dataframe approx expected") {
    val sqlCtx = sqlContext
    import sqlCtx.implicits._
    val input = sc.parallelize(inputList).toDF
    val input2 = sc.parallelize(inputList2).toDF
    approxEqualDataFrames(input, input2, 1E-5)
    intercept[org.scalatest.exceptions.TestFailedException] {
      approxEqualDataFrames(input, input2, 1E-7)
    }
  }

  test("dataframe approxEquals on rows") {
    val sqlCtx = sqlContext
    import sqlCtx.implicits._
    val row = sc.parallelize(inputList).toDF.collect()(0)
    val row2 = sc.parallelize(inputList2).toDF.collect()(0)
    val row3 = Row()
    val row4 = Row(1)
    val row5 = Row(null)
    val row6 = Row("1")
    val row6a = Row("2")
    val row7 = Row(1.toFloat)
    assert(false === approxEquals(row, row2, 1E-7))
    assert(true === approxEquals(row, row2, 1E-5))
    assert(true === approxEquals(row3, row3, 1E-5))
    assert(false === approxEquals(row, row3, 1E-5))
    assert(false === approxEquals(row4, row5, 1E-5))
    assert(true === approxEquals(row5, row5, 1E-5))
    assert(false === approxEquals(row4, row6, 1E-5))
    assert(false === approxEquals(row6, row4, 1E-5))
    assert(false === approxEquals(row6, row7, 1E-5))
    assert(false === approxEquals(row6, row6a, 1E-5))
  }

  test("unequal dataframes should not be equal when length differs") {
    val sqlCtx = sqlContext
    import sqlCtx.implicits._
    val input = sc.parallelize(inputList).toDF
    val input2 = sc.parallelize(inputList.headOption.toSeq).toDF
    intercept[org.scalatest.exceptions.TestFailedException] {
      equalDataFrames(input, input2)
    }
    intercept[org.scalatest.exceptions.TestFailedException] {
      approxEqualDataFrames(input, input2, 1E-5)
    }
  }

  test("unequal dataframes should not be equal when byte array differs") {
    val sqlCtx = sqlContext
    import sqlCtx.implicits._
    val input = sc.parallelize(inputList).toDF
    val diffInputList = List(Magic("panda", 9001.0, byteArray),
      Magic("coffee", 9002.0, diffByteArray))
    val input2 = sc.parallelize(diffInputList).toDF
    intercept[org.scalatest.exceptions.TestFailedException] {
      equalDataFrames(input, input2)
    }
    intercept[org.scalatest.exceptions.TestFailedException] {
      approxEqualDataFrames(input, input2, 1E-5)
    }
  }
}

case class Magic(name: String, power: Double, byteArray: Array[Byte])
