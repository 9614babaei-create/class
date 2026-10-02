package com.example.classattendance

object Jalali {
    fun toJalali(gy: Int, gm: Int, gd: Int): Triple<Int,Int,Int> {
        val gdm = intArrayOf(0,31,59,90,120,151,181,212,243,273,304,334)
        var gy2 = gy
        if (gm > 2) gy2++
        var days = 355666 + 365*gy + (gy2+3)/4 - (gy2+99)/100 + (gy2+399)/400 + gd + gdm[gm-1]
        var jy = -1595 + 33*(days/12053)
        days %= 12053
        jy += 4*(days/1461)
        days %= 1461
        if (days > 365) {
            jy += (days-1)/365
            days = (days-1)%365
        }
        val jm = if (days < 186) 1 + days/31 else 7 + (days-186)/30
        val jd = 1 + if (days < 186) days%31 else (days-186)%30
        return Triple(jy,jm,jd)
    }
}
