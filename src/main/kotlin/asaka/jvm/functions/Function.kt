package com.skillw.asaka.asaka.jvm.functions

interface AFunction<out R>

@FunctionalInterface
fun interface AFunction0<out R> : AFunction<R> {
    /** Invokes the function. */
    operator fun invoke(): R
}

/** A function that takes 1 argument. */
@FunctionalInterface
fun interface AFunction1<in P1, out R> : AFunction<R> {
    /** Invokes the function with the specified argument. */
    operator fun invoke(p1: P1): R
}

/** A function that takes 2 arguments. */
@FunctionalInterface
fun interface AFunction2<in P1, in P2, out R> : AFunction<R> {
    /** Invokes the function with the specified arguments. */
    operator fun invoke(p1: P1, p2: P2): R
}

/** A function that takes 3 arguments. */
@FunctionalInterface
fun interface AFunction3<in P1, in P2, in P3, out R> : AFunction<R> {
    /** Invokes the function with the specified arguments. */
    operator fun invoke(p1: P1, p2: P2, p3: P3): R
}

/** A function that takes 4 arguments. */
@FunctionalInterface
fun interface AFunction4<in P1, in P2, in P3, in P4, out R> : AFunction<R> {
    /** Invokes the function with the specified arguments. */
    operator fun invoke(p1: P1, p2: P2, p3: P3, p4: P4): R
}

/** A function that takes 5 arguments. */
@FunctionalInterface
fun interface AFunction5<in P1, in P2, in P3, in P4, in P5, out R> :
    AFunction<R> {
    /** Invokes the function with the specified arguments. */
    operator fun invoke(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5): R
}

/** A function that takes 6 arguments. */
@FunctionalInterface
fun interface AFunction6<in P1, in P2, in P3, in P4, in P5, in P6, out R> :
    AFunction<R> {
    /** Invokes the function with the specified arguments. */
    operator fun invoke(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6): R
}

/** A function that takes 7 arguments. */
@FunctionalInterface
fun interface AFunction7<in P1, in P2, in P3, in P4, in P5, in P6, in P7, out R> :
    AFunction<R> {
    /** Invokes the function with the specified arguments. */
    operator fun invoke(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7): R
}

/** A function that takes 8 arguments. */
@FunctionalInterface
fun interface AFunction8<in P1, in P2, in P3, in P4, in P5, in P6, in P7, in P8, out R> :
    AFunction<R> {
    /** Invokes the function with the specified arguments. */
    operator fun invoke(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8): R
}

/** A function that takes 9 arguments. */
@FunctionalInterface
fun interface AFunction9<in P1, in P2, in P3, in P4, in P5, in P6, in P7, in P8, in P9, out R> :
    AFunction<R> {
    /** Invokes the function with the specified arguments. */
    operator fun invoke(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9): R
}

/** A function that takes 10 arguments. */
@FunctionalInterface
fun interface AFunction10<in P1, in P2, in P3, in P4, in P5, in P6, in P7, in P8, in P9, in P10, out R> :
    AFunction<R> {
    /** Invokes the function with the specified arguments. */
    operator fun invoke(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10): R
}

/** A function that takes 11 arguments. */
@FunctionalInterface
fun interface AFunction11<in P1, in P2, in P3, in P4, in P5, in P6, in P7, in P8, in P9, in P10, in P11, out R> :
    AFunction<R> {
    /** Invokes the function with the specified arguments. */
    operator fun invoke(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11): R
}

/** A function that takes 12 arguments. */
@FunctionalInterface
fun interface AFunction12<in P1, in P2, in P3, in P4, in P5, in P6, in P7, in P8, in P9, in P10, in P11, in P12, out R> :
    AFunction<R> {
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
    ): R
}

/** A function that takes 13 arguments. */
@FunctionalInterface
fun interface AFunction13<in P1, in P2, in P3, in P4, in P5, in P6, in P7, in P8, in P9, in P10, in P11, in P12, in P13, out R> :
    AFunction<R> {
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
    ): R
}

/** A function that takes 14 arguments. */
@FunctionalInterface
fun interface AFunction14<in P1, in P2, in P3, in P4, in P5, in P6, in P7, in P8, in P9, in P10, in P11, in P12, in P13, in P14, out R> :
    AFunction<R> {
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
    ): R
}

/** A function that takes 15 arguments. */
@FunctionalInterface
fun interface AFunction15<in P1, in P2, in P3, in P4, in P5, in P6, in P7, in P8, in P9, in P10, in P11, in P12, in P13, in P14, in P15, out R> :
    AFunction<R> {
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
    ): R
}

/** A function that takes 16 arguments. */
@FunctionalInterface
fun interface AFunction16<in P1, in P2, in P3, in P4, in P5, in P6, in P7, in P8, in P9, in P10, in P11, in P12, in P13, in P14, in P15, in P16, out R> :
    AFunction<R> {
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
    ): R
}

/** A function that takes 17 arguments. */
@FunctionalInterface
fun interface AFunction17<in P1, in P2, in P3, in P4, in P5, in P6, in P7, in P8, in P9, in P10, in P11, in P12, in P13, in P14, in P15, in P16, in P17, out R> :
    AFunction<R> {
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
    ): R
}

/** A function that takes 18 arguments. */
@FunctionalInterface
fun interface AFunction18<in P1, in P2, in P3, in P4, in P5, in P6, in P7, in P8, in P9, in P10, in P11, in P12, in P13, in P14, in P15, in P16, in P17, in P18, out R> :
    AFunction<R> {
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
    ): R
}

/** A function that takes 19 arguments. */
@FunctionalInterface
fun interface AFunction19<in P1, in P2, in P3, in P4, in P5, in P6, in P7, in P8, in P9, in P10, in P11, in P12, in P13, in P14, in P15, in P16, in P17, in P18, in P19, out R> :
    AFunction<R> {
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
    ): R
}

/** A function that takes 20 arguments. */
@FunctionalInterface
fun interface AFunction20<in P1, in P2, in P3, in P4, in P5, in P6, in P7, in P8, in P9, in P10, in P11, in P12, in P13, in P14, in P15, in P16, in P17, in P18, in P19, in P20, out R> :
    AFunction<R> {
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
    ): R
}

/** A function that takes 21 arguments. */
@FunctionalInterface
fun interface AFunction21<in P1, in P2, in P3, in P4, in P5, in P6, in P7, in P8, in P9, in P10, in P11, in P12, in P13, in P14, in P15, in P16, in P17, in P18, in P19, in P20, in P21, out R> :
    AFunction<R> {
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
    ): R
}

/** A function that takes 22 arguments. */
@FunctionalInterface
fun interface AFunction22<in P1, in P2, in P3, in P4, in P5, in P6, in P7, in P8, in P9, in P10, in P11, in P12, in P13, in P14, in P15, in P16, in P17, in P18, in P19, in P20, in P21, in P22, out R> :
    AFunction<R> {
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
    ): R
}
