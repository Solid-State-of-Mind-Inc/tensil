package tf2rtl.tools

import java.io._
import scala.reflect.ClassTag
import tf2rtl.tools.golden.{Processor, ExecutiveTraceContext}
import scala.collection.mutable
import tf2rtl.ArchitectureDataType

object Xor {
  def prepareInputStream(
      dataType: ArchitectureDataType,
      arraySize: Int,
      count: Int = 1
  ): InputStream =
    new ByteArrayInputStream(prepareInputBytes(dataType, arraySize, count))

  def prepareInputBytes(
      dataType: ArchitectureDataType,
      arraySize: Int,
      count: Int = 1
  ): Array[Byte] = {
    val inputPrep           = new ByteArrayOutputStream()
    val inputPrepDataStream = new DataOutputStream(inputPrep)

    for (i <- 0 until count) {
      val (x0, x1, _) = GoldenXor(i % GoldenXor.size)
      Util.writeArgs(dataType, inputPrepDataStream, arraySize, x0, x1)
    }

    inputPrep.toByteArray()
  }

  def assertOutput(
      dataType: ArchitectureDataType,
      arraySize: Int,
      bytes: Array[Byte],
      count: Int = 1
  ): Unit = {
    val rmse = new RMSE()

    val output =
      new DataInputStream(new ByteArrayInputStream(bytes))

    for (i <- 0 until count) {
      val (_, _, y) = GoldenXor(i % GoldenXor.size)
      rmse.addSample(Util.readResult(dataType, output, arraySize, 1)(0), y)
    }

    assert(rmse.compute < dataType.error)
  }

  private val GoldenXor = Seq(
    (0f, 0f, 0f),
    (1f, 0f, 1f),
    (0f, 1f, 1f),
    (1f, 1f, 0f),
  )
}
