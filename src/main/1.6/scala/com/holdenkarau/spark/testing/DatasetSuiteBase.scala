package com.holdenkarau.spark.testing

import org.apache.spark.rdd.RDD

import scala.reflect.ClassTag

import org.apache.spark.sql.Dataset

class DatasetSuiteBase extends DataFrameSuiteBase with DatasetSuiteBaseLike {

}

class JavaDatasetSuiteBase extends JavaDataFrameSuiteBase with DatasetSuiteBaseLike with Serializable {
  def fakeClassTag[T]: ClassTag[T] = ClassTag.AnyRef.asInstanceOf[ClassTag[T]]


  /**
    * Check if two Datasets are equals, Datasets should have the same type.
    * This method could be customized by overriding equals method for the given class type.
    */
  def equalDatasets[U](expected: Dataset[U], result: Dataset[U]) = {
    super.equalDatasets(expected, result)(fakeClassTag[U])
  }

  /**
    * Compares if two Datasets are equal, Datasets should have the same type.
    * When comparing inexact fields uses tol.
    *
    * @param tol max acceptable tolerance, should be less than 1.
    */
  def approxEqualDatasets[U](expected: Dataset[U], result: Dataset[U], tol: Double) = {
    super.approxEqualDatasets(expected, result, tol)(fakeClassTag[U])
  }
}

trait DatasetSuiteBaseLike extends DataFrameSuiteBaseLike {

  /**
    * Check if two Datasets are equals, Datasets should have the same type.
    * This method could be customized by overriding equals method for the given class type.
    */
  def equalDatasets[U](expected: Dataset[U], result: Dataset[U])
                      (implicit UCT: ClassTag[U]) = {
    try {
      expected.rdd.cache
      result.rdd.cache
      assert("Length not Equal", expected.rdd.count, result.rdd.count)

      val expectedIndexValue: RDD[(Long, U)] = zipWithIndex(expected.rdd)
      val resultIndexValue: RDD[(Long, U)] = zipWithIndex(result.rdd)
      val unequalRDD = expectedIndexValue.join(resultIndexValue).filter
      { case (idx, (o1, o2)) => !o1.equals(o2) }

      assertEmpty(unequalRDD.take(maxUnequalRowsToShow))
    } finally {
      expected.rdd.unpersist()
      result.rdd.unpersist()
    }
  }

  /**
    * Compares if two Datasets are equal, Datasets should have the same type.
    * When comparing inexact fields uses tol.
    *
    * @param tol max acceptable tolerance, should be less than 1.
    */
  def approxEqualDatasets[U](expected: Dataset[U], result: Dataset[U], tol: Double)
                            (implicit UCT: ClassTag[U]) = {

    approxEqualDataFrames(expected.toDF, result.toDF, tol)
  }

}
