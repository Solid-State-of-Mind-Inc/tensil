/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright © 2019-2022 Tensil AI Company */

package tensil.tools

import org.scalatest._
import java.io._

import org.tensorflow.framework.types.DataType
import tensil.tools.compiler.{
  Backend,
  MatMulFlags,
  LoadWeightsFlags,
  DataMoveFlags,
  SIMDFlags,
  SIMDOp,
  SIMDSource,
  SIMDDestination,
  MemoryAddress,
  MemoryTag,
  MemoryRef
}
import tensil.{Architecture, ArchitectureDataType, InstructionLayout}

class CompilerBackendSpec extends FlatSpec {
  behavior of "CompilerBackend"

  val Depth16: Long = 1L << 14
  val Depth32: Long = 1L << 30
  val Depth64: Long = 1L << 62

  val SIMDRegistersDepth: Int = 1
  val Stride0Depth: Int       = 1 << 2
  val Stride1Depth: Int       = 1 << 2

  val SegmentKey = (0, 0, 0, 0)

  def mk16BitBackend() = {
    new Backend(
      new InstructionLayout(
        Architecture.mkWithDefaults(
          arraySize = 4,
          localDepth = Depth16,
          accumulatorDepth = Depth16,
          dram0Depth = Depth16,
          dram1Depth = Depth16,
          simdRegistersDepth = SIMDRegistersDepth,
          stride0Depth = Stride0Depth,
          stride1Depth = Stride1Depth
        )
      )
    )
  }

  def mk32BitBackend() = {
    new Backend(
      new InstructionLayout(
        Architecture.mkWithDefaults(
          4,
          localDepth = Depth32,
          accumulatorDepth = Depth32,
          dram0Depth = Depth32,
          dram1Depth = Depth32,
          simdRegistersDepth = SIMDRegistersDepth,
          stride0Depth = Stride0Depth,
          stride1Depth = Stride1Depth
        )
      )
    )
  }

  def mk64BitBackend() = {
    new Backend(
      new InstructionLayout(
        Architecture.mkWithDefaults(
          4,
          localDepth = Depth64,
          accumulatorDepth = Depth64,
          dram0Depth = Depth64,
          dram1Depth = Depth64,
          simdRegistersDepth = SIMDRegistersDepth,
          stride0Depth = Stride0Depth,
          stride1Depth = Stride1Depth
        )
      )
    )
  }

  it should "emit 16-bit NoOp" in {
    val out     = new ByteArrayOutputStream()
    val backend = mk16BitBackend()

    val segment = backend.mkSegment(SegmentKey)
    segment.emitNoOp()
    segment.emitNoOp()
    segment.emitNoOp()
    backend.finalizeSegment(segment)
    backend.writeSegments(out)

    assert(
      out.toByteArray() === Array(
        // Padding
        0, 0, 0, 0, 0, 0,
        // Header
        0,
        // Padding
        0, 0, 0, 0, 0, 0,
        // Header
        0,
        // Padding
        0, 0, 0, 0, 0, 0,
        // Header
        0
      ).map(_.toByte)
    )
  }

  it should "emit 32-bit NoOp" in {
    val out     = new ByteArrayOutputStream()
    val backend = mk32BitBackend()

    val segment = backend.mkSegment(SegmentKey)
    segment.emitNoOp()
    segment.emitNoOp()
    segment.emitNoOp()
    backend.finalizeSegment(segment)
    backend.writeSegments(out)

    assert(
      out.toByteArray() === Array(
        // Padding
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        // Header
        0,
        // Padding
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        // Header
        0,
        // Padding
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        // Header
        0,
      ).map(_.toByte)
    )
  }

  it should "emit 64-bit NoOp" in {
    val out     = new ByteArrayOutputStream()
    val backend = mk64BitBackend()

    val segment = backend.mkSegment(SegmentKey)
    segment.emitNoOp()
    segment.emitNoOp()
    segment.emitNoOp()
    backend.finalizeSegment(segment)
    backend.writeSegments(out)

    assert(
      out.toByteArray() === Array(
        // Padding
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        // Header
        0,
        // Padding
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        // Header
        0,
        // Padding
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        // Header
        0
      ).map(_.toByte)
    )
  }

