package com.skillw.asaka.asaka.jvm.functions

interface AFunctionV

@FunctionalInterface
fun interface AFunctionV0 : AFunctionV {
    /** Invokes the function. */
    operator fun invoke()
}

/** A function that takes 1 argument. */
@FunctionalInterface
fun interface AFunctionV1<in P1> : AFunctionV {
    /** Invokes the function with the specified argument. */
    operator fun invoke(p1: P1)
}

/** A function that takes 2 arguments. */
@FunctionalInterface
fun interface AFunctionV2<in P1, in P2> : AFunctionV {
    /** Invokes the function with the specified arguments. */
    operator fun invoke(p1: P1, p2: P2)
}

/** A function that takes 3 arguments. */
@FunctionalInterface
fun interface AFunctionV3<in P1, in P2, in P3> : AFunctionV {
    /** Invokes the function with the specified arguments. */
    operator fun invoke(p1: P1, p2: P2, p3: P3)
}

/** A function that takes 4 arguments. */
@FunctionalInterface
fun interface AFunctionV4<in P1, in P2, in P3, in P4> : AFunctionV {
    /** Invokes the function with the specified arguments. */
    operator fun invoke(p1: P1, p2: P2, p3: P3, p4: P4)
}

/** A function that takes 5 arguments. */
@FunctionalInterface
fun interface AFunctionV5<in P1, in P2, in P3, in P4, in P5> :
    AFunctionV {
    /** Invokes the function with the specified arguments. */
    operator fun invoke(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5)
}

/** A function that takes 6 arguments. */
@FunctionalInterface
fun interface AFunctionV6<in P1, in P2, in P3, in P4, in P5, in P6> :
    AFunctionV {
    /** Invokes the function with the specified arguments. */
    operator fun invoke(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6)
}

/** A function that takes 7 arguments. */
@FunctionalInterface
fun interface AFunctionV7<in P1, in P2, in P3, in P4, in P5, in P6, in P7> :
    AFunctionV {
    /** Invokes the function with the specified arguments. */
    operator fun invoke(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7)
}

/** A function that takes 8 arguments. */
@FunctionalInterface
fun interface AFunctionV8<in P1, in P2, in P3, in P4, in P5, in P6, in P7, in P8> :
    AFunctionV {
    /** Invokes the function with the specified arguments. */
    operator fun invoke(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8)
}

/** A function that takes 9 arguments. */
@FunctionalInterface
fun interface AFunctionV9<in P1, in P2, in P3, in P4, in P5, in P6, in P7, in P8, in P9> :
    AFunctionV {
    /** Invokes the function with the specified arguments. */
    operator fun invoke(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9)
}

/** A function that takes 10 arguments. */
@FunctionalInterface
fun interface AFunctionV10<in P1, in P2, in P3, in P4, in P5, in P6, in P7, in P8, in P9, in P10> :
    AFunctionV {
    /** Invokes the function with the specified arguments. */
    operator fun invoke(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10)
}

/** A function that takes 11 arguments. */
@FunctionalInterface
fun interface AFunctionV11<in P1, in P2, in P3, in P4, in P5, in P6, in P7, in P8, in P9, in P10, in P11> :
    AFunctionV {
    /** Invokes the function with the specified arguments. */
    operator fun invoke(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11)
}

/** A function that takes 12 arguments. */
@FunctionalInterface
fun interface AFunctionV12<in P1, in P2, in P3, in P4, in P5, in P6, in P7, in P8, in P9, in P10, in P11, in P12> :
    AFunctionV {
    /** Invokes the function with the specified arguments. */
    operator fun invoke(
        p1: P1,
        p2: P2,
        p3: P3,
        p4: P4,
        p5: P5,
        p6: P6,
        p7: P7,
        p8: P8,
        p9: P9,
        p10: P10,
        p11: P11,
        p12: P12,
    )
}

/** A function that takes 13 arguments. */
@FunctionalInterface
fun interface AFunctionV13<in P1, in P2, in P3, in P4, in P5, in P6, in P7, in P8, in P9, in P10, in P11, in P12, in P13> :
    AFunctionV {
    /** Invokes the function with the specified arguments. */
    operator fun invoke(
        p1: P1,
        p2: P2,
        p3: P3,
        p4: P4,
        p5: P5,
        p6: P6,
        p7: P7,
        p8: P8,
        p9: P9,
        p10: P10,
        p11: P11,
        p12: P12,
        p13: P13,
    )
}

/** A function that takes 14 arguments. */
@FunctionalInterface
fun interface AFunctionV14<in P1, in P2, in P3, in P4, in P5, in P6, in P7, in P8, in P9, in P10, in P11, in P12, in P13, in P14> :
    AFunctionV {
    /** Invokes the function with the specified arguments. */
    operator fun invoke(
        p1: P1,
        p2: P2,
        p3: P3,
        p4: P4,
        p5: P5,
        p6: P6,
        p7: P7,
        p8: P8,
        p9: P9,
        p10: P10,
        p11: P11,
        p12: P12,
        p13: P13,
        p14: P14,
    )
}

/** A function that takes 15 arguments. */
@FunctionalInterface
fun interface AFunctionV15<in P1, in P2, in P3, in P4, in P5, in P6, in P7, in P8, in P9, in P10, in P11, in P12, in P13, in P14, in P15> :
    AFunctionV {
    /** Invokes the function with the specified arguments. */
    operator fun invoke(
        p1: P1,
        p2: P2,
        p3: P3,
        p4: P4,
        p5: P5,
        p6: P6,
        p7: P7,
        p8: P8,
        p9: P9,
        p10: P10,
        p11: P11,
        p12: P12,
        p13: P13,
        p14: P14,
        p15: P15,
    )
}

