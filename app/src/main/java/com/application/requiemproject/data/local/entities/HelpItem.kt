package com.application.requiemproject.data.local.entities

/**
 * Represents a help entry consisting of a question and its corresponding solution.
 *
 * This data class is used to populate items in a bottom sheet dialog within a fragment.
 * Each instance holds a help question (e.g., "How do I reset my password?") and
 * its detailed solution, which is displayed to the user when the item is selected.
 *
 * The id field is typically used as a primary key when storing these entries
 * in a database (e.g., Room or just a mutable list). A default value of 0 indicates a new or unsaved
 * entity.
 *
 * @property id Unique identifier for the help item. Defaults to 0 for new items.
 * @property question The user-facing question or title of the help entry.
 * @property solution The detailed answer or solution corresponding to the question.
 *
 * @since 1.0.0
 */
data class HelpItem(
    val id: Long = 0,
    val question: String,
    val solution: String
)