  it should "emit 32-bit MatMul" in {
    val out     = new ByteArrayOutputStream()
    val backend = mk32BitBackend()

    val segment = backend.mkSegment(SegmentKey)
    segment.emitMatMul(
      accumulate = false,
      0,
      MemoryAddress(MemoryTag.Local, MemoryRef.Invalid, 0x00012345L),
      1,
      MemoryAddress(MemoryTag.Accumulators, MemoryRef.Invalid, 0x0089abcdL),
      0x543210L
    )
    segment.emitMatMul(
      accumulate = true,
      2,
      MemoryAddress(MemoryTag.Local, MemoryRef.Invalid, 0x00012345L),
      3,
      MemoryAddress(MemoryTag.Accumulators, MemoryRef.Invalid, 0x0089abcdL),
      0x543210L
    )
    segment.emitMatMul(
      accumulate = false,
      0,
      MemoryAddress(MemoryTag.Zeroes, MemoryRef.Invalid, 0x00012345L),
      0,
      MemoryAddress(MemoryTag.Accumulators, MemoryRef.Invalid, 0x0089abcdL),
      0x543210L
    )
    segment.emitMatMul(
      accumulate = true,
      0,
      MemoryAddress(MemoryTag.Zeroes, MemoryRef.Invalid, 0x00012345L),
      0,
      MemoryAddress(MemoryTag.Accumulators, MemoryRef.Invalid, 0x0089abcdL),
      0x543210L
    )
    backend.finalizeSegment(segment)
    backend.writeSegments(out)

    assert(
      out.toByteArray() === Array(
        // Local Address
        0x45, 0x23, 0x01, 0x00,
        // Accumulator Address
        0xcd, 0xab, 0x89, 0x40,
        // Size
        0x10, 0x32, 0x54, 0x00,
        // Header
        0x20,
        // Local Address
        0x45, 0x23, 0x01, 0x80,
        // Accumulator Address
        0xcd, 0xab, 0x89, 0xc0,
        // Size
        0x10, 0x32, 0x54, 0x00,
        // Header
        0x21,
        // Local Address
        0x45, 0x23, 0x01, 0x00,
        // Accumulator Address
        0xcd, 0xab, 0x89, 0x00,
        // Size
        0x10, 0x32, 0x54, 0x00,
        // Header
        0x22,
        // Local Address
        0x45, 0x23, 0x01, 0x00,
        // Accumulator Address
        0xcd, 0xab, 0x89, 0x00,
        // Size
        0x10, 0x32, 0x54, 0x00,
        // Header
        0x23,
      ).map(_.toByte)
    )
  }

  it should "emit 32-bit SIMD" in {
    val out     = new ByteArrayOutputStream()
    val backend = mk32BitBackend()

    val segment = backend.mkSegment(SegmentKey)
    segment.emitSIMD(
      accumulate = false,
      SIMDOp.Zero,
      0,
      0,
      SIMDDestination.Register1,
      MemoryAddress(MemoryTag.Invalid, MemoryRef.Invalid, 0x00012345L),
      MemoryAddress(MemoryTag.Invalid, MemoryRef.Invalid, 0x0089abcdL)
    )
    segment.emitSIMD(
      accumulate = false,
      SIMDOp.Max,
      SIMDSource.Input,
      SIMDSource.Register1,
      SIMDDestination.Output,
      MemoryAddress(MemoryTag.Accumulators, MemoryRef.Invalid, 0x00012345L),
      MemoryAddress(MemoryTag.Accumulators, MemoryRef.Invalid, 0x0089abcdL),
    )
    backend.finalizeSegment(segment)
    backend.writeSegments(out)

    assert(
      out.toByteArray() === Array(
        // Write Accumulator Address
        0x45, 0x23, 0x01, 0x00,
        // Read Accumulator Address
        0xcd, 0xab, 0x89, 0x00,
        // SIMD Instruction
        0x09,
        // Padding
        0, 0, 0,
        // Header
        0x80,
        // Write Accumulator Address
        0x45, 0x23, 0x01, 0x00,
        // Read Accumulator Address
        0xcd, 0xab, 0x89, 0x00,
        // SIMD Instruction
        0x7a,
        // Padding
        0, 0, 0,
        // Header
        0x83,
      ).map(_.toByte)
    )
  }

