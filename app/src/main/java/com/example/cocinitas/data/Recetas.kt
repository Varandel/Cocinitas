package com.example.cocinitas.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class TipoComida {
    @SerialName("Comida") Comida,
    @SerialName("Cena") Cena
}

@Serializable
data class MedidaIngrediente(
    @SerialName("Volumen (en mL)") val volumenMl: Double? = null,
    @SerialName("Masa (en gramos)") val masaGramos: Double? = null,
    @SerialName("Cantidad") val cantidad: Double? = null
)

@Serializable
data class Ingrediente(
    @SerialName("Nombre") val nombre: String,
    // Como tu esquema define los valores dentro del nombre o como propiedades de medida:
    val medidas: MedidaIngrediente
)

@Serializable
data class Receta(
    @SerialName("id_Receta") val idReceta: Int,
    @SerialName("Nombre") val nombre: String,
    @SerialName("Tipo Alimentos") val tipoAlimentos: List<String>,
    @SerialName("Pasos Receta") val pasosReceta: List<String>,
    @SerialName("Ingredientes") val ingredientes: List<Ingrediente>,
    @SerialName("Tipo Comida") val tipoComida: TipoComida
)