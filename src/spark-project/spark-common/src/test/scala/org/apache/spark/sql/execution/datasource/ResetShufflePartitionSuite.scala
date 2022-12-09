/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark.sql.execution.datasource

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.common.{LocalMetadata, SparderBaseFunSuite}
import org.apache.spark.sql.internal.SQLConf

class ResetShufflePartitionSuite extends SparderBaseFunSuite with LocalMetadata {

  private val testResetShufflePartition = new ResetShufflePartition {}

  test("KE-39271: test shuffle partition reset without kylin.query.engine.spark-sql-shuffle-partitions") {
    overwriteSystemProp("kylin.storage.columnar.partition-split-size-mb", "1")
    val workerThread = 10
    val sparkSession = SparkSession.builder()
      .master(s"local[$workerThread]")
      .config("spark.sql.shuffle.partitions", workerThread.toString)
      .getOrCreate()

    assert(sparkSession.sessionState.conf.getConf(SQLConf.SHUFFLE_PARTITIONS) == workerThread)

    val sourceRows = 0
    var totalFileSize = 1024 * 1024 * 5
    testResetShufflePartition.setShufflePartitions(totalFileSize, sourceRows, sparkSession)
    assert(sparkSession.sessionState.conf.getConf(SQLConf.SHUFFLE_PARTITIONS) == 6)

    totalFileSize = 1024 * 1024 * 6
    testResetShufflePartition.setShufflePartitions(totalFileSize, sourceRows, sparkSession)
    assert(sparkSession.sessionState.conf.getConf(SQLConf.SHUFFLE_PARTITIONS) == 7)

    totalFileSize = 1024 * 1024 * 3
    testResetShufflePartition.setShufflePartitions(totalFileSize, sourceRows, sparkSession)
    assert(sparkSession.sessionState.conf.getConf(SQLConf.SHUFFLE_PARTITIONS) == 7)

    totalFileSize = 1024 * 1024 * 12
    testResetShufflePartition.setShufflePartitions(totalFileSize, sourceRows, sparkSession)
    assert(sparkSession.sessionState.conf.getConf(SQLConf.SHUFFLE_PARTITIONS) == workerThread)
  }

  test("KE-39271: test shuffle partition reset with kylin.query.engine.spark-sql-shuffle-partitions") {
    overwriteSystemProp("kylin.storage.columnar.partition-split-size-mb", "1")
    overwriteSystemProp("kylin.query.engine.spark-sql-shuffle-partitions", "100")

    val workerThread = 10
    val sparkSession = SparkSession.builder()
      .master(s"local[$workerThread]")
      .config("spark.sql.shuffle.partitions", workerThread.toString)
      .getOrCreate()

    assert(sparkSession.sessionState.conf.getConf(SQLConf.SHUFFLE_PARTITIONS) == workerThread)

    val sourceRows = 0
    var totalFileSize = 1024 * 1024 * 5
    testResetShufflePartition.setShufflePartitions(totalFileSize, sourceRows, sparkSession)
    assert(sparkSession.sessionState.conf.getConf(SQLConf.SHUFFLE_PARTITIONS) == 100)

    totalFileSize = 1024 * 1024 * 15
    testResetShufflePartition.setShufflePartitions(totalFileSize, sourceRows, sparkSession)
    assert(sparkSession.sessionState.conf.getConf(SQLConf.SHUFFLE_PARTITIONS) == 100)

    totalFileSize = 1024 * 1024 * 105
    testResetShufflePartition.setShufflePartitions(totalFileSize, sourceRows, sparkSession)
    assert(sparkSession.sessionState.conf.getConf(SQLConf.SHUFFLE_PARTITIONS) == 100)
  }
}
