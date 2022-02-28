package tensil

import chisel3.{Data, Vec}
import chisel3.util.ReadyValidIO

import scala.reflect.ClassTag
import scala.language.implicitConversions

package object decoupled {
  implicit def decoupledToDriver[T <: Data : ClassTag](
      x: ReadyValidIO[T]
  ): Driver[T] =
    new Driver(x)

  implicit def decoupledVecToDriver[T <: Data : ClassTag](
      x: ReadyValidIO[Vec[T]]
  ): VecDriver[T] = new VecDriver(x)
}
