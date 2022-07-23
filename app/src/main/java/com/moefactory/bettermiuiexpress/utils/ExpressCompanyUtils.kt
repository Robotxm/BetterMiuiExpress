package com.moefactory.bettermiuiexpress.utils

data class ExpressCompany(
    val kuaiDi100Code: String?,
    val cainiaoCode: String?
)

object ExpressCompanyUtils {

    private val expressCompanies = listOf(
        ExpressCompany(Kuaidi100Code.SHUN_FENG, CaiNiaoCode.SHUN_FENG),
        ExpressCompany(Kuaidi100Code.SHEN_TONG, CaiNiaoCode.SHEN_TONG),
        ExpressCompany(Kuaidi100Code.YUAN_TONG, CaiNiaoCode.YUAN_TONG),
        ExpressCompany(Kuaidi100Code.ZHONG_TONG, CaiNiaoCode.ZHONG_TONG),
        ExpressCompany(Kuaidi100Code.GUO_TONG, CaiNiaoCode.GUO_TONG),
        ExpressCompany(Kuaidi100Code.DE_BANG, CaiNiaoCode.DE_BANG),
        ExpressCompany(Kuaidi100Code.YOU_ZHENG_CN, CaiNiaoCode.YOU_ZHENG_CN),
        ExpressCompany(Kuaidi100Code.YOU_ZHENG_IN, CaiNiaoCode.YOU_ZHENG_IN),
        ExpressCompany(Kuaidi100Code.EMS, CaiNiaoCode.EMS),
        ExpressCompany(Kuaidi100Code.EMS_IN, CaiNiaoCode.EMS_IN),
        ExpressCompany(Kuaidi100Code.YUN_DA, CaiNiaoCode.YUN_DA),
        ExpressCompany(Kuaidi100Code.HUI_TONG, CaiNiaoCode.HUI_TONG),
        ExpressCompany(Kuaidi100Code.QUAN_FENG, CaiNiaoCode.QUAN_FENG),
        ExpressCompany(Kuaidi100Code.RU_FENG_DA, CaiNiaoCode.RU_FENG_DA),
        ExpressCompany(Kuaidi100Code.ZHONG_TIE, CaiNiaoCode.ZHONG_TIE),
        ExpressCompany(Kuaidi100Code.UPS, CaiNiaoCode.UPS),
        ExpressCompany(Kuaidi100Code.DHL, CaiNiaoCode.DHL),
        ExpressCompany(Kuaidi100Code.TIAN_TIAN, CaiNiaoCode.TIAN_TIAN),
        ExpressCompany(Kuaidi100Code.KUAI_JIE, CaiNiaoCode.KUAI_JIE),
        ExpressCompany(Kuaidi100Code.JING_DONG, CaiNiaoCode.JING_DONG),
        ExpressCompany(Kuaidi100Code.JIA_YI, CaiNiaoCode.JIA_YI),
        ExpressCompany(Kuaidi100Code.BAI_SHI, CaiNiaoCode.BAI_SHI),
        ExpressCompany(Kuaidi100Code.YOU_SU, CaiNiaoCode.YOU_SU),
        ExpressCompany(Kuaidi100Code.WAN_XIANG, CaiNiaoCode.WAN_XIANG),
        ExpressCompany(Kuaidi100Code.CHENG_BANG, CaiNiaoCode.CHENG_BANG),
        ExpressCompany(Kuaidi100Code.SU_NING, CaiNiaoCode.SU_NING),
        ExpressCompany(Kuaidi100Code.ZHI_MA_KAI_MEN, CaiNiaoCode.ZHI_MA_KAI_MEN),
        ExpressCompany(Kuaidi100Code.AN_NENG, CaiNiaoCode.AN_NENG),
        ExpressCompany(Kuaidi100Code.LONG_BANG, CaiNiaoCode.LONG_BANG),
    )

    private object Kuaidi100Code {
        const val AN_NENG = "annengwuliu"
        const val BAI_SHI = "baishiwuliu"
        val CHENG_BANG = null // "chengbang" // Incorrect
        const val DE_BANG = "debangwuliu"
        const val DHL = "dhl"
        const val EMS = "ems"
        const val EMS_IN = "emsguoji"
        const val GUO_TONG = "guotongkuaidi"
        const val HUI_TONG = "huitongkuaidi"
        const val JIA_YI = "jiayiwuliu"
        const val JING_DONG = "jd"
        const val KUAI_JIE = "kuaijiesudi"
        val LONG_BANG = null // "longbangwuliu" // Incorrect
        const val QUAN_FENG = "quanfengkuaidi"
        val RU_FENG_DA = null // "rufengda" // Incorrect
        const val SHEN_TONG = "shentong"
        const val SHUN_FENG = "shunfeng"
        const val SU_NING = "suning"
        const val TIAN_TIAN = "tiantian"
        const val UPS = "ups"
        const val WAN_XIANG = "wanxiangwuliu"
        val YOU_SU = null // "yousuwuliu" // Incorrect
        const val YOU_ZHENG_CN = "youzhengguonei"
        const val YOU_ZHENG_IN = "youzhengguoji"
        const val YUAN_TONG = "yuantong"
        const val YUN_DA = "yunda"
        const val ZHI_MA_KAI_MEN = "zhimakaimen"
        val ZHONG_TIE = null // "zhongtiekuaiyun" // Incorrect
        const val ZHONG_TONG = "zhongtong"
    }

    private object CaiNiaoCode {
        const val AN_NENG = "ANE56"
        const val BAI_SHI = "BESTQJT"
        const val CHENG_BANG = "CHENGBANG"
        const val DE_BANG = "DBL"
        const val DHL = "DHL"
        const val EMS = "EMS"
        const val EMS_IN = "EMSGJ"
        const val GUO_TONG = "GTO"
        const val HUI_TONG = "HTKY"
        const val JIA_YI = "JIAYI"
        const val JING_DONG = "JDKD"
        const val KUAI_JIE = "FAST"
        const val LONG_BANG = "LB"
        const val QUAN_FENG = "QFKD"
        const val RU_FENG_DA = "RFD"
        const val SHEN_TONG = "STO"
        const val SHUN_FENG = "SF"
        const val SU_NING = "SNWL"
        const val TIAN_TIAN = "TTKDEX"
        const val UPS = "UPS"
        const val WAN_XIANG = "EWINSHINE"
        const val YOU_SU = "UC"
        const val YOU_ZHENG_CN = "POSTB"
        const val YOU_ZHENG_IN = "CNPOSTGJ"
        const val YUAN_TONG = "YTO"
        const val YUN_DA = "YUNDA"
        const val ZHI_MA_KAI_MEN = "ZMKM"
        const val ZHONG_TIE = "CRE"
        const val ZHONG_TONG = "ZTO"
    }

    fun convertCode(cainiaoCode: String): String? {
        return expressCompanies.find { it.cainiaoCode == cainiaoCode }?.kuaiDi100Code
    }
}
