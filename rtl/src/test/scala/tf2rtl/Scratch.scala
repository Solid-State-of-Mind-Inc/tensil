package tensil

import chisel3._
import chisel3.experimental.FixedPoint
import chisel3.experimental.BundleLiterals._
import chisel3.util.Decoupled
import chiseltest._
import tensil.util.decoupled.Counter
import chisel3.util.Queue

class Scratch extends Module { val io = IO(new Bundle {}) }

class ScratchSpec extends UnitSpec {
  behavior of "Scratch"

  it should "work" in {
    test(new Scratch) { m => }
  }
}
