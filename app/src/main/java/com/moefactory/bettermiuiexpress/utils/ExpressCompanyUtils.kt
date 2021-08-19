package com.moefactory.bettermiuiexpress.utils

object ExpressCompanyUtils {
    
    private val convertMap: MutableMap<String, String> = mutableMapOf<String, String>().apply {
        this[Kuaidi100Code.SHUN_FENG] = CaiNiaoCode.SHUN_FENG
        this[Kuaidi100Code.SHEN_TONG] = CaiNiaoCode.SHEN_TONG
        this[Kuaidi100Code.YUAN_TONG] = CaiNiaoCode.YUAN_TONG
        this[Kuaidi100Code.ZHONG_TONG] = CaiNiaoCode.ZHONG_TONG
        this[Kuaidi100Code.GUO_TONG] = CaiNiaoCode.GUO_TONG
        this[Kuaidi100Code.DE_BANG] = CaiNiaoCode.DE_BANG
        this[Kuaidi100Code.YOU_ZHENG_CN] = CaiNiaoCode.YOU_ZHENG_CN
        this[Kuaidi100Code.YOU_ZHENG_IN] = CaiNiaoCode.YOU_ZHENG_IN
        this[Kuaidi100Code.EMS] = CaiNiaoCode.EMS
        this[Kuaidi100Code.EMS_IN] = CaiNiaoCode.EMS_IN
        this[Kuaidi100Code.YUN_DA] = CaiNiaoCode.YUN_DA
        this[Kuaidi100Code.HUI_TONG] = CaiNiaoCode.HUI_TONG
        this[Kuaidi100Code.QUAN_FENG] = CaiNiaoCode.QUAN_FENG
        this[Kuaidi100Code.RU_FENG_DA] = CaiNiaoCode.RU_FENG_DA
        this[Kuaidi100Code.ZHONG_TIE] = CaiNiaoCode.ZHONG_TIE
        this[Kuaidi100Code.UPS] = CaiNiaoCode.UPS
        this[Kuaidi100Code.DHL] = CaiNiaoCode.DHL
        this[Kuaidi100Code.TIAN_TIAN] = CaiNiaoCode.TIAN_TIAN
        this[Kuaidi100Code.KUAI_JIE] = CaiNiaoCode.KUAI_JIE
        this[Kuaidi100Code.JING_DONG] = CaiNiaoCode.JING_DONG
        this[Kuaidi100Code.JIA_YI] = CaiNiaoCode.JIA_YI
        this[Kuaidi100Code.BAI_SHI] = CaiNiaoCode.BAI_SHI
        this[Kuaidi100Code.YOU_SU] = CaiNiaoCode.YOU_SU
        this[Kuaidi100Code.WAN_XIANG] = CaiNiaoCode.WAN_XIANG
        this[Kuaidi100Code.CHENG_BANG] = CaiNiaoCode.CHENG_BANG
        this[Kuaidi100Code.SU_NING] = CaiNiaoCode.SU_NING
        this[Kuaidi100Code.ZHI_MA_KAI_MEN] = CaiNiaoCode.ZHI_MA_KAI_MEN
        this[Kuaidi100Code.AN_NENG] = CaiNiaoCode.AN_NENG
        this[Kuaidi100Code.LONG_BANG] = CaiNiaoCode.LONG_BANG
    }

    object Kuaidi100Code {
        const val AN_NENG = "annengwuliu"
        const val BAI_SHI = "baishiwuliu"
        const val CHENG_BANG = "chengbang"
        const val DE_BANG = "debangwuliu"
        const val DHL = "dhl"
        const val EMS = "ems"
        const val EMS_IN = "emsguoji"
        const val GUO_TONG = "guotongkuaidi"
        const val HUI_TONG = "huitongkuaidi"
        const val JIA_YI = "jiayiwuliu"
        const val JING_DONG = "jd"
        const val KUAI_JIE = "kuaijiesudi"
        const val LONG_BANG = "longbangwuliu"
        const val QUAN_FENG = "quanfengkuaidi"
        const val RU_FENG_DA = "rufengda"
        const val SHEN_TONG = "shentong"
        const val SHUN_FENG = "shunfeng"
        const val SU_NING = "suning"
        const val TIAN_TIAN = "tiantian"
        const val UPS = "ups"
        const val WAN_XIANG = "wanxiangwuliu"
        const val YOU_SU = "yousuwuliu"
        const val YOU_ZHENG_CN = "youzhengguonei"
        const val YOU_ZHENG_IN = "youzhengguoji"
        const val YUAN_TONG = "yuantong"
        const val YUN_DA = "yunda"
        const val ZHI_MA_KAI_MEN = "zhimakaimen"
        const val ZHONG_TIE = "zhongtiekuaiyun"
        const val ZHONG_TONG = "zhongtong"
    }

    object CaiNiaoCode {
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

    fun convertState(i: Int): Int {
        return when (i) {
            0 -> 102
            1 -> 104
            2 -> 105
            3 -> 109
            4 -> 108
            5 -> 107
            6 -> 111
            else -> -1
        }
    }

    fun convertStateV2(i: Int): Int {
        return when (i) {
            0 -> 101
            1 -> 103
            2, 3, 4 -> 104
            5 -> 105
            6 -> 109
            7 -> 108
            8 -> 106
            9 -> 107
            else -> -1
        }
    }

    fun convertCode(cainiaoCode: String): String? {
        return convertMap.filterValues { it == cainiaoCode }.keys.firstOrNull()
    }
}
