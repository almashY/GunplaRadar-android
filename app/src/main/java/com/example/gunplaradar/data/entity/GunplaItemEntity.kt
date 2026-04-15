package com.example.gunplaradar.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "gunpla_items")
data class GunplaItemEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val grade: String,
    val price: Int? = null,
    val imageData: ByteArray? = null,
    val url: String? = null,
    val releaseDate: Long? = null,
    val restockDate: Long? = null,
    val purchasedDate: Long? = null,
    val purchaseStoreId: String? = null,
    val priority: Int = 2,
    val tagColor: Int = 0
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as GunplaItemEntity
        if (id != other.id) return false
        if (name != other.name) return false
        if (grade != other.grade) return false
        if (price != other.price) return false
        if (imageData != null) {
            if (other.imageData == null) return false
            if (!imageData.contentEquals(other.imageData)) return false
        } else if (other.imageData != null) return false
        if (url != other.url) return false
        if (releaseDate != other.releaseDate) return false
        if (restockDate != other.restockDate) return false
        if (purchasedDate != other.purchasedDate) return false
        if (purchaseStoreId != other.purchaseStoreId) return false
        if (priority != other.priority) return false
        if (tagColor != other.tagColor) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + grade.hashCode()
        result = 31 * result + (price ?: 0)
        result = 31 * result + (imageData?.contentHashCode() ?: 0)
        result = 31 * result + (url?.hashCode() ?: 0)
        result = 31 * result + (releaseDate?.hashCode() ?: 0)
        result = 31 * result + (restockDate?.hashCode() ?: 0)
        result = 31 * result + (purchasedDate?.hashCode() ?: 0)
        result = 31 * result + (purchaseStoreId?.hashCode() ?: 0)
        result = 31 * result + priority
        result = 31 * result + tagColor
        return result
    }
}