/** A function that takes 16 arguments. */
@FunctionalInterface
fun interface AFunctionV16<in P1, in P2, in P3, in P4, in P5, in P6, in P7, in P8, in P9, in P10, in P11, in P12, in P13, in P14, in P15, in P16> :
    AFunctionV {
    /** Invokes the function with the specified arguments. */
    operator fun invoke(
        p1: P1,
        p2: P2,
        p3: P3,
        p4: P4,
        p5: P5,
        p6: P6,
        p7: P7,
        p8: P8,
        p9: P9,
        p10: P10,
        p11: P11,
        p12: P12,
        p13: P13,
        p14: P14,
        p15: P15,
        p16: P16,
    )
}

/** A function that takes 17 arguments. */
@FunctionalInterface
fun interface AFunctionV17<in P1, in P2, in P3, in P4, in P5, in P6, in P7, in P8, in P9, in P10, in P11, in P12, in P13, in P14, in P15, in P16, in P17> :
    AFunctionV {
    /** Invokes the function with the specified arguments. */
    operator fun invoke(
        p1: P1,
        p2: P2,
        p3: P3,
        p4: P4,
        p5: P5,
        p6: P6,
        p7: P7,
        p8: P8,
        p9: P9,
        p10: P10,
        p11: P11,
        p12: P12,
        p13: P13,
        p14: P14,
        p15: P15,
        p16: P16,
        p17: P17,
    )
}

/** A function that takes 18 arguments. */
@FunctionalInterface
fun interface AFunctionV18<in P1, in P2, in P3, in P4, in P5, in P6, in P7, in P8, in P9, in P10, in P11, in P12, in P13, in P14, in P15, in P16, in P17, in P18> :
    AFunctionV {
    /** Invokes the function with the specified arguments. */
    operator fun invoke(
        p1: P1,
        p2: P2,
        p3: P3,
        p4: P4,
        p5: P5,
        p6: P6,
        p7: P7,
        p8: P8,
        p9: P9,
        p10: P10,
        p11: P11,
        p12: P12,
        p13: P13,
        p14: P14,
        p15: P15,
        p16: P16,
        p17: P17,
        p18: P18,
    )
}

/** A function that takes 19 arguments. */
@FunctionalInterface
fun interface AFunctionV19<in P1, in P2, in P3, in P4, in P5, in P6, in P7, in P8, in P9, in P10, in P11, in P12, in P13, in P14, in P15, in P16, in P17, in P18, in P19> :
    AFunctionV {
    /** Invokes the function with the specified arguments. */
    operator fun invoke(
        p1: P1,
        p2: P2,
        p3: P3,
        p4: P4,
        p5: P5,
        p6: P6,
        p7: P7,
        p8: P8,
        p9: P9,
        p10: P10,
        p11: P11,
        p12: P12,
        p13: P13,
        p14: P14,
        p15: P15,
        p16: P16,
        p17: P17,
        p18: P18,
        p19: P19,
    )
}

/** A function that takes 20 arguments. */
@FunctionalInterface
fun interface AFunctionV20<in P1, in P2, in P3, in P4, in P5, in P6, in P7, in P8, in P9, in P10, in P11, in P12, in P13, in P14, in P15, in P16, in P17, in P18, in P19, in P20> :
    AFunctionV {
    /** Invokes the function with the specified arguments. */
    operator fun invoke(
        p1: P1,
        p2: P2,
        p3: P3,
        p4: P4,
        p5: P5,
        p6: P6,
        p7: P7,
        p8: P8,
        p9: P9,
        p10: P10,
        p11: P11,
        p12: P12,
        p13: P13,
        p14: P14,
        p15: P15,
        p16: P16,
        p17: P17,
        p18: P18,
        p19: P19,
        p20: P20,
    )
}

/** A function that takes 21 arguments. */
@FunctionalInterface
fun interface AFunctionV21<in P1, in P2, in P3, in P4, in P5, in P6, in P7, in P8, in P9, in P10, in P11, in P12, in P13, in P14, in P15, in P16, in P17, in P18, in P19, in P20, in P21> :
    AFunctionV {
    /** Invokes the function with the specified arguments. */
    operator fun invoke(
        p1: P1,
        p2: P2,
        p3: P3,
        p4: P4,
        p5: P5,
        p6: P6,
        p7: P7,
        p8: P8,
        p9: P9,
        p10: P10,
        p11: P11,
        p12: P12,
        p13: P13,
        p14: P14,
        p15: P15,
        p16: P16,
        p17: P17,
        p18: P18,
        p19: P19,
        p20: P20,
        p21: P21,
    )
}

/** A function that takes 22 arguments. */
@FunctionalInterface
fun interface AFunctionV22<in P1, in P2, in P3, in P4, in P5, in P6, in P7, in P8, in P9, in P10, in P11, in P12, in P13, in P14, in P15, in P16, in P17, in P18, in P19, in P20, in P21, in P22> :
    AFunctionV {
    /** Invokes the function with the specified arguments. */
    operator fun invoke(
        p1: P1,
        p2: P2,
        p3: P3,
        p4: P4,
        p5: P5,
        p6: P6,
        p7: P7,
        p8: P8,
        p9: P9,
        p10: P10,
        p11: P11,
        p12: P12,
        p13: P13,
        p14: P14,
        p15: P15,
        p16: P16,
        p17: P17,
        p18: P18,
        p19: P19,
        p20: P20,
        p21: P21,
        p22: P22,
    )
}
