package iitr.collector.shopping.ar.data

class Product(
    val id: String,
    val name: String,
    val category:String,
    val rating: Double,
    val image: String,
    val images: String,
    val price: Double,
    val discount: Double,
    val mrp: Double,
    val description: String,
    val stock: Int,
    val popularity: Int,
    val cat: String,
    val model: String,
    val type: String = "normal"
) {
    fun toMap(arriving: String): Map<String, Any> {
        return mapOf(
            "id" to id,
            "name" to name,
            "category" to category,
            "rating" to rating,
            "image" to image,
            "images" to images,
            "price" to price,
            "discount" to discount,
            "mrp" to mrp,
            "description" to description,
            "stock" to stock,
            "popularity" to popularity,
            "arriving" to arriving,
            "cat" to cat,
            "model" to "model"
        )
    }
}