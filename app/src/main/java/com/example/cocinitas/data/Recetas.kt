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
    @SerialName("Cantidad") val cantidad: Int? = null,
    @SerialName("Otro") val textoLibre: String? = null
)

@Serializable
data class Ingrediente(
    @SerialName("Nombre") val nombre: String,
    val medidas: MedidaIngrediente
)

@Serializable
data class Receta(
    @SerialName("Nombre") val nombre: String,
    @SerialName("Tipo Alimentos") val tipoAlimentos: List<String>,
    @SerialName("Pasos Receta") val pasosReceta: List<String>,
    @SerialName("Ingredientes") val ingredientes: List<Ingrediente>,
    @SerialName("Tipos Comida") val tiposComida: List<TipoComida>
)

enum class UnidadMedida(val label: String) {
    VOLUMEN("Volumen (en mL)"),
    MASA("Masa (en gramos)"),
    CANTIDAD("Cantidad"),
    OTRO("Otro")
}

fun formatearMedida(ingrediente: Ingrediente): String {
    val m = ingrediente.medidas
    return when {
        m.volumenMl != null -> "${m.volumenMl} mL"
        m.masaGramos != null -> "${m.masaGramos} g"
        m.cantidad != null -> "${m.cantidad} uds"
        m.textoLibre != null -> m.textoLibre
        else -> "Al gusto"
    }
}