package com.moefactory.bettermiuiexpress.base.converter

import com.moefactory.bettermiuiexpress.model.KuaiDi100RequestParam
import com.moefactory.bettermiuiexpress.repository.ExpressActualRepository
import kotlinx.serialization.encodeToString
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

class KuaiDi100RequestDataConverterFactory : Converter.Factory() {

    override fun stringConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<*, String>? {
        return if (type == KuaiDi100RequestParam::class.java) KuaiDi100RequestDataConverter() else null
    }

    private class KuaiDi100RequestDataConverter: Converter<KuaiDi100RequestParam, String> {
        override fun convert(value: KuaiDi100RequestParam): String {
            return ExpressActualRepository.jsonParser.encodeToString(value)
        }
    }

    companion object {
        fun create() = KuaiDi100RequestDataConverterFactory()
    }
}