  it should "emit 32-bit DataMove" in {
    val out     = new ByteArrayOutputStream()
    val backend = mk32BitBackend()

    val segment = backend.mkSegment(SegmentKey)

    segment.emitDataMove(
      toLocal = true,
      accumulate = false,
      0,
      MemoryAddress(MemoryTag.Local, MemoryRef.Invalid, 0x00012345L),
      1,
      MemoryAddress(MemoryTag.Vars, MemoryRef.Invalid, 0x0089abcdL),
      0x00543210L
    )
    segment.emitDataMove(
      toLocal = false,
      accumulate = false,
      2,
      MemoryAddress(MemoryTag.Local, MemoryRef.Invalid, 0x00012345L),
      3,
      MemoryAddress(MemoryTag.Vars, MemoryRef.Invalid, 0x0089abcdL),
      0x00543210L
    )
    segment.emitDataMove(
      toLocal = true,
      accumulate = false,
      3,
      MemoryAddress(MemoryTag.Local, MemoryRef.Invalid, 0x00012345L),
      2,
      MemoryAddress(MemoryTag.Consts, MemoryRef.Invalid, 0x0089abcdL),
      0x00543210L
    )
    segment.emitDataMove(
      toLocal = false,
      accumulate = false,
      1,
      MemoryAddress(MemoryTag.Local, MemoryRef.Invalid, 0x00012345L),
      0,
      MemoryAddress(MemoryTag.Consts, MemoryRef.Invalid, 0x0089abcdL),
      0x00543210L
    )
    segment.emitDataMove(
      toLocal = true,
      accumulate = true,
      1,
      MemoryAddress(MemoryTag.Local, MemoryRef.Invalid, 0x00012345L),
      1,
      MemoryAddress(MemoryTag.Accumulators, MemoryRef.Invalid, 0x0089abcdL),
      0x00543210L
    )
    segment.emitDataMove(
      toLocal = false,
      accumulate = false,
      2,
      MemoryAddress(MemoryTag.Local, MemoryRef.Invalid, 0x00012345L),
      2,
      MemoryAddress(MemoryTag.Accumulators, MemoryRef.Invalid, 0x0089abcdL),
      0x00543210L
    )
    segment.emitDataMove(
      toLocal = false,
      accumulate = true,
      3,
      MemoryAddress(MemoryTag.Local, MemoryRef.Invalid, 0x00012345L),
      3,
      MemoryAddress(MemoryTag.Accumulators, MemoryRef.Invalid, 0x0089abcdL),
      0x00543210L
    )
    backend.finalizeSegment(segment)
    backend.writeSegments(out)

    assert(
      out.toByteArray() === Array(
        // Local Address
        0x45, 0x23, 0x01, 0x00,
        // Vars Address
        0xcd, 0xab, 0x89, 0x40,
        // Size
        0x10, 0x32, 0x54, 0x00,
        // Header
        0x40,
        // Local Address
        0x45, 0x23, 0x01, 0x80,
        // Vars Address
        0xcd, 0xab, 0x89, 0xc0,
        // Size
        0x10, 0x32, 0x54, 0x00,
        // Header
        0x41,
        // Local Address
        0x45, 0x23, 0x01, 0xc0,
        // Consts Address
        0xcd, 0xab, 0x89, 0x80,
        // Size
        0x10, 0x32, 0x54, 0x00,
        // Header
        0x42,
        // Local Address
        0x45, 0x23, 0x01, 0x40,
        // Consts Address
        0xcd, 0xab, 0x89, 0x00,
        // Size
        0x10, 0x32, 0x54, 0x00,
        // Header
        0x43,
        // Local Address
        0x45, 0x23, 0x01, 0x40,
        // Accumulator Address
        0xcd, 0xab, 0x89, 0x40,
        // Size
        0x10, 0x32, 0x54, 0x00,
        // Header
        0x4c,
        // Local Address
        0x45, 0x23, 0x01, 0x80,
        // Accumulator Address
        0xcd, 0xab, 0x89, 0x80,
        // Size
        0x10, 0x32, 0x54, 0x00,
        // Header
        0x4d,
        // Local Address
        0x45, 0x23, 0x01, 0xc0,
        // Accumulator Address
        0xcd, 0xab, 0x89, 0xc0,
        // Size
        0x10, 0x32, 0x54, 0x00,
        // Header
        0x4f,
      ).map(_.toByte)
    )
  }

  it should "emit 32-bit LoadWeights" in {
    val out     = new ByteArrayOutputStream()
    val backend = mk32BitBackend()

    val segment = backend.mkSegment(SegmentKey)
    segment.emitLoadWeights(
      0,
      MemoryAddress(MemoryTag.Local, MemoryRef.Invalid, 0x00012345L),
      0x0089abcdL
    )
    segment.emitLoadWeights(
      1,
      MemoryAddress(MemoryTag.Local, MemoryRef.Invalid, 0x00012345L),
      0x0089abcdL
    )
    segment.emitLoadWeights(
      2,
      MemoryAddress(MemoryTag.Local, MemoryRef.Invalid, 0x00012345L),
      0x0089abcdL
    )
    segment.emitLoadWeights(
      3,
      MemoryAddress(MemoryTag.Local, MemoryRef.Invalid, 0x00012345L),
      0x0089abcdL
    )
    segment.emitLoadWeights(
      0,
      MemoryAddress.Zeroes,
      0x0089abcdL
    )
    backend.finalizeSegment(segment)
    backend.writeSegments(out)

    assert(
      out.toByteArray() === Array(
        // Weights Address
        0x45, 0x23, 0x01, 0x00,
        // Weights Size
        0xcd, 0xab, 0x89, 0x00,
        // Padding
        0, 0, 0, 0,
        // Header
        0x60,
        // Weights Address
        0x45, 0x23, 0x01, 0x40,
        // Weights Size
        0xcd, 0xab, 0x89, 0x00,
        // Padding
        0, 0, 0, 0,
        // Header
        0x60,
        // Weights Address
        0x45, 0x23, 0x01, 0x80,
        // Weights Size
        0xcd, 0xab, 0x89, 0x00,
        // Padding
        0, 0, 0, 0,
        // Header
        0x60,
        // Weights Address
        0x45, 0x23, 0x01, 0xc0,
        // Weights Size
        0xcd, 0xab, 0x89, 0x00,
        // Padding
        0, 0, 0, 0,
        // Header
        0x60,
        // Weights Address
        0, 0, 0, 0,
        // Weights Size
        0xcd, 0xab, 0x89, 0x00,
        // Padding
        0, 0, 0, 0,
        // Header
        0x61
      ).map(_.toByte)
    )
  }
}
