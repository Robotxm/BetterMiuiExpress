package com.moefactory.bettermiuiexpress.base.converter

import com.moefactory.bettermiuiexpress.model.CaiNiaoRequestData
import com.moefactory.bettermiuiexpress.repository.ExpressActualRepository
import kotlinx.serialization.encodeToString
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

class CaiNiaoRequestDataConverterFactory : Converter.Factory() {

    override fun stringConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<*, String>? {
        return if (type == CaiNiaoRequestData::class.java) CaiNiaoRequestDataConverter() else null
    }

    private class CaiNiaoRequestDataConverter: Converter<CaiNiaoRequestData, String> {
        override fun convert(value: CaiNiaoRequestData): String {
            return ExpressActualRepository.jsonParser.encodeToString(value)
        }
    }

    companion object {
        fun create() = CaiNiaoRequestDataConverterFactory()
    }
}