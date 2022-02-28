package tensil

class Fixed8bp4(
    val bits: Short
) extends AnyVal {}

object Fixed8bp4
    extends FixedBase(
      width = 8,
      basePoint = 2,
      sizeBytes = 1,
      fromLongBits = (bits: Long) => new Fixed8bp4(bits.toShort),
      toLongBits = (fixed: Fixed8bp4) => fixed.bits.toLong
    ) {

  implicit val numeric = mkNumeric
}
